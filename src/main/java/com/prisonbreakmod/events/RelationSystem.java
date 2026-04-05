package com.prisonbreakmod.events;

import com.prisonbreakmod.ai.SharedPrisonState;

/**
 * Thin wrapper around SharedPrisonState.relations with gameplay-relevant helper methods.
 */
public class RelationSystem {

    public static int getRelation(String npcId) {
        return SharedPrisonState.getInstance().getRelation(npcId);
    }

    public static void adjust(String npcId, int delta) {
        SharedPrisonState.getInstance().adjustRelation(npcId, delta);
    }

    public static void set(String npcId, int value) {
        SharedPrisonState.getInstance().setRelation(npcId, value);
    }

    /** Returns true if relation >= threshold. */
    public static boolean hasRelation(String npcId, int threshold) {
        return getRelation(npcId) >= threshold;
    }

    // Specific NPC IDs as constants for mission code
    public static final String MAREK = "companion_marek";
    public static final String ZBYSZEK = "companion_zbyszek";
    public static final String WISNIEWSK = "guard_1";
    public static final String GRUBA = "guard_2";
    public static final String MONIKA = "guard_14";
    public static final String HALINA = "guard_11";
    public static final String ZDZISLAW = "guard_15";
    public static final String POP_KORNIK = "prisoner_44";

    /** Check if player qualifies for archive work (relation with Wiśniewski > 35). */
    public static boolean canGetArchiveWork() {
        return hasRelation(WISNIEWSK, 35);
    }

    /** Check if Monika will leave hospital door open (relation > 80). */
    public static boolean monikaLeavesHospitalDoor() {
        return hasRelation(MONIKA, 80);
    }

    /** Check if Halina won't report (relation > 70). */
    public static boolean halinaLooksAway() {
        return hasRelation(HALINA, 70);
    }

    /** Check if Wiśniewski skips full inspection Friday (relation > 45). */
    public static boolean wisniewskiSkipsInspection() {
        return hasRelation(WISNIEWSK, 45);
    }
}
