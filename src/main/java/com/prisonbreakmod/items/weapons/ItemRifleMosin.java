package com.prisonbreakmod.items.weapons;

import com.prisonbreakmod.items.projectiles.EntityProjectileBullet;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
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

public class ItemRifleMosin extends Item {

    private static final int COOLDOWN = 80; // 4s
    private static final int USE_DURATION = 72000;

    public ItemRifleMosin() {
        this.setMaxStackSize(1);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        player.setActiveHand(hand);
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, net.minecraft.entity.EntityLivingBase entityLiving, int timeLeft) {
        if (!(entityLiving instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) entityLiving;
        int useTicks = USE_DURATION - timeLeft;
        if (useTicks < 10) return; // must aim for at least 0.5s

        if (!world.isRemote) {
            EntityProjectileBullet bullet = new EntityProjectileBullet(world, player, 9.0, true);
            bullet.shoot(player, player.rotationPitch, player.rotationYaw, 0, 4.5F, 0.5F);
            world.spawnEntity(bullet);
            // Recoil
            player.motionY -= 0.05;
            // Sound
            SoundEvent shootSound = SoundEvent.REGISTRY.getObject(
                    new ResourceLocation("prisonbreakmod:gunshot_rifle"));
            if (shootSound != null) {
                world.playSound(null, player.posX, player.posY, player.posZ,
                        shootSound, SoundCategory.PLAYERS, 3.0F, 0.9F);
            } else {
                world.playSound(null, player.posX, player.posY, player.posZ,
                        net.minecraft.init.SoundEvents.ENTITY_ARROW_SHOOT,
                        SoundCategory.PLAYERS, 3.0F, 0.4F);
            }
        } else {
            // Muzzle smoke
            world.spawnParticle(EnumParticleTypes.SMOKE_LARGE,
                    player.posX + player.getLookVec().x * 1.5,
                    player.posY + player.getEyeHeight(),
                    player.posZ + player.getLookVec().z * 1.5,
                    0.1, 0.1, 0.1, 0.05);
        }
        player.getCooldownTracker().setCooldown(this, COOLDOWN);
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.BOW;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return USE_DURATION;
    }
}
