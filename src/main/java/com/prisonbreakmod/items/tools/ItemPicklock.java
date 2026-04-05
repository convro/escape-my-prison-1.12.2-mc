package com.prisonbreakmod.items.tools;

import com.prisonbreakmod.ai.SharedPrisonState;
import com.prisonbreakmod.events.AlertSystem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Picklock — Mk1 (tier=1, 1 use) or Mk2 (tier=2, 3 uses).
 * Handled by BlockProtectionSystem on use against doors/gates.
 */
public class ItemPicklock extends Item {

    private final int tier;

    public ItemPicklock(int tier) {
        this.tier = tier;
        this.setMaxStackSize(1);
        this.setMaxDamage(tier == 1 ? 1 : 3);
    }

    public int getTier() { return tier; }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos,
                                       EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote) return EnumActionResult.SUCCESS;
        // BlockProtectionSystem handles the actual unlock logic
        // This just signals intent — BlockBarredDoor.onBlockActivated checks for picklock
        ItemStack stack = player.getHeldItem(hand);
        // Damage the picklock
        stack.damageItem(1, player);
        return EnumActionResult.SUCCESS;
    }

    public static boolean isPicklockOfTier(ItemStack stack, int minTier) {
        if (stack.isEmpty()) return false;
        if (!(stack.getItem() instanceof ItemPicklock)) return false;
        return ((ItemPicklock) stack.getItem()).getTier() >= minTier;
    }
}
