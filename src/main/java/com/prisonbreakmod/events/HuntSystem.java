package com.prisonbreakmod.events;

import com.prisonbreakmod.ai.SharedPrisonState;
import com.prisonbreakmod.entity.EntityGuard;
import com.prisonbreakmod.entity.EntityTrackingDog;
import com.prisonbreakmod.entity.guards.GuardFactory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages post-escape hunt: patrol groups, tracking dogs, helicopter coordination.
 * Activated when Alert Level 3 is set (D-Day escape detected).
 */
public class HuntSystem {

    private static HuntSystem instance;

    private boolean huntActive = false;
    private long huntStartTick = -1;
    private final List<EntityGuard> huntPatrols = new ArrayList<>();

    // Dog release after 1h game time
    private static final long DOG_RELEASE_DELAY = 72000L; // 1h game = 72000 ticks? (3600s * 20t/s)
    private boolean dogsReleased = false;

    public static HuntSystem getInstance() {
        if (instance == null) instance = new HuntSystem();
        return instance;
    }

    public void startHunt(World world) {
        if (huntActive) return;
        huntActive = true;
        huntStartTick = world.getTotalWorldTime();
        SharedPrisonState.getInstance().setHuntActive(true);
        SharedPrisonState.getInstance().addEvent("HUNT_STARTED");

        // Spawn 3 patrol groups of 2 guards each
        if (!world.playerEntities.isEmpty()) {
            EntityPlayer player = world.playerEntities.get(0);
            spawnPatrols(world, player);
        }
    }

    private void spawnPatrols(World world, EntityPlayer escaper) {
        double ox = 0, oz = 0; // prison origin
        // 3 directions
        double[][] dirs = {{1, 0}, {-0.5, 0.866}, {-0.5, -0.866}};
        for (int g = 0; g < 3; g++) {
            double spawnX = ox + dirs[g][0] * 100;
            double spawnZ = oz + dirs[g][1] * 100;
            for (int i = 0; i < 2; i++) {
                // Use guard IDs 4, 5, 6 etc. for pursuit
                int guardId = 4 + (g * 2) + i;
                EntityGuard guard = GuardFactory.createGuard(world, Math.min(guardId, 20));
                BlockPos spawnPos = world.getTopSolidOrLiquidBlock(
                        new BlockPos(spawnX + i * 3, 0, spawnZ + i * 3));
                guard.setPosition(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
                world.spawnEntity(guard);
                huntPatrols.add(guard);
            }
        }
    }

    public void tick(World world) {
        if (!huntActive || world.isRemote) return;
        long elapsed = world.getTotalWorldTime() - huntStartTick;

        // Release tracking dogs after 1h
        if (!dogsReleased && elapsed >= DOG_RELEASE_DELAY) {
            dogsReleased = true;
            releaseDogs(world);
        }

        // Update footprint tracking for patrols
        long[] lastPrint = SharedPrisonState.getInstance().getLatestFootprint();
        if (lastPrint != null) {
            for (EntityGuard guard : huntPatrols) {
                if (!guard.isDead) {
                    guard.getNavigator().tryMoveToXYZ(
                            lastPrint[1], guard.posY, lastPrint[2], 0.8);
                }
            }
        }
    }

    private void releaseDogs(World world) {
        double ox = 0, oz = 0;
        for (int i = 0; i < 2; i++) {
            EntityTrackingDog dog = new EntityTrackingDog(world);
            dog.setPosition(ox + i * 5, 64, oz);
            world.spawnEntity(dog);
        }
        SharedPrisonState.getInstance().addEvent("TRACKING_DOGS_RELEASED");
    }

    public boolean isHuntActive() { return huntActive; }

    public void loadState(boolean active, long startTick, boolean dogsOut) {
        this.huntActive = active;
        this.huntStartTick = startTick;
        this.dogsReleased = dogsOut;
    }
}
