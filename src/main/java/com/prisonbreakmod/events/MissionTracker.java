package com.prisonbreakmod.events;

import com.prisonbreakmod.ai.SharedPrisonState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import java.util.HashMap;
import java.util.Map;

/**
 * Tracks mission phases and individual mission states.
 */
public class MissionTracker {

    private static MissionTracker instance;

    public enum MissionState { LOCKED, ACTIVE, DONE, FAILED }

    private final Map<String, MissionState> missions = new HashMap<>();
    private int currentPhase = 0;

    public static MissionTracker getInstance() {
        if (instance == null) instance = new MissionTracker();
        return instance;
    }

    private MissionTracker() {
        // Initialize all missions
        initMissions();
    }

    private void initMissions() {
        // Phase 0
        setMission("0.1_first_contact_zbyszek", MissionState.ACTIVE);
        setMission("0.2_marek_in_canteen", MissionState.ACTIVE);
        setMission("0.3_prison_currency", MissionState.ACTIVE);
        setMission("0.4_first_errand", MissionState.ACTIVE);

        // Phase 1-6 locked initially
        for (int phase = 1; phase <= 6; phase++) {
            lockPhase(phase);
        }
    }

    private void lockPhase(int phase) {
        String[] missionIds = getPhase(phase);
        for (String id : missionIds) {
            setMission(id, MissionState.LOCKED);
        }
    }

    private String[] getPhase(int phase) {
        switch (phase) {
            case 1: return new String[]{
                    "1.1_night_schedule", "1.2_archive_access",
                    "1.3_captain_weakness", "1.4_sewers", "1.5_pop_kornik_favor",
                    "1.6_mithril_dust"};
            case 2: return new String[]{
                    "2.1_supplies_for_zbyszek", "2.2_herb_to_zdzislaw",
                    "2.3_picklock_mk1", "2.4_magazine_a",
                    "2.5_emp_grenades", "2.6_survival_gear"};
            case 3: return new String[]{
                    "3.1_disable_camera_b", "3.2_boilerroom_sabotage",
                    "3.3_sleep_zdzislaw", "3.4_monika_relation",
                    "3.5_dog_neutralize", "3.6_wisniewksi_optional"};
            case 4: return new String[]{"4a_directors_office", "4b_mithril_path", "4_master_key"};
            case 5: return new String[]{"5_dress_rehearsal", "5_identify_issues", "5_signals"};
            case 6: return new String[]{"6_d_day"};
            default: return new String[]{};
        }
    }

    public void setMission(String id, MissionState state) {
        missions.put(id, state);
    }

    public MissionState getMission(String id) {
        return missions.getOrDefault(id, MissionState.LOCKED);
    }

    public boolean isMissionDone(String id) {
        return getMission(id) == MissionState.DONE;
    }

    public boolean isMissionActive(String id) {
        return getMission(id) == MissionState.ACTIVE;
    }

    public void completeMission(String id) {
        setMission(id, MissionState.DONE);
        SharedPrisonState.getInstance().addEvent("MISSION_DONE:" + id);
        checkPhaseTransition();
    }

    public int getCurrentPhase() { return currentPhase; }

    private void checkPhaseTransition() {
        // Phase 0 -> 1
        if (currentPhase == 0) {
            if (isMissionDone("0.1_first_contact_zbyszek") && isMissionDone("0.2_marek_in_canteen")
                    && isMissionDone("0.3_prison_currency") && isMissionDone("0.4_first_errand")
                    && RelationSystem.hasRelation(RelationSystem.MAREK, 40)
                    && RelationSystem.hasRelation(RelationSystem.ZBYSZEK, 55)) {
                unlockPhase(1);
            }
        }
        // Phase 1 -> 2
        if (currentPhase == 1) {
            if (isMissionDone("1.1_night_schedule") && isMissionDone("1.4_sewers")
                    && RelationSystem.hasRelation("prisoner_44", 70)
                    && RelationSystem.hasRelation(RelationSystem.MAREK, 60)) {
                unlockPhase(2);
            }
        }
        // Phase 2 -> 3: require all gear
        if (currentPhase == 2 && isMissionDone("2.6_survival_gear")) {
            unlockPhase(3);
        }
        // etc.
    }

    private void unlockPhase(int phase) {
        currentPhase = phase;
        String[] ids = getPhase(phase);
        for (String id : ids) {
            setMission(id, MissionState.ACTIVE);
        }
        SharedPrisonState.getInstance().addEvent("PHASE_UNLOCKED:" + phase);
    }

    // NBT persistence
    public void writeToNBT(NBTTagCompound tag) {
        NBTTagCompound missionTag = new NBTTagCompound();
        for (Map.Entry<String, MissionState> e : missions.entrySet()) {
            missionTag.setInteger(e.getKey(), e.getValue().ordinal());
        }
        tag.setTag("missions", missionTag);
        tag.setInteger("phase", currentPhase);
    }

    public void readFromNBT(NBTTagCompound tag) {
        currentPhase = tag.getInteger("phase");
        NBTTagCompound missionTag = tag.getCompoundTag("missions");
        for (String key : missionTag.getKeySet()) {
            int ord = missionTag.getInteger(key);
            if (ord >= 0 && ord < MissionState.values().length) {
                missions.put(key, MissionState.values()[ord]);
            }
        }
    }
}
