package com.prisonbreakmod.items.tools;

import com.prisonbreakmod.crafting.ThermiteHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Thermite — applied to PROTECTED_ABSOLUTE block; burns for 5 real minutes to destroy it. */
public class ItemThermite extends Item {

    public ItemThermite() {
        this.setMaxStackSize(1);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos,
                                       EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote) return EnumActionResult.SUCCESS;
        // ThermiteHandler manages the 5-minute timer
        boolean started = ThermiteHandler.startBurning(world, pos, player.getHeldItem(hand));
        if (started) {
            player.getHeldItem(hand).shrink(1);
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.FAIL;
    }
}
