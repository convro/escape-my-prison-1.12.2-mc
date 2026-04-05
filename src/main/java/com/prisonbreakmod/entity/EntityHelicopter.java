package com.prisonbreakmod.entity;

import com.prisonbreakmod.ai.SharedPrisonState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.List;

/**
 * Helicopter entity that appears after escape (Alert Level 3).
 * Scans for players within 50 blocks and reports to SharedPrisonState.
 */
public class EntityHelicopter extends Entity {

    private static final double SCAN_RADIUS = 50.0;
    private static final double MOVE_SPEED = 0.4;
    private static final int SCAN_INTERVAL = 40; // every 2s

    private double targetX, targetY, targetZ;
    private boolean active = false;
    private int scanTimer = 0;

    public EntityHelicopter(World world) {
        super(world);
        this.setSize(2.0F, 1.0F);
        this.noClip = true;
        this.isImmuneToFire = true;
    }

    public EntityHelicopter(World world, double x, double y, double z) {
        this(world);
        this.setPosition(x, y + 30, z);
        this.targetX = x;
        this.targetY = y + 30;
        this.targetZ = z;
        this.active = true;
        SharedPrisonState.getInstance().setHelicopterActive(true);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (world.isRemote || !active) return;

        // Move toward target
        double dx = targetX - posX;
        double dy = targetY - posY;
        double dz = targetZ - posZ;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (dist > 2.0) {
            motionX = (dx / dist) * MOVE_SPEED;
            motionY = (dy / dist) * MOVE_SPEED * 0.3;
            motionZ = (dz / dist) * MOVE_SPEED;
        } else {
            // Pick new random target near last known player position
            motionX = (world.rand.nextDouble() - 0.5) * MOVE_SPEED;
            motionZ = (world.rand.nextDouble() - 0.5) * MOVE_SPEED;
            motionY = 0;
            targetX = posX + (world.rand.nextDouble() - 0.5) * 200;
            targetZ = posZ + (world.rand.nextDouble() - 0.5) * 200;
        }
        moveEntity(motionX, motionY, motionZ);

        // Scan for players
        scanTimer--;
        if (scanTimer <= 0) {
            scanTimer = SCAN_INTERVAL;
            scanForPlayers();
        }
    }

    private void scanForPlayers() {
        AxisAlignedBB scanBox = new AxisAlignedBB(
                posX - SCAN_RADIUS, posY - SCAN_RADIUS, posZ - SCAN_RADIUS,
                posX + SCAN_RADIUS, posY + SCAN_RADIUS, posZ + SCAN_RADIUS);
        List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class, scanBox);
        for (EntityPlayer player : players) {
            // Check line of sight roughly
            double px = player.posX - posX;
            double py = player.posY - posY;
            double pz = player.posZ - posZ;
            double d = Math.sqrt(px * px + py * py + pz * pz);
            if (d <= SCAN_RADIUS) {
                SharedPrisonState.getInstance().addEvent(
                        "HELICOPTER_SPOTTED:" + player.getName() +
                        "@" + (int) player.posX + "," + (int) player.posY + "," + (int) player.posZ);
                // Move toward player
                targetX = player.posX;
                targetY = player.posY + 25;
                targetZ = player.posZ;
            }
        }
    }

    public void setTarget(double x, double y, double z) {
        this.targetX = x;
        this.targetY = y + 25;
        this.targetZ = z;
    }

    public void deactivate() {
        this.active = false;
        SharedPrisonState.getInstance().setHelicopterActive(false);
        this.setDead();
    }

    @Override
    protected void entityInit() {}

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        this.active = compound.getBoolean("active");
        this.targetX = compound.getDouble("tx");
        this.targetY = compound.getDouble("ty");
        this.targetZ = compound.getDouble("tz");
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setBoolean("active", active);
        compound.setDouble("tx", targetX);
        compound.setDouble("ty", targetY);
        compound.setDouble("tz", targetZ);
    }
}
