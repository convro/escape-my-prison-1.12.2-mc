package com.prisonbreakmod.events;

import com.prisonbreakmod.PrisonBreakMod;
import com.prisonbreakmod.ai.AsyncAIHandler;
import com.prisonbreakmod.ai.SharedPrisonState;
import com.prisonbreakmod.util.FootprintTracker;
import com.prisonbreakmod.world.WeatherSystem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber(modid = PrisonBreakMod.MODID)
public class PrisonEventHandler {

    private static long lastSaveTick = 0;
    private static final long SAVE_INTERVAL = 600; // 30s

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        net.minecraft.server.MinecraftServer server =
                net.minecraftforge.fml.common.FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null) return;

        net.minecraft.world.World world = server.getEntityWorld();

        // Process AI results
        AsyncAIHandler.getInstance().processResults();

        // Weather & temperature
        WeatherSystem.getInstance().tick(world);

        // Random events
        RandomEventManager.getInstance().tick(world);

        // Hunt system
        HuntSystem.getInstance().tick(world);

        // Footprints for players
        for (EntityPlayer player : world.playerEntities) {
            FootprintTracker.tick(world, player);
        }

        // Dog reactivation check
        SharedPrisonState.getInstance().checkDogReactivation(world.getTotalWorldTime());

        // Sleeping herb check
        SharedPrisonState.getInstance().checkSleepingHerb(world.getTotalWorldTime());

        // Periodic save
        if (world.getTotalWorldTime() - lastSaveTick >= SAVE_INTERVAL) {
            lastSaveTick = world.getTotalWorldTime();
            com.prisonbreakmod.data.PrisonSaveData.getInstance(world).markDirty();
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.world.isRemote) return;

        EntityPlayer player = event.player;

        // Sprint fatigue
        SprintFatigueTracker.tick(player);

        // Footprint tracking for hunt
        if (SharedPrisonState.getInstance().isHuntActive()) {
            SharedPrisonState.getInstance().addFootprint(
                    player.world.getTotalWorldTime(),
                    player.posX, player.posZ);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        // HUD update handled by renderer
    }
}
