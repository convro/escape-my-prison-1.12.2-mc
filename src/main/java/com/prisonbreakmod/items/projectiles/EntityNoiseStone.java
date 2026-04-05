package com.prisonbreakmod.items.projectiles;

import com.prisonbreakmod.ai.SharedPrisonState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

/**
 * A thrown stone that makes a loud noise on landing, alerting nearby guards.
 */
public class EntityNoiseStone extends EntityThrowable {

    public EntityNoiseStone(World world) {
        super(world);
    }

    public EntityNoiseStone(World world, EntityLivingBase thrower) {
        super(world, thrower);
    }

    public EntityNoiseStone(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    // -------------------------------------------------------------------------
    // Impact
    // -------------------------------------------------------------------------

    @Override
    protected void onImpact(RayTraceResult result) {
        if (!world.isRemote) {
            BlockPos pos = new BlockPos(this.posX, this.posY, this.posZ);

            // Play NOTE_BLOCK (harp) sound at maximum volume to attract guards
            world.playSound(null, this.posX, this.posY, this.posZ,
                    SoundEvents.BLOCK_NOTE_HARP, SoundCategory.BLOCKS,
                    3.0f, 1.0f);

            // Also play extra clatter
            world.playSound(null, this.posX, this.posY, this.posZ,
                    SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.NEUTRAL,
                    2.0f, 0.5f);

            // Register noise event so guard AI can investigate
            String eventKey = "NOISE_AT:" + pos.getX() + "," + pos.getY() + "," + pos.getZ();
            SharedPrisonState.getInstance().addEvent(eventKey);

            this.setDead();
        }
    }

    @Override
    protected float getGravityVelocity() {
        return 0.03f;
    }
}
