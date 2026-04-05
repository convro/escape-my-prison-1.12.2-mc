package com.prisonbreakmod.ai;

import com.prisonbreak.shadow.okhttp3.MediaType;
import com.prisonbreak.shadow.okhttp3.OkHttpClient;
import com.prisonbreak.shadow.okhttp3.Request;
import com.prisonbreak.shadow.okhttp3.RequestBody;
import com.prisonbreak.shadow.okhttp3.Response;
import com.prisonbreak.shadow.okhttp3.ResponseBody;
import com.prisonbreakmod.PrisonBreakMod;
import com.prisonbreakmod.config.PrisonConfig;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * HTTP client that wraps the DeepSeek Chat Completions API.
 *
 * <p>Uses OkHttp3 (shaded under {@code com.prisonbreak.shadow.okhttp3}) so that
 * the library does not conflict with any OkHttp version bundled by Minecraft or
 * other mods.
 *
 * <p>Access via {@link #getInstance()} — the singleton is lazily initialised
 * with double-checked locking and remains alive for the entire JVM session.
 * Call {@link #shutdown()} during mod teardown to release threads gracefully.
 */
public final class DeepSeekClient {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    /** General-purpose chat model identifier. */
    public static final String MODEL_CHAT   = "deepseek-chat";

    /** Extended reasoning model identifier. */
    public static final String MODEL_REASON = "deepseek-reasoner";

    private static final MediaType JSON_MEDIA_TYPE =
            MediaType.parse("application/json; charset=utf-8");

    // -------------------------------------------------------------------------
    // Singleton
    // -------------------------------------------------------------------------

    private static volatile DeepSeekClient INSTANCE;

    public static DeepSeekClient getInstance() {
        if (INSTANCE == null) {
            synchronized (DeepSeekClient.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DeepSeekClient();
                }
            }
        }
        return INSTANCE;
    }

    // -------------------------------------------------------------------------
    // Instance state
    // -------------------------------------------------------------------------

    private final OkHttpClient http;
    private final RateLimiter  limiter;

    /**
     * Dedicated thread pool for AI HTTP calls.
     * 8 threads to saturate the rate limiter ceiling without starving the
     * server thread.
     */
    private final ExecutorService aiPool;

    // -------------------------------------------------------------------------
    // Constructor (private)
    // -------------------------------------------------------------------------

    private DeepSeekClient() {
        this.limiter = new RateLimiter(PrisonConfig.rateLimitPerMinute);

        this.http = new OkHttpClient.Builder()
                .connectTimeout(PrisonConfig.timeoutMs, TimeUnit.MILLISECONDS)
                .readTimeout(PrisonConfig.timeoutMs, TimeUnit.MILLISECONDS)
                .writeTimeout(PrisonConfig.timeoutMs, TimeUnit.MILLISECONDS)
                .build();

        this.aiPool = Executors.newFixedThreadPool(8, r -> {
            Thread t = new Thread(r, "PrisonBreak-AIPool");
            t.setDaemon(true);
            return t;
        });

        PrisonBreakMod.LOGGER.info("[DeepSeekClient] Initialized. Model pool: 8 threads, rate: {}/min.",
                PrisonConfig.rateLimitPerMinute);
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Submits a chat-completion request to DeepSeek asynchronously.
     *
     * <p>The returned future completes on the AI thread pool with either a
     * parsed {@link AIResponse} or {@link AIResponse#fallback()} on any error.
     *
     * @param systemPrompt instruction context to prepend as the "system" message
     * @param userMsg      the user-role message (NPC observation or dialogue)
     * @param model        one of {@link #MODEL_CHAT} or {@link #MODEL_REASON}
     * @param maxTokens    maximum tokens the model may generate
     * @return a non-null {@link CompletableFuture} that never completes exceptionally
     */
    public CompletableFuture<AIResponse> queryAsync(
            String systemPrompt,
            String userMsg,
            String model,
            int maxTokens) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                // Honour rate limit — blocks until a token is available.
                limiter.acquire();
                return doRequest(systemPrompt, userMsg, model, maxTokens);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                PrisonBreakMod.LOGGER.warn("[DeepSeekClient] Thread interrupted while acquiring rate-limit slot.");
                return AIResponse.fallback();
            } catch (Exception e) {
                PrisonBreakMod.LOGGER.error("[DeepSeekClient] Unexpected error: {}", e.getMessage());
                return AIResponse.fallback();
            }
        }, aiPool);
    }

    /**
     * Shuts down the AI thread pool and rate-limiter scheduler.
     * Should be called when the mod/server is stopping.
     */
    public void shutdown() {
        aiPool.shutdown();
        try {
            if (!aiPool.awaitTermination(5, TimeUnit.SECONDS)) {
                aiPool.shutdownNow();
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            aiPool.shutdownNow();
        }
        limiter.shutdown();
        PrisonBreakMod.LOGGER.info("[DeepSeekClient] Shut down.");
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Builds the JSON request body, fires the HTTP call, and parses the result.
     */
    private AIResponse doRequest(
            String systemPrompt,
            String userMsg,
            String model,
            int maxTokens) throws IOException {

        String apiKey = PrisonConfig.apiKey;
        if (apiKey == null || apiKey.trim().isEmpty()) {
            PrisonBreakMod.LOGGER.warn("[DeepSeekClient] apiKey is not configured — returning fallback.");
            return AIResponse.fallback();
        }

        String baseUrl = PrisonConfig.baseUrl;
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            baseUrl = "https://api.deepseek.com/v1/chat/completions";
        }

        String jsonBody = buildRequestBody(model, maxTokens, systemPrompt, userMsg);

        Request request = new Request.Builder()
                .url(baseUrl)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type",  "application/json")
                .post(RequestBody.create(JSON_MEDIA_TYPE, jsonBody))
                .build();

        PrisonBreakMod.LOGGER.debug("[DeepSeekClient] Sending request to {} with model={}, maxTokens={}.",
                baseUrl, model, maxTokens);

        try (Response response = http.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                PrisonBreakMod.LOGGER.warn("[DeepSeekClient] HTTP {} from API — returning fallback.",
                        response.code());
                return AIResponse.fallback();
            }

            ResponseBody body = response.body();
            if (body == null) {
                PrisonBreakMod.LOGGER.warn("[DeepSeekClient] Empty response body — returning fallback.");
                return AIResponse.fallback();
            }

            String rawJson = body.string();
            PrisonBreakMod.LOGGER.debug("[DeepSeekClient] Received {} chars from API.", rawJson.length());
            return AIResponseParser.parse(rawJson);
        }
    }

    /**
     * Constructs the JSON request body manually to avoid a hard Gson dependency
     * at the shaded-library level, and to keep the payload minimal.
     *
     * <p>Format:
     * <pre>
     * {
     *   "model": "...",
     *   "max_tokens": N,
     *   "temperature": 0.8,
     *   "messages": [
     *     {"role": "system", "content": "..."},
     *     {"role": "user",   "content": "..."}
     *   ]
     * }
     * </pre>
     */
    private static String buildRequestBody(
            String model,
            int maxTokens,
            String systemPrompt,
            String userMsg) {

        // Escape the strings to produce valid JSON without a full serialiser.
        String escapedSystem = jsonEscape(systemPrompt);
        String escapedUser   = jsonEscape(userMsg);
        String escapedModel  = jsonEscape(model);

        return "{"
             + "\"model\":" + "\"" + escapedModel + "\","
             + "\"max_tokens\":" + maxTokens + ","
             + "\"temperature\":0.8,"
             + "\"messages\":["
             +   "{\"role\":\"system\",\"content\":\"" + escapedSystem + "\"},"
             +   "{\"role\":\"user\",\"content\":\"" + escapedUser + "\"}"
             + "]"
             + "}";
    }

    /**
     * Minimal JSON string escaping — handles the characters that would break
     * a JSON string literal.
     */
    private static String jsonEscape(String input) {
        if (input == null) return "";
        StringBuilder sb = new StringBuilder(input.length() + 16);
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b");  break;
                case '\f': sb.append("\\f");  break;
                case '\n': sb.append("\\n");  break;
                case '\r': sb.append("\\r");  break;
                case '\t': sb.append("\\t");  break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }
}
