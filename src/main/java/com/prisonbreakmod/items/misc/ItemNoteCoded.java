package com.prisonbreakmod.items.misc;

import net.minecraft.item.ItemStack;

/** Coded note — guards need 5 minutes to decode. 3x safer than simple note. */
public class ItemNoteCoded extends ItemNoteSimple {

    public static final float INTERCEPT_CHANCE = 0.05F;
    public static final int DECODE_TICKS = 6000; // 5 min

    public ItemNoteCoded() {
        super();
        this.setMaxStackSize(8);
    }

    public static ItemStack create(String sender, String recipient, String message) {
        ItemStack stack = ItemNoteSimple.create(sender, recipient, message);
        // Replace item type
        return new ItemStack(com.prisonbreakmod.items.ModItems.NOTE_CODED, 1, 0,
                stack.getTagCompound());
    }
}
