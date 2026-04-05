package com.prisonbreakmod.items.projectiles;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.List;

/**
 * A stationary gas cloud entity that lingers in the world and applies
 * debuff effects to any living entity within its radius.
 */
public class EntityGasCloud extends Entity {

    private static final int DEFAULT_LIFETIME = 200; // ticks
    private static final int BLIZZARD_LIFETIME = 50; // ticks — shrinks faster
    private static final float RADIUS = 5.0f;

    private int ticksExisted2 = 0; // shadow the super field for clarity
    private int lifetime = DEFAULT_LIFETIME;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public EntityGasCloud(World world) {
        super(world);
        this.setSize(RADIUS * 2, 2.0f);
        this.noClip = true;
        this.isImmuneToFire = true;
    }

    public EntityGasCloud(World world, double x, double y, double z) {
        this(world);
        this.setPosition(x, y, z);
    }

    // -------------------------------------------------------------------------
    // Entity boilerplate
    // -------------------------------------------------------------------------

    @Override
    protected void entityInit() {
        // No data watcher entries required
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        ticksExisted2 = compound.getInteger("GasTicks");
        lifetime = compound.getInteger("GasLifetime");
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setInteger("GasTicks", ticksExisted2);
        compound.setInteger("GasLifetime", lifetime);
    }

    // -------------------------------------------------------------------------
    // Update
    // -------------------------------------------------------------------------

    @Override
    public void onUpdate() {
        super.onUpdate();
        ticksExisted2++;

        // Determine effective lifetime (blizzard check via biome temperature)
        boolean isBlizzardCondition = world.getBiome(this.getPosition()).getTemperature(this.getPosition()) < 0.15f
                && world.isRaining();
        int effectiveLifetime = isBlizzardCondition ? BLIZZARD_LIFETIME : lifetime;

        if (ticksExisted2 >= effectiveLifetime) {
            this.setDead();
            return;
        }

        // Spawn green/yellow gas particles on client
        if (world.isRemote) {
            for (int i = 0; i < 5; i++) {
                double ox = (rand.nextDouble() - 0.5) * RADIUS * 2;
                double oy = rand.nextDouble() * 2.0;
                double oz = (rand.nextDouble() - 0.5) * RADIUS * 2;
                world.spawnParticle(
                        EnumParticleTypes.SMOKE_LARGE,
                        posX + ox, posY + oy, posZ + oz,
                        0, 0.02, 0
                );
            }
        }

        // Server-side: apply effects to entities in range
        if (!world.isRemote) {
            AxisAlignedBB aabb = new AxisAlignedBB(
                    posX - RADIUS, posY - 1, posZ - RADIUS,
                    posX + RADIUS, posY + 3, posZ + RADIUS
            );
            List<EntityLivingBase> targets = world.getEntitiesWithinAABB(EntityLivingBase.class, aabb);
            for (EntityLivingBase target : targets) {
                double dx = target.posX - posX;
                double dz = target.posZ - posZ;
                if (dx * dx + dz * dz <= RADIUS * RADIUS) {
                    target.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 160, 0, false, false));
                    target.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 160, 0, false, false));
                    target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 160, 3, false, false));
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Gravity / physics
    // -------------------------------------------------------------------------

    @Override
    public boolean hasNoGravity() {
        return true;
    }
}
