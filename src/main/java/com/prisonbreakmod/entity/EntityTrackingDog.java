package com.prisonbreakmod.entity;

import com.prisonbreakmod.ai.SharedPrisonState;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIFollowOwner;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.List;

/**
 * Tracking dog — follows scent trail (footprints) from SharedPrisonState.
 * Disabled when SharedPrisonState.isDogNeutralized() == true.
 */
public class EntityTrackingDog extends EntityCreature {

    private static final double SCENT_RADIUS_AIR = 8.0;
    private static final double SCENT_RADIUS_WALL = 3.0;
    private static final int TRACK_INTERVAL = 20;

    private int trackTimer = 0;
    private double lastKnownX = 0, lastKnownZ = 0;
    private boolean hasScent = false;

    public EntityTrackingDog(World world) {
        super(world);
        this.setSize(0.8F, 0.85F);
        this.experienceValue = 0;
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(6, new EntityAIWander(this, 0.8D));
        this.tasks.addTask(7, new EntityAILookIdle(this));
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (world.isRemote) return;
        if (SharedPrisonState.getInstance().isDogNeutralized()) {
            hasScent = false;
            return;
        }
        trackTimer--;
        if (trackTimer <= 0) {
            trackTimer = TRACK_INTERVAL;
            trackPlayer();
        }
    }

    private void trackPlayer() {
        // Check footprints
        long[] latestPrint = SharedPrisonState.getInstance().getLatestFootprint();
        if (latestPrint != null) {
            double fpX = latestPrint[1];
            double fpZ = latestPrint[2];
            double dx = fpX - posX;
            double dz = fpZ - posZ;
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist < 100) {
                hasScent = true;
                lastKnownX = fpX;
                lastKnownZ = fpZ;
                // Move toward footprint
                getNavigator().tryMoveToXYZ(fpX, posY, fpZ, 1.2);
            }
        }

        // Direct air scent check
        AxisAlignedBB scentBox = new AxisAlignedBB(
                posX - SCENT_RADIUS_AIR, posY - 2, posZ - SCENT_RADIUS_AIR,
                posX + SCENT_RADIUS_AIR, posY + 4, posZ + SCENT_RADIUS_AIR);
        List<EntityPlayer> nearby = world.getEntitiesWithinAABB(EntityPlayer.class, scentBox);
        if (!nearby.isEmpty()) {
            EntityPlayer target = nearby.get(0);
            SharedPrisonState.getInstance().addEvent(
                    "DOG_FOUND:" + target.getName() +
                    "@" + (int) target.posX + "," + (int) target.posY + "," + (int) target.posZ);
            // Alert
            SharedPrisonState.getInstance().raiseAlert(2, "Pies Burek wykrył więźnia");
            getNavigator().tryMoveToEntityLiving(target, 1.4);
        }
    }

    public boolean hasScent() { return hasScent; }
}
