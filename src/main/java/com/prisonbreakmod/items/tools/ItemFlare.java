package com.prisonbreakmod.items.tools;

import com.prisonbreakmod.items.projectiles.EntityFlareProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

public class ItemFlare extends Item {

    public ItemFlare() {
        this.setMaxStackSize(4);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!world.isRemote) {
            EntityFlareProjectile flare = new EntityFlareProjectile(world, player);
            flare.shoot(player, player.rotationPitch, player.rotationYaw, 0.0F, 1.8F, 1.0F);
            world.spawnEntity(flare);
            world.playSound(null, player.posX, player.posY, player.posZ,
                    net.minecraft.init.SoundEvents.ENTITY_FIREWORK_LAUNCH,
                    SoundCategory.PLAYERS, 1.0F, 1.5F);
            stack.shrink(1);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }
}
