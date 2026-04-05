package com.prisonbreakmod.items.tools;

import com.prisonbreakmod.ai.SharedPrisonState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemEMPGrenade extends Item {

    public ItemEMPGrenade() {
        this.setMaxStackSize(4);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!world.isRemote) {
            // Throw EMP projectile
            com.prisonbreakmod.items.projectiles.EntityEMPGrenade emp =
                    new com.prisonbreakmod.items.projectiles.EntityEMPGrenade(world, player);
            emp.shoot(player, player.rotationPitch, player.rotationYaw, 0.0F, 1.5F, 1.0F);
            world.spawnEntity(emp);
            world.playSound(null, player.posX, player.posY, player.posZ,
                    net.minecraft.init.SoundEvents.ENTITY_SNOWBALL_THROW,
                    SoundCategory.PLAYERS, 1.0F, 1.0F);
            stack.shrink(1);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }
}
