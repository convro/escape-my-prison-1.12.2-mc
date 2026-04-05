package com.prisonbreakmod.items.tools;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/** Prison-made shiv. 3 uses. 2HP damage. Hidden item (NBT). */
public class ItemShiv extends Item {

    public ItemShiv() {
        this.setMaxStackSize(1);
        this.setMaxDamage(3);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, net.minecraft.entity.Entity entity) {
        if (!player.world.isRemote && entity instanceof EntityLivingBase) {
            EntityLivingBase target = (EntityLivingBase) entity;
            target.attackEntityFrom(net.minecraft.util.DamageSource.causePlayerDamage(player), 2.0F);
            stack.damageItem(1, player);
        }
        return false;
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        stack.damageItem(1, attacker);
        return true;
    }

    /** Mark item as hidden so BlockProtectionSystem can check for it. */
    public static ItemStack createHidden() {
        ItemStack stack = new ItemStack(com.prisonbreakmod.items.ModItems.SHIV);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean("hidden", true);
        stack.setTagCompound(tag);
        return stack;
    }

    public static boolean isHidden(ItemStack stack) {
        return stack.hasTagCompound() && stack.getTagCompound().getBoolean("hidden");
    }
}
