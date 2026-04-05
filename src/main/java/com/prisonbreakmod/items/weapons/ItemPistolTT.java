package com.prisonbreakmod.items.weapons;

import com.prisonbreakmod.items.projectiles.EntityProjectileBullet;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

public class ItemPistolTT extends Item {

    private static final int COOLDOWN = 40; // 2s

    public ItemPistolTT() {
        this.setMaxStackSize(1);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!world.isRemote) {
            // Shoot bullet
            EntityProjectileBullet bullet = new EntityProjectileBullet(world, player, 6.0, false);
            bullet.shoot(player, player.rotationPitch, player.rotationYaw, 0, 3.0F, 1.0F);
            world.spawnEntity(bullet);
            // Sound
            SoundEvent shootSound = SoundEvent.REGISTRY.getObject(
                    new ResourceLocation("prisonbreakmod:gunshot_pistol"));
            if (shootSound != null) {
                world.playSound(null, player.posX, player.posY, player.posZ,
                        shootSound, SoundCategory.PLAYERS, 2.0F, 1.0F);
            } else {
                world.playSound(null, player.posX, player.posY, player.posZ,
                        net.minecraft.init.SoundEvents.ENTITY_ARROW_SHOOT,
                        SoundCategory.PLAYERS, 2.0F, 0.5F);
            }
        } else {
            // Muzzle flash (client)
            world.spawnParticle(EnumParticleTypes.FLAME,
                    player.posX + player.getLookVec().x,
                    player.posY + player.getEyeHeight() + player.getLookVec().y,
                    player.posZ + player.getLookVec().z,
                    0.05, 0.05, 0.05, 0.05);
        }
        player.getCooldownTracker().setCooldown(this, COOLDOWN);
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }
}
