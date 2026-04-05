package com.prisonbreakmod.config;

import com.prisonbreakmod.PrisonBreakMod;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.LangKey;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RangeInt;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Forge @Config-annotated configuration class for Prison Break: Gulag.
 *
 * Values are automatically loaded from / saved to
 * {@code config/prisonbreakmod.cfg} by Forge.  Whenever the player changes a
 * value in the in-game config GUI the {@link OnConfigChanged#onConfigChanged}
 * event handler re-syncs the in-memory values.
 */
@Config(modid = PrisonBreakMod.MODID, type = Type.INSTANCE, name = "prisonbreakmod")
@LangKey("config.prisonbreakmod.title")
public final class PrisonConfig {

    // -------------------------------------------------------------------------
    // AI / API settings
    // -------------------------------------------------------------------------

    @Name("apiKey")
    @Comment({
        "DeepSeek API key.",
        "Leave blank to run in offline / fallback mode."
    })
    @LangKey("config.prisonbreakmod.apiKey")
    public static String apiKey = "";

    @Name("baseUrl")
    @Comment("Full URL of the DeepSeek chat-completions endpoint.")
    @LangKey("config.prisonbreakmod.baseUrl")
    public static String baseUrl = "https://api.deepseek.com/v1/chat/completions";

    @Name("rateLimitPerMinute")
    @Comment("Maximum number of API requests per minute (token-bucket rate limiter).")
    @LangKey("config.prisonbreakmod.rateLimitPerMinute")
    @RangeInt(min = 1, max = 3600)
    public static int rateLimitPerMinute = 50;

    @Name("timeoutMs")
    @Comment("HTTP read/write timeout for each AI request, in milliseconds.")
    @LangKey("config.prisonbreakmod.timeoutMs")
    @RangeInt(min = 500, max = 60000)
    public static int timeoutMs = 8000;

    @Name("fallbackAfterMs")
    @Comment({
        "If the AI has not responded within this many milliseconds, the NPC",
        "falls back to its scripted behaviour instead of waiting longer."
    })
    @LangKey("config.prisonbreakmod.fallbackAfterMs")
    @RangeInt(min = 500, max = 120000)
    public static int fallbackAfterMs = 10000;

    // -------------------------------------------------------------------------
    // NPC cooldown settings
    // -------------------------------------------------------------------------

    @Name("guardCooldownTicks")
    @Comment("Minimum server ticks between two consecutive AI queries for a guard NPC.")
    @LangKey("config.prisonbreakmod.guardCooldownTicks")
    @RangeInt(min = 1, max = 72000)
    public static int guardCooldownTicks = 900;

    @Name("prisonerCooldownTicks")
    @Comment("Minimum server ticks between two consecutive AI queries for a prisoner NPC.")
    @LangKey("config.prisonbreakmod.prisonerCooldownTicks")
    @RangeInt(min = 1, max = 72000)
    public static int prisonerCooldownTicks = 1800;

    @Name("companionCooldownTicks")
    @Comment("Minimum server ticks between two consecutive AI queries for a companion NPC.")
    @LangKey("config.prisonbreakmod.companionCooldownTicks")
    @RangeInt(min = 1, max = 72000)
    public static int companionCooldownTicks = 400;

    // -------------------------------------------------------------------------
    // Token budget settings
    // -------------------------------------------------------------------------

    @Name("maxGuardTokens")
    @Comment("Maximum number of tokens the model may generate in a guard AI response.")
    @LangKey("config.prisonbreakmod.maxGuardTokens")
    @RangeInt(min = 50, max = 4096)
    public static int maxGuardTokens = 280;

    @Name("maxPrisonerTokens")
    @Comment("Maximum number of tokens the model may generate in a prisoner AI response.")
    @LangKey("config.prisonbreakmod.maxPrisonerTokens")
    @RangeInt(min = 50, max = 4096)
    public static int maxPrisonerTokens = 200;

    @Name("maxCompanionTokens")
    @Comment("Maximum number of tokens the model may generate in a companion AI response.")
    @LangKey("config.prisonbreakmod.maxCompanionTokens")
    @RangeInt(min = 50, max = 4096)
    public static int maxCompanionTokens = 400;

    @Name("maxDialogueTokens")
    @Comment("Maximum number of tokens used for dialogue-only AI calls.")
    @LangKey("config.prisonbreakmod.maxDialogueTokens")
    @RangeInt(min = 50, max = 4096)
    public static int maxDialogueTokens = 300;

    @Name("maxOfflineAnalysisTokens")
    @Comment("Maximum tokens for the longer, offline (reasoner) analysis queries.")
    @LangKey("config.prisonbreakmod.maxOfflineAnalysisTokens")
    @RangeInt(min = 50, max = 8192)
    public static int maxOfflineAnalysisTokens = 1500;

    // -------------------------------------------------------------------------
    // World / gameplay settings
    // -------------------------------------------------------------------------

    @Name("dayLengthMinutes")
    @Comment("Real-time minutes that correspond to one full Minecraft day/night cycle.")
    @LangKey("config.prisonbreakmod.dayLengthMinutes")
    @RangeInt(min = 1, max = 1440)
    public static int dayLengthMinutes = 20;

    @Name("temperatureMin")
    @Comment("Minimum ambient temperature (°C) inside the Arctic prison zone.")
    @LangKey("config.prisonbreakmod.temperatureMin")
    @RangeInt(min = -100, max = 0)
    public static int temperatureMin = 10;

    @Name("blizzardChancePerHour")
    @Comment({
        "Percentage chance (0-100) that a blizzard starts during any given",
        "real-time hour while the player is in the Arctic zone."
    })
    @LangKey("config.prisonbreakmod.blizzardChancePerHour")
    @RangeInt(min = 0, max = 100)
    public static int blizzardChancePerHour = 40;

    // -------------------------------------------------------------------------
    // Private constructor — this class must not be instantiated.
    // -------------------------------------------------------------------------

    private PrisonConfig() {
        throw new UnsupportedOperationException("PrisonConfig is a static config holder.");
    }

    // -------------------------------------------------------------------------
    // Config-changed event listener (inner class registered via @Mod.EventBusSubscriber)
    // -------------------------------------------------------------------------

    /**
     * Listens for Forge config-changed events so that values modified via the
     * in-game GUI are immediately written back to disk and re-read into the
     * static fields.
     */
    @Mod.EventBusSubscriber(modid = PrisonBreakMod.MODID)
    public static final class OnConfigChanged {

        private OnConfigChanged() {}

        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (PrisonBreakMod.MODID.equals(event.getModID())) {
                ConfigManager.sync(PrisonBreakMod.MODID, Type.INSTANCE);
                PrisonBreakMod.LOGGER.info("[PrisonConfig] Configuration reloaded.");
            }
        }
    }
}
