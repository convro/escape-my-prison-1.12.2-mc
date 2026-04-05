package com.prisonbreakmod.items.tools;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** One-time rope — allows descending up to 8 blocks from attached block. */
public class ItemRope extends Item {

    public ItemRope() {
        this.setMaxStackSize(8);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos,
                                      EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote) return EnumActionResult.SUCCESS;
        Block block = world.getBlockState(pos).getBlock();
        // Must attach to wood or iron
        boolean isWood = block instanceof net.minecraft.block.BlockLog
                || block instanceof net.minecraft.block.BlockPlanks;
        boolean isIron = block.getUnlocalizedName().contains("iron");
        if (!isWood && !isIron) return EnumActionResult.FAIL;

        // Move player down up to 8 blocks
        BlockPos ropeTop = pos.down();
        double targetY = player.posY;
        for (int i = 0; i < 8; i++) {
            BlockPos check = ropeTop.down(i);
            if (!world.isAirBlock(check)) break;
            targetY = check.getY();
        }

        // Spawn string particles as visual rope
        for (int i = 0; i < 8; i++) {
            world.spawnParticle(EnumParticleTypes.SUSPENDED,
                    pos.getX() + 0.5, pos.getY() - i, pos.getZ() + 0.5,
                    0, 0, 0);
        }

        player.setPositionAndUpdate(player.posX, targetY, player.posZ);
        // Consume one rope
        ItemStack held = player.getHeldItem(hand);
        held.shrink(1);
        return EnumActionResult.SUCCESS;
    }
}
