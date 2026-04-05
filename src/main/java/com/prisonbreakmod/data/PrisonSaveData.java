package com.prisonbreakmod.data;

import com.prisonbreakmod.ai.SharedPrisonState;
import com.prisonbreakmod.events.HuntSystem;
import com.prisonbreakmod.events.MissionTracker;
import com.prisonbreakmod.world.WeatherSystem;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;

import java.util.Map;

/**
 * Persistent save data for the Prison Break mod.
 * Attached to world save, written every 30 seconds.
 */
public class PrisonSaveData extends WorldSavedData {

    private static final String ID = "prison_break_data";

    public PrisonSaveData() { super(ID); }
    public PrisonSaveData(String name) { super(name); }

    public static PrisonSaveData getInstance(World world) {
        PrisonSaveData data = (PrisonSaveData) world.getPerWorldStorage()
                .getOrLoadData(PrisonSaveData.class, ID);
        if (data == null) {
            data = new PrisonSaveData();
            world.getPerWorldStorage().setData(ID, data);
        }
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        SharedPrisonState state = SharedPrisonState.getInstance();

        // Alert system
        state.setAlertLevel(compound.getInteger("alertLevel"));
        state.setAlertReason(compound.getString("alertReason"));

        // Relations
        NBTTagCompound relations = compound.getCompoundTag("relations");
        for (String key : relations.getKeySet()) {
            state.setRelation(key, relations.getInteger(key));
        }

        // Events (last 20)
        NBTTagList events = compound.getTagList("events", 8);
        for (int i = 0; i < events.tagCount(); i++) {
            state.addEvent(events.getStringTagAt(i));
        }

        // Hunt system
        boolean huntActive = compound.getBoolean("huntActive");
        long huntStart = compound.getLong("huntStart");
        boolean dogsOut = compound.getBoolean("dogsOut");
        HuntSystem.getInstance().loadState(huntActive, huntStart, dogsOut);

        // Weather
        boolean blizzard = compound.getBoolean("blizzard");
        long blizzardEnd = compound.getLong("blizzardEnd");
        WeatherSystem.getInstance().loadState(blizzard, blizzardEnd);

        // Missions
        MissionTracker.getInstance().readFromNBT(compound.getCompoundTag("missions"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        SharedPrisonState state = SharedPrisonState.getInstance();

        compound.setInteger("alertLevel", state.getAlertLevel());
        compound.setString("alertReason", state.getAlertReason());

        // Relations
        NBTTagCompound relations = new NBTTagCompound();
        for (Map.Entry<String, Integer> e : state.getRelationsSnapshot().entrySet()) {
            relations.setInteger(e.getKey(), e.getValue());
        }
        compound.setTag("relations", relations);

        // Events
        NBTTagList events = new NBTTagList();
        for (String ev : state.getEventsSnapshot()) {
            events.appendTag(new net.minecraft.nbt.NBTTagString(ev));
        }
        compound.setTag("events", events);

        // Hunt
        compound.setBoolean("huntActive", HuntSystem.getInstance().isHuntActive());
        compound.setBoolean("blizzard", WeatherSystem.getInstance().isBlizzard());

        // Missions
        NBTTagCompound missionTag = new NBTTagCompound();
        MissionTracker.getInstance().writeToNBT(missionTag);
        compound.setTag("missions", missionTag);

        return compound;
    }
}
