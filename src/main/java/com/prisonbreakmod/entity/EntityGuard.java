package com.prisonbreakmod.entity;

import com.prisonbreakmod.PrisonBreakMod;
import com.prisonbreakmod.ai.AIResponse;
import com.prisonbreakmod.ai.PromptBuilder;
import com.prisonbreakmod.ai.SharedPrisonState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import java.util.List;

/**
 * Represents a prison guard NPC driven by the DeepSeek AI.
 *
 * Each guard has a unique personality, schedule and detection range.
 * The AI receives a structured system prompt built by {@link PromptBuilder}
 * and returns a JSON action object which is parsed and applied here.
 *
 * AI response format:
 * <pre>
 * {"action":"patrol|stop|investigate|alert|speak|attack|chase",
 *  "movement":{"x":0,"y":0,"z":0},
 *  "dialogue":"...",
 *  "alertChange":0,
 *  "memory":"...",
 *  "reason":"..."}
 * </pre>
 */
public class EntityGuard extends AbstractNPC {

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    /** Numeric guard identifier (1–20). */
    protected final int guardId;

    /** Display name shown above the NPC's head and in chat. */
    protected final String name;

    /** Role/post description (e.g. "Komendant", "Strażnik stołówki"). */
    protected final String role;

    /** Weapon(s) carried by this guard (e.g. "pistolTT", "baton+tazer"). */
    protected final String weaponType;

    /** Schedule description used in the system prompt. */
    protected final String schedule;

    /** Full personality description injected into the AI system prompt. */
    protected final String personalityDesc;

    /** Detection radius in blocks. Default is 30. */
    protected final double detectionRange;

    /** Whether the guard is currently on active duty. */
    protected boolean isOnDuty = true;

    /**
     * Cooldown in ticks between alert-state checks so we don't spam reactions.
     * Decrements each tick, resets when an alert action is taken.
     */
    protected int alertCooldown = 0;

    /**
     * Pending dialogue line set by the last AI response.
     * A separate renderer/handler picks this up and displays it.
     */
    protected String pendingDialogue = "";

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Creates a new guard entity.
     *
     * @param world           the server world
     * @param guardId         numeric identifier (1–20)
     * @param name            display name
     * @param role            post/role description
     * @param weaponType      weapon string
     * @param personalityDesc full personality description from the spec
     * @param schedule        schedule description
     * @param detectionRange  detection radius in blocks
     */
    public EntityGuard(World world,
                       int guardId,
                       String name,
                       String role,
                       String weaponType,
                       String personalityDesc,
                       String schedule,
                       double detectionRange) {
        super(world);
        this.guardId        = guardId;
        this.name           = name;
        this.role           = role;
        this.weaponType     = weaponType;
        this.personalityDesc = personalityDesc;
        this.schedule       = schedule;
        this.detectionRange = detectionRange;

        this.npcId          = "guard_" + String.format("%02d", guardId);
        this.maxCooldownTicks = 900;
        this.cooldownTicks  = guardId * 45; // stagger initial queries

        initMemory();
    }

    // -------------------------------------------------------------------------
    // Forge AI initialisation
    // -------------------------------------------------------------------------

    @Override
    protected void initEntityAI() {
        // Priority 5: watch closest player within detection range
        this.tasks.addTask(5, new EntityAIWatchClosest(this, EntityPlayer.class,
                (float) detectionRange));

        // Priority 6: wander around the patrol area
        this.tasks.addTask(6, new EntityAIWander(this, 0.8D));

        // Priority 7: look idle when nothing else to do
        this.tasks.addTask(7, new EntityAILookIdle(this));
    }

    // -------------------------------------------------------------------------
    // AbstractNPC implementations
    // -------------------------------------------------------------------------

    @Override
    public String buildSystemPrompt() {
        return PromptBuilder.buildGuardSystemPrompt(
                guardId,
                name,
                role,
                weaponType,
                personalityDesc,
                schedule,
                detectionRange);
    }

