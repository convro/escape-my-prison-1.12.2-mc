package com.prisonbreakmod.ai;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.prisonbreakmod.PrisonBreakMod;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses a raw JSON string from the DeepSeek Chat Completions API into an
 * {@link AIResponse}.
 *
 * <p>Parsing strategy (in order):
 * <ol>
 *   <li>Extract {@code choices[0].message.content} from the outer API envelope.</li>
 *   <li>Parse the content string as a JSON object and map fields to
 *       {@link AIResponse.Builder}.</li>
 *   <li>If any JSON parsing step fails, fall back to a regex keyword scan of
 *       the raw text to guess the most appropriate {@code action}.</li>
 * </ol>
 */
public final class AIResponseParser {

    // -------------------------------------------------------------------------
    // Action keyword patterns for regex fallback
    // -------------------------------------------------------------------------

    private static final Pattern PAT_PATROL  = Pattern.compile("\\bpatrol\\b",  Pattern.CASE_INSENSITIVE);
    private static final Pattern PAT_ALERT   = Pattern.compile("\\balert\\b",   Pattern.CASE_INSENSITIVE);
    private static final Pattern PAT_SPEAK   = Pattern.compile("\\bspeak\\b",   Pattern.CASE_INSENSITIVE);
    private static final Pattern PAT_ATTACK  = Pattern.compile("\\battack\\b",  Pattern.CASE_INSENSITIVE);
    private static final Pattern PAT_IDLE    = Pattern.compile("\\bidle\\b",    Pattern.CASE_INSENSITIVE);

    // Suppress instantiation
    private AIResponseParser() {}

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Parse the raw JSON string returned by the DeepSeek API.
     *
     * @param rawJson full HTTP response body from the completions endpoint
     * @return a populated {@link AIResponse}, or {@link AIResponse#fallback()} on failure
     */
    public static AIResponse parse(String rawJson) {
        if (rawJson == null || rawJson.trim().isEmpty()) {
            PrisonBreakMod.LOGGER.warn("[AIResponseParser] Received null or empty JSON — using fallback.");
            return AIResponse.fallback();
        }

        // Step 1: Extract the message content from the API envelope.
        String content = extractContent(rawJson);
        if (content == null) {
            PrisonBreakMod.LOGGER.warn("[AIResponseParser] Could not extract content from API envelope — using fallback.");
            return AIResponse.fallback();
        }

        // Step 2: Try to parse the content as a structured JSON object.
        AIResponse response = parseContentJson(content);
        if (response != null) {
            return response;
        }

        // Step 3: Regex keyword fallback.
        PrisonBreakMod.LOGGER.warn("[AIResponseParser] Content is not valid JSON — attempting regex keyword extraction.");
        return regexFallback(content);
    }

    // -------------------------------------------------------------------------
    // Step 1 — extract choices[0].message.content
    // -------------------------------------------------------------------------

    private static String extractContent(String rawJson) {
        try {
            JsonParser parser = new JsonParser();
            JsonElement root = parser.parse(rawJson);
            if (!root.isJsonObject()) {
                return null;
            }
            JsonObject obj = root.getAsJsonObject();

            // choices array
            if (!obj.has("choices") || !obj.get("choices").isJsonArray()) {
                return null;
            }
            JsonElement firstChoice = obj.getAsJsonArray("choices").get(0);
            if (firstChoice == null || !firstChoice.isJsonObject()) {
                return null;
            }

            JsonObject choice = firstChoice.getAsJsonObject();
            if (!choice.has("message") || !choice.get("message").isJsonObject()) {
                return null;
            }

            JsonObject message = choice.getAsJsonObject("message");
            if (!message.has("content") || message.get("content").isJsonNull()) {
                return null;
            }

            return message.get("content").getAsString();
        } catch (Exception e) {
            PrisonBreakMod.LOGGER.error("[AIResponseParser] Exception extracting content: {}", e.getMessage());
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // Step 2 — parse the content string as structured JSON
    // -------------------------------------------------------------------------

    /**
     * Attempts to parse the content string as a JSON object with the expected
     * NPC response schema. Returns {@code null} if parsing fails so that the
     * caller can fall through to the regex fallback.
     */
    private static AIResponse parseContentJson(String content) {
        // The model sometimes wraps JSON in a markdown code fence — strip it.
        String cleaned = content.trim();
        if (cleaned.startsWith("```")) {
            int start = cleaned.indexOf('{');
            int end   = cleaned.lastIndexOf('}');
            if (start >= 0 && end > start) {
                cleaned = cleaned.substring(start, end + 1);
            }
        }

        try {
            JsonParser parser = new JsonParser();
            JsonElement el = parser.parse(cleaned);
            if (!el.isJsonObject()) {
                return null;
            }
            JsonObject obj = el.getAsJsonObject();

            AIResponse.Builder builder = new AIResponse.Builder();

            if (obj.has("action") && !obj.get("action").isJsonNull()) {
                builder.action(obj.get("action").getAsString().trim().toLowerCase());
            }
            if (obj.has("movementX") && !obj.get("movementX").isJsonNull()) {
                builder.movementX(obj.get("movementX").getAsDouble());
            }
            if (obj.has("movementY") && !obj.get("movementY").isJsonNull()) {
                builder.movementY(obj.get("movementY").getAsDouble());
            }
            if (obj.has("movementZ") && !obj.get("movementZ").isJsonNull()) {
                builder.movementZ(obj.get("movementZ").getAsDouble());
            }
            if (obj.has("dialogue") && !obj.get("dialogue").isJsonNull()) {
                builder.dialogue(obj.get("dialogue").getAsString());
            }
            if (obj.has("alertChange") && !obj.get("alertChange").isJsonNull()) {
                builder.alertChange(obj.get("alertChange").getAsInt());
            }
            if (obj.has("memory") && !obj.get("memory").isJsonNull()) {
                builder.memory(obj.get("memory").getAsString());
            }
            if (obj.has("reason") && !obj.get("reason").isJsonNull()) {
                builder.reason(obj.get("reason").getAsString());
            }
            if (obj.has("relChange") && !obj.get("relChange").isJsonNull()) {
                builder.relChange(obj.get("relChange").getAsInt());
            }
            if (obj.has("revealedInfo") && !obj.get("revealedInfo").isJsonNull()) {
                builder.revealedInfo(obj.get("revealedInfo").getAsString());
            }

            return builder.build();

        } catch (Exception e) {
            PrisonBreakMod.LOGGER.debug("[AIResponseParser] Content JSON parse failed: {}", e.getMessage());
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // Step 3 — regex keyword fallback
    // -------------------------------------------------------------------------

    /**
     * Scans the text for known action keywords and builds a minimal
     * {@link AIResponse} from the first match found.
     * Priority: attack > alert > patrol > speak > idle.
     */
    private static AIResponse regexFallback(String text) {
        String action = "idle";

        if (matches(PAT_ATTACK, text)) {
            action = "attack";
        } else if (matches(PAT_ALERT, text)) {
            action = "alert";
        } else if (matches(PAT_PATROL, text)) {
            action = "patrol";
        } else if (matches(PAT_SPEAK, text)) {
            action = "speak";
        } else if (matches(PAT_IDLE, text)) {
            action = "idle";
        }

        PrisonBreakMod.LOGGER.info("[AIResponseParser] Regex fallback resolved action='{}'.", action);

        return new AIResponse.Builder()
                .action(action)
                .reason("Parsed via regex keyword fallback.")
                .build();
    }

    private static boolean matches(Pattern p, String text) {
        Matcher m = p.matcher(text);
        return m.find();
    }
}
