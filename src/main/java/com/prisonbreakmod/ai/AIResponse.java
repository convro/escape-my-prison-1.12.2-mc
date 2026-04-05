package com.prisonbreakmod.ai;

/**
 * Immutable data class representing a structured response from the DeepSeek AI.
 *
 * Every field has a sensible default so callers never need to null-check. Use
 * {@link #fallback()} to obtain a safe no-op response when the AI is
 * unavailable or returns unparseable output.
 *
 * Builder pattern is provided for convenient construction in the parser.
 */
public final class AIResponse {

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    /** Primary action token (e.g. "idle", "patrol", "alert", "speak", "attack"). */
    private final String action;

    /** Relative movement vector — X component (east/west). */
    private final double movementX;

    /** Relative movement vector — Y component (up/down). */
    private final double movementY;

    /** Relative movement vector — Z component (north/south). */
    private final double movementZ;

    /** Spoken/chat dialogue text (empty string if the NPC is silent). */
    private final String dialogue;

    /**
     * Change to apply to the global {@link SharedPrisonState#alertLevel}.
     * Positive = raise alert, negative = lower alert.
     */
    private final int alertChange;

    /** Short memory snippet that should be stored in the NPC's {@link NPCMemory}. */
    private final String memory;

    /** Human-readable reasoning behind the chosen action (for debugging). */
    private final String reason;

    /**
     * Relation delta to apply between this NPC and the player.
     * Positive = friendlier, negative = more hostile.
     */
    private final int relChange;

    /**
     * Any piece of hidden information the NPC decides to reveal
     * (e.g. a location clue, an item hint).  Empty when nothing is revealed.
     */
    private final String revealedInfo;

    // -------------------------------------------------------------------------
    // Constructor (private — use Builder or fallback())
    // -------------------------------------------------------------------------

    private AIResponse(Builder b) {
        this.action       = b.action       != null ? b.action       : "idle";
        this.movementX    = b.movementX;
        this.movementY    = b.movementY;
        this.movementZ    = b.movementZ;
        this.dialogue     = b.dialogue     != null ? b.dialogue     : "";
        this.alertChange  = b.alertChange;
        this.memory       = b.memory       != null ? b.memory       : "";
        this.reason       = b.reason       != null ? b.reason       : "";
        this.relChange    = b.relChange;
        this.revealedInfo = b.revealedInfo != null ? b.revealedInfo : "";
    }

    // -------------------------------------------------------------------------
    // Static factory
    // -------------------------------------------------------------------------

    /**
     * Returns a safe, no-op {@code AIResponse} to be used as a fallback when
     * the AI is offline or parsing fails.
     *
     * @return fallback response with action "idle" and all other fields at defaults
     */
    public static AIResponse fallback() {
        return new Builder()
                .action("idle")
                .reason("AI unavailable — using fallback behaviour.")
                .build();
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public String getAction()       { return action; }
    public double getMovementX()    { return movementX; }
    public double getMovementY()    { return movementY; }
    public double getMovementZ()    { return movementZ; }
    public String getDialogue()     { return dialogue; }
    public int    getAlertChange()  { return alertChange; }
    public String getMemory()       { return memory; }
    public String getReason()       { return reason; }
    public int    getRelChange()    { return relChange; }
    public String getRevealedInfo() { return revealedInfo; }

    /** @return {@code true} if this NPC should say something aloud */
    public boolean hasSpeech() {
        return dialogue != null && !dialogue.isEmpty();
    }

    /** @return {@code true} if there is a non-empty memory snippet */
    public boolean hasMemory() {
        return memory != null && !memory.isEmpty();
    }

    /** @return {@code true} if the NPC is revealing hidden information */
    public boolean hasRevealedInfo() {
        return revealedInfo != null && !revealedInfo.isEmpty();
    }

    // -------------------------------------------------------------------------
    // toString (for debug logging)
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return "AIResponse{" +
               "action='" + action + '\'' +
               ", movement=(" + movementX + ',' + movementY + ',' + movementZ + ')' +
               ", dialogue='" + dialogue + '\'' +
               ", alertChange=" + alertChange +
               ", relChange=" + relChange +
               ", memory='" + memory + '\'' +
               ", revealedInfo='" + revealedInfo + '\'' +
               ", reason='" + reason + '\'' +
               '}';
    }

    // =========================================================================
    // Builder
    // =========================================================================

    /** Fluent builder for {@link AIResponse}. */
    public static final class Builder {

        private String action       = "idle";
        private double movementX    = 0.0;
        private double movementY    = 0.0;
        private double movementZ    = 0.0;
        private String dialogue     = "";
        private int    alertChange  = 0;
        private String memory       = "";
        private String reason       = "";
        private int    relChange    = 0;
        private String revealedInfo = "";

        public Builder() {}

        public Builder action(String val)       { this.action       = val; return this; }
        public Builder movementX(double val)    { this.movementX    = val; return this; }
        public Builder movementY(double val)    { this.movementY    = val; return this; }
        public Builder movementZ(double val)    { this.movementZ    = val; return this; }
        public Builder dialogue(String val)     { this.dialogue     = val; return this; }
        public Builder alertChange(int val)     { this.alertChange  = val; return this; }
        public Builder memory(String val)       { this.memory       = val; return this; }
        public Builder reason(String val)       { this.reason       = val; return this; }
        public Builder relChange(int val)       { this.relChange    = val; return this; }
        public Builder revealedInfo(String val) { this.revealedInfo = val; return this; }

        public AIResponse build() {
            return new AIResponse(this);
        }
    }
}
