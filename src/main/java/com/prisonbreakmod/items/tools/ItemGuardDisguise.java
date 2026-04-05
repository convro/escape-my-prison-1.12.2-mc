package com.prisonbreakmod.items.tools;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

/**
 * Guard disguise — when worn, player passes as a guard.
 * S01 (Wiśniewski) always sees through it at any distance.
 * Others detect within 3 blocks if they "know" the player.
 */
public class ItemGuardDisguise extends Item {

    public ItemGuardDisguise() {
        this.setMaxStackSize(1);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!world.isRemote) {
            boolean worn = isWorn(stack);
            setWorn(stack, !worn);
            String msg = !worn ? "§aUbrałeś przebranie strażnika." : "§7Zdjąłeś przebranie.";
            player.sendStatusMessage(new net.minecraft.util.text.TextComponentString(msg), true);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    public static boolean isWorn(ItemStack stack) {
        return stack.hasTagCompound() && stack.getTagCompound().getBoolean("worn");
    }

    public static void setWorn(ItemStack stack, boolean worn) {
        if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        stack.getTagCompound().setBoolean("worn", worn);
    }
}
