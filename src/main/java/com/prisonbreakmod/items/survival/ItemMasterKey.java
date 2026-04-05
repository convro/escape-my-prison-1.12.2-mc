package com.prisonbreakmod.items.survival;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Master Key — opens ALL locks. Tracks which locks have been used via NBT. */
public class ItemMasterKey extends Item {

    public ItemMasterKey() {
        this.setMaxStackSize(1);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos,
                                       EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote) return EnumActionResult.SUCCESS;
        // Mark this lock position as used in NBT
        ItemStack stack = player.getHeldItem(hand);
        markLockUsed(stack, pos);
        // Actual unlock handled by block's onBlockActivated checking hasUsedLock
        return EnumActionResult.SUCCESS;
    }

    public static void markLockUsed(ItemStack stack, BlockPos pos) {
        if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        String key = pos.getX() + "," + pos.getY() + "," + pos.getZ();
        stack.getTagCompound().setBoolean("lock_" + key, true);
    }

    public static boolean hasUnlockedPos(ItemStack stack, BlockPos pos) {
        if (!stack.hasTagCompound()) return false;
        String key = pos.getX() + "," + pos.getY() + "," + pos.getZ();
        return stack.getTagCompound().getBoolean("lock_" + key);
    }

    /** Returns true — master key can open any lock regardless of tier. */
    public static boolean isMasterKey(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ItemMasterKey;
    }
}
