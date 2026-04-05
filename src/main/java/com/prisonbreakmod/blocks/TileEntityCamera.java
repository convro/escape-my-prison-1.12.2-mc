package com.prisonbreakmod.blocks;

import com.prisonbreakmod.ai.SharedPrisonState;
import com.prisonbreakmod.events.AlertSystem;
import com.prisonbreakmod.world.BlockProtectionSystem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class TileEntityCamera extends TileEntity implements ITickable {

    private float rotationAngle = 0;
    private boolean isRotating = false;
    private boolean active = true;
    private int zone = 0; // 0=default, 1=admin, 2=buffer, 3=tower
    private long reactivationTick = -1;

    // Cone: 60° FOV, 15 block range
    private static final float FOV_DEGREES = 30.0F; // half-angle = 30°
    private static final double RANGE = 15.0;

    @Override
    public void update() {
        if (world.isRemote || !active) {
            // Check scheduled reactivation
            if (!active && reactivationTick > 0 && world.getTotalWorldTime() >= reactivationTick) {
                active = true;
                reactivationTick = -1;
            }
            return;
        }

        // Only update once per second
        if (world.getTotalWorldTime() % 20 != 0) return;

        // Rotating cameras (towers): rotate 20° every 3 seconds
        if (isRotating && world.getTotalWorldTime() % 60 == 0) {
            rotationAngle = (rotationAngle + 20) % 360;
            markDirty();
        }

        // Build look direction from rotation angle
        double rad = Math.toRadians(rotationAngle);
        Vec3d lookDir = new Vec3d(-Math.sin(rad), 0, Math.cos(rad));

        Vec3d camPos = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

        List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class,
                new AxisAlignedBB(pos).grow(RANGE));

        for (EntityPlayer player : players) {
            Vec3d toPlayer = player.getPositionVector().subtract(camPos);
            double dist = toPlayer.lengthVector();
            if (dist > RANGE || dist < 0.5) continue;
            double angle = Math.toDegrees(Math.acos(
                    Math.max(-1, Math.min(1, toPlayer.normalize().dotProduct(lookDir)))));
            if (angle < FOV_DEGREES) {
                // Player detected
                if (!BlockProtectionSystem.isPlayerAllowedInZone(player, zone)) {
                    SharedPrisonState.getInstance().raiseAlert(1,
                            "CAM:" + pos + " detected " + player.getName());
                    SharedPrisonState.getInstance().addEvent(
                            "CAM_DETECT:" + pos + ":" + player.getName());
                }
            }
        }
    }

    public void disable(long durationTicks) {
        this.active = false;
        this.reactivationTick = world.getTotalWorldTime() + durationTicks;
        markDirty();
    }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; markDirty(); }
    public void setRotating(boolean rotating) { this.isRotating = rotating; markDirty(); }
    public void setZone(int zone) { this.zone = zone; markDirty(); }
    public float getRotationAngle() { return rotationAngle; }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        active = compound.getBoolean("active");
        isRotating = compound.getBoolean("rotating");
        rotationAngle = compound.getFloat("angle");
        zone = compound.getInteger("zone");
        reactivationTick = compound.getLong("reactiveTick");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setBoolean("active", active);
        compound.setBoolean("rotating", isRotating);
        compound.setFloat("angle", rotationAngle);
        compound.setInteger("zone", zone);
        compound.setLong("reactiveTick", reactivationTick);
        return compound;
    }
}
