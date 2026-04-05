package com.prisonbreakmod.items.survival;

import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

/** Warm jacket — chest slot, +30 to temperature bar. */
public class ItemWarmJacket extends ItemArmor {

    public static final ArmorMaterial GULAG_LEATHER = net.minecraftforge.common.util.EnumHelper
            .addArmorMaterial("GULAG_LEATHER", "prisonbreakmod:gulag_leather",
                    5, new int[]{1, 2, 3, 1}, 5, net.minecraft.util.SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 0.0F);

    public static final int TEMP_BONUS = 30;

    public ItemWarmJacket() {
        super(GULAG_LEATHER, 0, EntityEquipmentSlot.CHEST);
        this.setMaxStackSize(1);
    }
}
