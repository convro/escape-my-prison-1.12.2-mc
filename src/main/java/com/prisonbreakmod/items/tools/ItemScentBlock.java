package com.prisonbreakmod.items.tools;

import com.prisonbreakmod.ai.SharedPrisonState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Placed in ground to neutralize tracking dog Burek for 3 minutes (3600 ticks). */
public class ItemScentBlock extends Item {

    private static final int DURATION = 3600;

    public ItemScentBlock() {
        this.setMaxStackSize(4);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos,
                                       EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote) return EnumActionResult.SUCCESS;
        if (!world.isAirBlock(pos.up())) return EnumActionResult.FAIL;
        SharedPrisonState state = SharedPrisonState.getInstance();
        state.setDogNeutralized(true);
        state.addEvent("SCENT_BLOCK_PLACED@" + pos.getX() + "," + pos.getZ());
        // Schedule re-enable after DURATION ticks via event system
        state.scheduleDogReactivation(world.getTotalWorldTime() + DURATION);
        player.getHeldItem(hand).shrink(1);
        return EnumActionResult.SUCCESS;
    }
}
