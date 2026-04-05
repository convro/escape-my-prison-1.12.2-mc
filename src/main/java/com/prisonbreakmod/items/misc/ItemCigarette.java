package com.prisonbreakmod.items.misc;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/** Prison currency — stacks to 64. Nicotine for Zbyszek. */
public class ItemCigarette extends Item {

    public ItemCigarette() {
        this.setMaxStackSize(64);
    }

    public static ItemStack createBranded(String brand) {
        ItemStack stack = new ItemStack(com.prisonbreakmod.items.ModItems.CIGARETTE, 1);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("brand", brand);
        stack.setTagCompound(tag);
        return stack;
    }

    public static String getBrand(ItemStack stack) {
        if (stack.hasTagCompound()) return stack.getTagCompound().getString("brand");
        return "Belomorkanal";
    }
}