    @Override
    public String buildUserMessage() {
        SharedPrisonState state = SharedPrisonState.getInstance();
        int alertLevel = state.getAlertLevel();
        String recentEvents = state.getRecentEvents(5);
        String pos = getPositionStr();

        // Collect nearby players
        @SuppressWarnings("unchecked")
        List<EntityPlayer> nearbyPlayers = world.getEntitiesWithinAABB(
                EntityPlayer.class,
                getEntityBoundingBox().grow(detectionRange));

        StringBuilder sb = new StringBuilder();
        sb.append("SYTUACJA:\n");
        sb.append("Moja pozycja: ").append(pos).append("\n");
        sb.append("Poziom alertu: ").append(alertLevel).append("/3\n");
        sb.append("Jestem na służbie: ").append(isOnDuty ? "TAK" : "NIE").append("\n");

        if (!nearbyPlayers.isEmpty()) {
            sb.append("Pobliski gracz: ");
            EntityPlayer closest = nearbyPlayers.get(0);
            sb.append(sanitizeInput(closest.getName())).append(" @ ");
            sb.append((int) closest.posX).append(",")
              .append((int) closest.posY).append(",")
              .append((int) closest.posZ).append("\n");
        } else {
            sb.append("Brak graczy w zasięgu.\n");
        }

        if (recentEvents != null && !recentEvents.isEmpty()) {
            sb.append("Ostatnie zdarzenia:\n").append(recentEvents).append("\n");
        }

        String mem = getMemoryStr();
        if (!mem.isEmpty()) {
            sb.append("Moja pamięć:\n").append(mem).append("\n");
        }

        sb.append("\nCo robisz? Odpowiedz TYLKO JSON.");
        return sb.toString();
    }

    @Override
    public void applyAIResponse(AIResponse response) {
        if (response == null) return;
        this.lastResponse = response;

        String action = response.getAction();

        // Apply movement delta
        double dx = response.getMovementX();
        double dy = response.getMovementY();
        double dz = response.getMovementZ();
        if (dx != 0 || dy != 0 || dz != 0) {
            setPosition(posX + dx, posY + dy, posZ + dz);
        }

        // Alert changes — AsyncAIHandler also handles this, so only log locally
        int alertChange = response.getAlertChange();
        if (alertChange > 0) {
            SharedPrisonState.getInstance().raiseAlert(alertChange);
            alertCooldown = 100;
            PrisonBreakMod.LOGGER.debug("[{}] raised alert by {}", npcId, alertChange);
        } else if (alertChange < 0) {
            SharedPrisonState.getInstance().lowerAlert(-alertChange);
        }

        // Memory snippet
        if (response.hasMemory() && memory != null) {
            memory.addMemory(response.getMemory());
        }

        // Dialogue
        if (response.hasSpeech()) {
            pendingDialogue = response.getDialogue();
            PrisonBreakMod.LOGGER.debug("[{}] says: {}", npcId, pendingDialogue);
        }

        // Broadcast specific actions to shared state
        switch (action) {
            case "alert":
                SharedPrisonState.getInstance().addEvent("GUARD_ALERT:" + npcId + "@" + getPositionStr());
                break;
            case "attack":
                SharedPrisonState.getInstance().addEvent("GUARD_ATTACK:" + npcId);
                break;
            case "investigate":
                SharedPrisonState.getInstance().addEvent("GUARD_INVESTIGATE:" + npcId);
                break;
            default:
                break;
        }
    }

    @Override
    public String getPersonalityDescription() {
        return personalityDesc;
    }

    @Override
    public String getBehaviorRules() {
        return "Patroluj teren. Reaguj na graczy stosownie do alertu. Komunikuj przez radio.";
    }

    // -------------------------------------------------------------------------
    // Entity overrides
    // -------------------------------------------------------------------------

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean hasCustomName() {
        return true;
    }

    // -------------------------------------------------------------------------
    // Tick override (alert cooldown management)
    // -------------------------------------------------------------------------

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (alertCooldown > 0) {
            alertCooldown--;
        }
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public int getGuardId()         { return guardId; }
    public String getGuardName()    { return name; }
    public String getRole()         { return role; }
    public String getWeaponType()   { return weaponType; }
    public double getDetectionRange() { return detectionRange; }
    public boolean isOnDuty()       { return isOnDuty; }
    public void setOnDuty(boolean v) { this.isOnDuty = v; }
    public String getPendingDialogue() { return pendingDialogue; }
    public void clearPendingDialogue() { this.pendingDialogue = ""; }
}
