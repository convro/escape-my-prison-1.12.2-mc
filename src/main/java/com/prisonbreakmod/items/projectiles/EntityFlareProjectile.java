package com.prisonbreakmod.items.projectiles;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

/**
 * A flare projectile that blinds entities in a forward cone on impact.
 */
public class EntityFlareProjectile extends EntityThrowable {

    private static final float CONE_HALF_ANGLE_DEG = 20.0f;   // half-angle of cone
    private static final float CONE_RANGE = 20.0f;             // blocks
    private static final int   BLINDNESS_TICKS = 600;          // 30 seconds

    public EntityFlareProjectile(World world) {
        super(world);
    }

    public EntityFlareProjectile(World world, EntityLivingBase thrower) {
        super(world, thrower);
    }

    public EntityFlareProjectile(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    // -------------------------------------------------------------------------
    // Update — spawn blaze particles while in flight
    // -------------------------------------------------------------------------

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (world.isRemote) {
            world.spawnParticle(EnumParticleTypes.FLAME,
                    posX, posY, posZ,
                    -motionX * 0.2, -motionY * 0.2, -motionZ * 0.2);
            world.spawnParticle(EnumParticleTypes.SMOKE_LARGE,
                    posX, posY, posZ, 0, 0.01, 0);
        }
    }

    // -------------------------------------------------------------------------
    // Impact
    // -------------------------------------------------------------------------

    @Override
    protected void onImpact(RayTraceResult result) {
        if (!world.isRemote) {
            // Determine the forward direction of the flare at impact (its velocity)
            Vec3d forward = new Vec3d(motionX, motionY, motionZ).normalize();

            // Collect all living entities in a large bounding box first, then filter by cone
            AxisAlignedBB searchBox = new AxisAlignedBB(
                    posX - CONE_RANGE, posY - CONE_RANGE, posZ - CONE_RANGE,
                    posX + CONE_RANGE, posY + CONE_RANGE, posZ + CONE_RANGE
            );
            List<EntityLivingBase> candidates = world.getEntitiesWithinAABB(EntityLivingBase.class, searchBox);

            double cosHalfAngle = Math.cos(Math.toRadians(CONE_HALF_ANGLE_DEG));

            for (EntityLivingBase entity : candidates) {
                Vec3d toEntity = new Vec3d(
                        entity.posX - posX,
                        entity.posY + entity.getEyeHeight() - posY,
                        entity.posZ - posZ
                );
                double dist = toEntity.lengthVector();
                if (dist > CONE_RANGE) continue;

                Vec3d toEntityNorm = toEntity.normalize();
                double dot = forward.dotProduct(toEntityNorm);
                if (dot >= cosHalfAngle) {
                    entity.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, BLINDNESS_TICKS, 0, false, false));
                }
            }

            // Spawn blaze burst particles (visible via broadcastBlockBreak-like mechanism)
            for (int i = 0; i < 20; i++) {
                world.spawnParticle(
                        EnumParticleTypes.FLAME,
                        posX + (rand.nextDouble() - 0.5) * 2,
                        posY + rand.nextDouble() * 2,
                        posZ + (rand.nextDouble() - 0.5) * 2,
                        (rand.nextDouble() - 0.5) * 0.3,
                        rand.nextDouble() * 0.3,
                        (rand.nextDouble() - 0.5) * 0.3
                );
            }

            this.setDead();
        }
    }

    @Override
    protected float getGravityVelocity() {
        return 0.01f; // flares travel fairly flat
    }
}
