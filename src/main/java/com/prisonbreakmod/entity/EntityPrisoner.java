package com.prisonbreakmod.entity;

import com.prisonbreakmod.ai.AIResponse;
import com.prisonbreakmod.ai.PromptBuilder;
import com.prisonbreakmod.ai.SharedPrisonState;
import com.prisonbreakmod.config.PrisonConfig;
import com.prisonbreakmod.util.TimeUtils;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class EntityPrisoner extends AbstractNPC {

    protected int prisonerId;
    protected String name;
    protected String cellId;
    protected String backstory;
    protected String currentWork = "idle";
    protected boolean isDonosiciel;
    /** Cached last dialogue line for rendering */
    protected String lastDialogue = "";
    protected String pendingRevealedInfo = "";
    protected boolean inPanicMode = false;

    public EntityPrisoner(World world, int prisonerId, String name, String cellId,
                          String backstory, boolean isDonosiciel) {
        super(world);
        this.prisonerId = prisonerId;
        this.name = name;
        this.cellId = cellId;
        this.backstory = backstory;
        this.isDonosiciel = isDonosiciel;
        this.npcId = "prisoner_" + prisonerId;
        this.maxCooldownTicks = PrisonConfig.prisonerCooldownTicks;
        this.cooldownTicks = (int)(Math.random() * this.maxCooldownTicks);
    }

    public EntityPrisoner(World world) {
        super(world);
        this.npcId = "prisoner_0";
        this.maxCooldownTicks = PrisonConfig.prisonerCooldownTicks;
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(6, new EntityAIWander(this, 0.6D));
        this.tasks.addTask(7, new EntityAILookIdle(this));
        this.tasks.addTask(5, new EntityAIWatchClosest(this, EntityPlayer.class, 15.0F));
    }

    @Override
    public String buildSystemPrompt() {
        return PromptBuilder.buildPrisonerSystemPrompt(
                getPersonalityDescription(), npcId);
    }

    @Override
    public String buildUserMessage() {
        SharedPrisonState state = SharedPrisonState.getInstance();
        int relation = state.getRelation(npcId);
        return "Relacja z graczem: " + relation + "/100. " +
               "Aktualna praca: " + currentWork + ". " +
               "Czas gry: " + TimeUtils.getGameTimeStr() + ". " +
               "Kontekst: " + state.toContextJSON() + ". " +
               "Co robisz teraz? Odpowiedz w JSON.";
    }

    @Override
    public void applyAIResponse(AIResponse response) {
        if (response == null) return;

        // Apply movement
        if (response.getMovementX() != 0 || response.getMovementZ() != 0) {
            double speed = 0.6;
            this.motionX += response.getMovementX() * speed;
            this.motionZ += response.getMovementZ() * speed;
        }

        // Relation change
        if (response.getRelChange() != 0) {
            SharedPrisonState.getInstance().adjustRelation(npcId, response.getRelChange());
        }

        // Store dialogue
        if (response.getDialogue() != null && !response.getDialogue().isEmpty()) {
            this.lastDialogue = response.getDialogue();
        }

        // Store revealed info
        if (response.getRevealedInfo() != null && !response.getRevealedInfo().isEmpty()) {
            this.pendingRevealedInfo = response.getRevealedInfo();
            memory.addMemory("Ujawniono: " + response.getRevealedInfo());
        }

        // Panic mode
        if ("panic".equals(response.getAction())) {
            this.inPanicMode = true;
        }

        // Memory
        if (response.getMemory() != null && !response.getMemory().isEmpty()) {
            memory.addMemory(response.getMemory());
        }

        // Donosiciel logic
        if (isDonosiciel && "speak".equals(response.getAction())) {
            // 30% chance to report player to nearest guard if nearby
            if (Math.random() < 0.3 && SharedPrisonState.getInstance().getAlertLevel() == 0) {
                SharedPrisonState.getInstance().addEvent("DONOS_BY:" + npcId);
            }
        }

        lastResponse = response;
    }

    @Override
    public String getPersonalityDescription() {
        return "Jesteś więźniem o imieniu " + name + " (ID: " + prisonerId + "). " +
               "Cela: " + cellId + ". Historia: " + backstory + ". " +
               (isDonosiciel ? "WAŻNE: Jesteś tajnym informatorem strażników." : "") +
               " Praca: " + currentWork + ".";
    }

    @Override
    public String getBehaviorRules() {
        return "Zachowujesz się jak więzień w radzieckim łagrze. " +
               "Rozmawiasz ostrożnie, szeptem przy strażnikach. " +
               "Format odpowiedzi: JSON {\"action\":\"idle|work|walk|whisper|give_item|panic|fight\"," +
               "\"dialogue\":\"...\",\"relChange\":0,\"revealedInfo\":\"...\",\"reason\":\"...\"}";
    }

    @Override
    public String getName() {
        return name != null ? name : "Więzień";
    }

    public String getLastDialogue() { return lastDialogue; }
    public void clearDialogue() { lastDialogue = ""; }
    public String getPendingRevealedInfo() { return pendingRevealedInfo; }
    public void clearRevealedInfo() { pendingRevealedInfo = ""; }
    public int getPrisonerId() { return prisonerId; }
    public boolean isInPanicMode() { return inPanicMode; }
    public void setInPanicMode(boolean v) { inPanicMode = v; }
    public void setCurrentWork(String work) { this.currentWork = work; }
    public String getCurrentWork() { return currentWork; }
}
