package com.prisonbreakmod.items.projectiles;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

/**
 * A bullet projectile fired by firearm items (pistol, rifle).
 * Extends EntityArrow so it benefits from Forge's existing trajectory math.
 */
public class EntityProjectileBullet extends EntityArrow {

    private double damage;
    private boolean pierce; // used by rifle for block penetration

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /** Required no-arg constructor for EntityRegistry. */
    public EntityProjectileBullet(World world) {
        super(world);
        this.pickupStatus = PickupStatus.DISALLOWED;
    }

    public EntityProjectileBullet(World world, EntityLivingBase shooter, double damage) {
        super(world, shooter);
        this.damage = damage;
        this.pickupStatus = PickupStatus.DISALLOWED;
        this.setDamage(damage);
    }

    public EntityProjectileBullet(World world, EntityLivingBase shooter, double damage, boolean pierce) {
        this(world, shooter, damage);
        this.pierce = pierce;
    }

    // -------------------------------------------------------------------------
    // Data watcher
    // -------------------------------------------------------------------------

    @Override
    protected void entityInit() {
        super.entityInit();
    }

    // -------------------------------------------------------------------------
    // NBT
    // -------------------------------------------------------------------------

    @Override
    public void writeEntityToNBT(net.minecraft.nbt.NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setDouble("BulletDamage", damage);
        compound.setBoolean("Pierce", pierce);
    }

    @Override
    public void readEntityFromNBT(net.minecraft.nbt.NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        damage = compound.getDouble("BulletDamage");
        pierce = compound.getBoolean("Pierce");
        this.setDamage(damage);
    }

    // -------------------------------------------------------------------------
    // Impact
    // -------------------------------------------------------------------------

    @Override
    protected void onHit(RayTraceResult result) {
        if (result.typeOfHit == RayTraceResult.Type.ENTITY && result.entityHit instanceof EntityLivingBase) {
            // Spawn blood/impact particles on the client side
            if (this.world.isRemote) {
                for (int i = 0; i < 6; i++) {
                    this.world.spawnParticle(
                            EnumParticleTypes.CRIT,
                            result.entityHit.posX + (this.rand.nextDouble() - 0.5) * 0.5,
                            result.entityHit.posY + result.entityHit.height * 0.5,
                            result.entityHit.posZ + (this.rand.nextDouble() - 0.5) * 0.5,
                            0, 0, 0
                    );
                }
            }
        } else if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
            // Spawn block-impact particles
            if (this.world.isRemote) {
                for (int i = 0; i < 4; i++) {
                    this.world.spawnParticle(
                            EnumParticleTypes.SMOKE_NORMAL,
                            result.hitVec.x, result.hitVec.y, result.hitVec.z,
                            0, 0.05, 0
                    );
                }
            }
            if (!pierce) {
                super.onHit(result);
                return;
            }
            // pierce=true (rifle): skip block stop, let arrow continue
            return;
        }
        super.onHit(result);
    }

    // -------------------------------------------------------------------------
    // Rendering helper
    // -------------------------------------------------------------------------

    /** Bullets don't render as standard arrows — return empty model. */
    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 0;
    }
}
