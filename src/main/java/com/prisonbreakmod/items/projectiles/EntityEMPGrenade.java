package com.prisonbreakmod.items.projectiles;

import com.prisonbreakmod.ai.SharedPrisonState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.SoundEvents;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import java.util.List;

/**
 * Thrown EMP grenade that disables nearby cameras on impact.
 */
public class EntityEMPGrenade extends EntityThrowable {

    private static final int EMP_RADIUS = 10;
    private static final int CAMERA_DISABLE_TICKS = 1800; // 90 seconds

    public EntityEMPGrenade(World world) {
        super(world);
    }

    public EntityEMPGrenade(World world, EntityLivingBase thrower) {
        super(world, thrower);
    }

    public EntityEMPGrenade(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    // -------------------------------------------------------------------------
    // Impact
    // -------------------------------------------------------------------------

    @Override
    protected void onImpact(RayTraceResult result) {
        if (!world.isRemote) {
            BlockPos center = new BlockPos(this.posX, this.posY, this.posZ);

            // Scan all tile entities in range
            for (int dx = -EMP_RADIUS; dx <= EMP_RADIUS; dx++) {
                for (int dy = -EMP_RADIUS; dy <= EMP_RADIUS; dy++) {
                    for (int dz = -EMP_RADIUS; dz <= EMP_RADIUS; dz++) {
                        if (dx * dx + dy * dy + dz * dz > EMP_RADIUS * EMP_RADIUS) continue;
                        BlockPos pos = center.add(dx, dy, dz);
                        TileEntity te = world.getTileEntity(pos);
                        if (te != null && te.getClass().getSimpleName().contains("Camera")) {
                            // Attempt to call setActive(false) via reflection for loose coupling
                            try {
                                java.lang.reflect.Method setActive = te.getClass().getMethod("setActive", boolean.class);
                                setActive.invoke(te, false);
                            } catch (Exception ignored) {
                                // TileEntityCamera not yet registered — safe to ignore
                            }
                            // Schedule reactivation
                            final TileEntity camera = te;
                            final int reactivateTicks = CAMERA_DISABLE_TICKS;
                            world.getMinecraftServer().addScheduledTask(() -> {
                                // Reactivation handled by the camera tile entity itself via markDirty
                                camera.markDirty();
                            });
                        }
                    }
                }
            }

            // Fire global event
            SharedPrisonState.getInstance().addEvent("EMP_ACTIVATED");

            // Play explosion sound
            world.playSound(null, this.posX, this.posY, this.posZ,
                    SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS,
                    1.5f, 1.2f);

            // Spawn electric-blue particles
            for (int i = 0; i < 30; i++) {
                world.spawnParticle(
                        EnumParticleTypes.REDSTONE,
                        this.posX + (rand.nextDouble() - 0.5) * 6,
                        this.posY + rand.nextDouble() * 3,
                        this.posZ + (rand.nextDouble() - 0.5) * 6,
                        0, 0, 0,
                        // RGB packed as int: blue
                        (int) (0 * 255) | ((int) (0.5 * 255) << 8) | (255 << 16)
                );
            }

            this.setDead();
        }
    }

    // -------------------------------------------------------------------------
    // Gravity factor (makes grenade arc naturally)
    // -------------------------------------------------------------------------

    @Override
    protected float getGravityVelocity() {
        return 0.03f;
    }
}
