package com.prisonbreakmod.entity;

import com.prisonbreakmod.PrisonBreakMod;
import com.prisonbreakmod.ai.AIResponse;
import com.prisonbreakmod.ai.AsyncAIHandler;
import com.prisonbreakmod.ai.NPCMemory;
import net.minecraft.entity.EntityCreature;
import net.minecraft.world.World;

/**
 * Abstract base class for all AI-driven NPCs in Prison Break: Gulag.
 *
 * Manages the per-NPC AI query cooldown and integrates with
 * {@link AsyncAIHandler} for asynchronous DeepSeek queries.
 *
 * Subclasses must implement the four abstract methods to define
 * personality, prompts, and how AI responses are applied.
 */
public abstract class AbstractNPC extends EntityCreature {

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    /** Per-NPC episodic memory passed into AI prompts. */
    protected NPCMemory memory;

    /** Remaining ticks until the next AI query is allowed. */
    protected int cooldownTicks;

    /**
     * Maximum cooldown ticks between AI queries for this NPC type.
     * Subclasses set this in their constructor.
     */
    protected int maxCooldownTicks = 900;

    /** Unique string identifier used throughout the AI and state systems. */
    protected String npcId = "npc_unknown";

    /** Guard against double-submitting AI queries while a future is pending. */
    protected boolean isProcessingAI = false;

    /** Most recently received AI response (may be null before first query). */
    protected AIResponse lastResponse;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    protected AbstractNPC(World world) {
        super(world);
        // Memory is initialized after npcId is set by subclass constructor
    }

    // -------------------------------------------------------------------------
    // Abstract methods
    // -------------------------------------------------------------------------

    /**
     * Returns the system-role prompt for this NPC's AI persona.
     *
     * @return non-null system prompt string
     */
    public abstract String buildSystemPrompt();

    /**
     * Returns the user-role message describing the current observation.
     *
     * @return non-null user message string
     */
    public abstract String buildUserMessage();

    /**
     * Applies a resolved {@link AIResponse} to this NPC's in-world state.
     * Must be called on the server thread.
     *
     * @param response non-null AI response (may be a fallback)
     */
    public abstract void applyAIResponse(AIResponse response);

    /**
     * Returns a short personality description used in prompts.
     *
     * @return non-null personality string
     */
    public abstract String getPersonalityDescription();

    /**
     * Returns a short list of behaviour rules used in prompts.
     *
     * @return non-null rules string
     */
    public abstract String getBehaviorRules();

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    /**
     * Called every server tick. Decrements the cooldown and submits an AI
     * query via {@link AsyncAIHandler} when the cooldown expires and no query
     * is currently in-flight.
     */
    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();

        if (world.isRemote) {
            return;
        }

        if (cooldownTicks > 0) {
            cooldownTicks--;
        }

        if (cooldownTicks <= 0 && !isProcessingAI) {
            AsyncAIHandler handler = PrisonBreakMod.getInstance().getAIHandler();
            if (handler != null) {
                isProcessingAI = true;
                cooldownTicks = maxCooldownTicks;
                handler.submitQuery(this, buildUserMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // Helper methods
    // -------------------------------------------------------------------------

    /**
     * Returns the NPC's full episodic memory as a formatted string.
     *
     * @return memory string, or empty string if memory is blank
     */
    public String getMemoryStr() {
        if (memory == null) return "";
        return memory.getMemoryStr();
    }

    /**
     * Returns the current position formatted as "x,y,z" (block coordinates).
     *
     * @return position string
     */
    public String getPositionStr() {
        return (int) posX + "," + (int) posY + "," + (int) posZ;
    }

    /**
     * Removes potentially dangerous characters from player-provided input and
     * truncates to 256 characters to prevent prompt injection.
     *
     * Removed characters: {@code "  '  {  }  \}
     *
     * @param input raw input string (may be null)
     * @return sanitized string, never null
     */
    public static String sanitizeInput(String input) {
        if (input == null) return "";
        String sanitized = input
                .replace("\"", "")
                .replace("'", "")
                .replace("{", "")
                .replace("}", "")
                .replace("\\", "");
        if (sanitized.length() > 256) {
            sanitized = sanitized.substring(0, 256);
        }
        return sanitized;
    }

    // -------------------------------------------------------------------------
    // Accessors (required by AsyncAIHandler)
    // -------------------------------------------------------------------------

    /** @return this NPC's episodic memory store */
    public NPCMemory getNPCMemory() {
        return memory;
    }

    /**
     * Returns the cooldown tick count used by {@link AsyncAIHandler} for token
     * budget resolution.
     *
     * @return max cooldown in ticks
     */
    public int getAICooldownTicks() {
        return maxCooldownTicks;
    }

    /** @return this NPC's unique string identifier */
    public String getNpcId() {
        return npcId;
    }

    /**
     * Called by {@link AsyncAIHandler} when a query has been dispatched.
     * Records the world time and clears the processing guard.
     */
    public void markAIQueried() {
        isProcessingAI = false;
    }

    /**
     * Initializes the NPC memory with the assigned npcId.
     * Must be called once from the subclass constructor after {@code npcId} is set.
     */
    protected void initMemory() {
        this.memory = new NPCMemory(npcId);
    }

    /**
     * Returns {@code true} if this NPC is currently waiting for an AI response.
     *
     * @return processing flag
     */
    public boolean isProcessingAI() {
        return isProcessingAI;
    }

    /**
     * Returns the most recently applied AI response, or {@code null} if none
     * has been received yet.
     *
     * @return last response or null
     */
    public AIResponse getLastResponse() {
        return lastResponse;
    }
}
