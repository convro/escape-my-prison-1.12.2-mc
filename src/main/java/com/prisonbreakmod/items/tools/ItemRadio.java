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
 * Radio — opens GuiRadio for text communication with Marek/Zbyszek at any distance.
 * Battery = 36000 ticks (30 real minutes). NBT tracks charge.
 */
public class ItemRadio extends Item {

    public static final int MAX_CHARGE = 36000;

    public ItemRadio() {
        this.setMaxStackSize(1);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (world.isRemote) {
            int charge = getCharge(stack);
            if (charge > 0) {
                // Open radio GUI (client-side)
                net.minecraft.client.Minecraft.getMinecraft().displayGuiScreen(
                        new com.prisonbreakmod.gui.GuiRadio(player, stack));
            } else {
                player.sendStatusMessage(
                        new net.minecraft.util.text.TextComponentString("§cRadio bez baterii!"), true);
            }
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    public static int getCharge(ItemStack stack) {
        if (!stack.hasTagCompound()) return MAX_CHARGE;
        return stack.getTagCompound().getInteger("charge");
    }

    public static void drainCharge(ItemStack stack, int amount) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
            stack.getTagCompound().setInteger("charge", MAX_CHARGE);
        }
        int current = stack.getTagCompound().getInteger("charge");
        stack.getTagCompound().setInteger("charge", Math.max(0, current - amount));
    }

    public static void recharge(ItemStack stack) {
        if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        stack.getTagCompound().setInteger("charge", MAX_CHARGE);
    }
}
