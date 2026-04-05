package com.prisonbreakmod.items.tools;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/** Fake ID document — allows passing through S10 (Staszek Brama) gate check. */
public class ItemFakeID extends Item {

    public ItemFakeID() {
        this.setMaxStackSize(1);
    }

    public static ItemStack create(String ownerName) {
        ItemStack stack = new ItemStack(com.prisonbreakmod.items.ModItems.FAKE_ID);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("ownerName", ownerName);
        tag.setBoolean("used", false);
        stack.setTagCompound(tag);
        return stack;
    }

    public static boolean isUsed(ItemStack stack) {
        return stack.hasTagCompound() && stack.getTagCompound().getBoolean("used");
    }

    public static void markUsed(ItemStack stack) {
        if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        stack.getTagCompound().setBoolean("used", true);
    }
}
