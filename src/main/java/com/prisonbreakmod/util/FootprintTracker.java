package com.prisonbreakmod.util;

import com.prisonbreakmod.ai.SharedPrisonState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

/** Records player footprints every 10 seconds for the hunt tracking system. */
public class FootprintTracker {

    private static final int RECORD_INTERVAL = 200; // every 10s (200 ticks)

    public static void tick(World world, EntityPlayer player) {
        if (world.isRemote) return;
        if (world.getTotalWorldTime() % RECORD_INTERVAL != 0) return;

        // Only track if hunt is active or in buffer zone
        double dist = Math.sqrt(player.posX * player.posX + player.posZ * player.posZ);
        if (dist > 100) { // outside prison
            SharedPrisonState state = SharedPrisonState.getInstance();
            state.addFootprint(world.getTotalWorldTime(), player.posX, player.posZ);
            // Clean old footprints (older than 5 min = 6000 ticks)
            state.cleanOldFootprints(world.getTotalWorldTime());
        }
    }
}
