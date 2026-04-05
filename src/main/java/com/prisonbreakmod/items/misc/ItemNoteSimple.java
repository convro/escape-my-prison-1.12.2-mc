package com.prisonbreakmod.items.misc;

import com.prisonbreakmod.ai.SharedPrisonState;
import com.prisonbreakmod.events.WhisperSystem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

/** Simple note — deliverable to NPC, 15% intercept chance. */
public class ItemNoteSimple extends Item {

    public static final float INTERCEPT_CHANCE = 0.15F;

    public ItemNoteSimple() {
        this.setMaxStackSize(8);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (world.isRemote) return new ActionResult<>(EnumActionResult.PASS, player.getHeldItem(hand));
        // Deliver to nearest NPC via WhisperSystem
        WhisperSystem.tryDeliverNote(player, player.getHeldItem(hand));
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    public static ItemStack create(String sender, String recipient, String message) {
        ItemStack stack = new ItemStack(com.prisonbreakmod.items.ModItems.NOTE_SIMPLE, 1);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("sender", sender);
        tag.setString("recipient", recipient);
        tag.setString("message", message.substring(0, Math.min(256, message.length())));
        stack.setTagCompound(tag);
        return stack;
    }

    public static String getMessage(ItemStack stack) {
        if (!stack.hasTagCompound()) return "";
        return stack.getTagCompound().getString("message");
    }

    public static String getSender(ItemStack stack) {
        if (!stack.hasTagCompound()) return "?";
        return stack.getTagCompound().getString("sender");
    }

    public static String getRecipient(ItemStack stack) {
        if (!stack.hasTagCompound()) return "?";
        return stack.getTagCompound().getString("recipient");
    }
}
