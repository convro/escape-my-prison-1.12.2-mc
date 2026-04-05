package com.prisonbreakmod.events;

import com.prisonbreakmod.ai.SharedPrisonState;
import net.minecraft.world.World;

/**
 * Alert system — 4 levels (0=Normal, 1=Yellow, 2=Orange, 3=Red).
 * Static helper that delegates to SharedPrisonState.
 */
public class AlertSystem {

    public static void raise(World world, int levels, String reason) {
        SharedPrisonState state = SharedPrisonState.getInstance();
        state.raiseAlert(levels, reason);
        state.addEvent("ALERT_RAISED:" + levels + ":" + reason);
        if (world != null && !world.isRemote) {
            notifyGuards(world, state.getAlertLevel());
        }
    }

    public static void raise(int levels, String reason) {
        raise(null, levels, reason);
    }

    public static void lower(World world, int levels) {
        SharedPrisonState.getInstance().lowerAlert(levels);
    }

    public static int getLevel() {
        return SharedPrisonState.getInstance().getAlertLevel();
    }

    private static void notifyGuards(World world, int alertLevel) {
        // All guard NPCs pick up alert change on next AI query via SharedPrisonState.toContextJSON()
        // Additional immediate effect: at alert 2+, lock down movement
        if (alertLevel >= 2) {
            SharedPrisonState.getInstance().addEvent("LOCKDOWN_ACTIVE");
        }
        if (alertLevel >= 3) {
            // Spawn helicopter if not active
            if (!SharedPrisonState.getInstance().isHelicopterActive()) {
                // Find a player to center it on
                if (!world.playerEntities.isEmpty()) {
                    net.minecraft.entity.player.EntityPlayer p = world.playerEntities.get(0);
                    com.prisonbreakmod.entity.EntityHelicopter heli =
                            new com.prisonbreakmod.entity.EntityHelicopter(world,
                                    p.posX, p.posY, p.posZ);
                    world.spawnEntity(heli);
                }
            }
        }
    }
}
