package com.prisonbreakmod.items.weapons;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

/**
 * Guard flashlight — when held, emits a directional light cone
 * visible as REDSTONE particles, and extends guard night detection to 30 blocks.
 */
public class ItemFlashlight extends Item {

    public ItemFlashlight() {
        this.setMaxStackSize(1);
    }

    /** Called every 5 ticks when held by a guard NPC (via EntityGuard.onLivingUpdate). */
    public static void emitConeParticles(World world, double posX, double posY, double posZ,
                                          float yaw, float pitch) {
        if (!world.isRemote) return;
        double yawRad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);
        double dirX = -Math.sin(yawRad) * Math.cos(pitchRad);
        double dirY = -Math.sin(pitchRad);
        double dirZ = Math.cos(yawRad) * Math.cos(pitchRad);

        for (double dist = 2; dist <= 15; dist += 3) {
            double px = posX + dirX * dist;
            double py = posY + dirY * dist + 1.6;
            double pz = posZ + dirZ * dist;
            world.spawnParticle(EnumParticleTypes.REDSTONE, px, py, pz, 1.0, 1.0, 0.2);
        }
    }
}
