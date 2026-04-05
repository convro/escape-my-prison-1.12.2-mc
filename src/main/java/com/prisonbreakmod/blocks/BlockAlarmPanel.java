package com.prisonbreakmod.blocks;

import com.prisonbreakmod.ai.SharedPrisonState;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockAlarmPanel extends Block {

    public BlockAlarmPanel() {
        super(Material.IRON);
        this.setHardness(3.0F);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
                                     EntityPlayer player, EnumHand hand,
                                     EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            // Guards trigger alarm by interacting; player triggering = Alert+2
            SharedPrisonState.getInstance().raiseAlert(2, "Alarm panel activated at " + pos);
        }
        return true;
    }

    /** Called by sabotage event to disable panel. */
    public static void sabotage(World world, BlockPos pos) {
        world.setBlockState(pos, world.getBlockState(pos));
        SharedPrisonState.getInstance().addEvent("ALARM_PANEL_SABOTAGED:" + pos);
    }
}
