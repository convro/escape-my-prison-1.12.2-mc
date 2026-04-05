package com.prisonbreakmod.items.survival;

import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;

/** Full survival suit — body slot, +60 temperature. Required for prolonged outdoor survival. */
public class ItemSurvivalSuit extends ItemArmor {

    public static final int TEMP_BONUS = 60;

    public ItemSurvivalSuit() {
        super(ItemWarmJacket.GULAG_LEATHER, 0, EntityEquipmentSlot.CHEST);
        this.setMaxStackSize(1);
    }
}
