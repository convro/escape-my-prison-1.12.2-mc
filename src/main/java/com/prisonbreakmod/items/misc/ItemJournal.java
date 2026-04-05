package com.prisonbreakmod.items.misc;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

/** Player journal — stores mission notes, guard schedules, entries. Opens GuiJournal on use. */
public class ItemJournal extends Item {

    public ItemJournal() {
        this.setMaxStackSize(1);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (world.isRemote) {
            net.minecraft.client.Minecraft.getMinecraft().displayGuiScreen(
                    new com.prisonbreakmod.gui.GuiJournal(player.getHeldItem(hand)));
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    public static void addEntry(ItemStack stack, String entry) {
        if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        NBTTagList list = stack.getTagCompound().getTagList("entries", 8);
        list.appendTag(new NBTTagString(entry));
        stack.getTagCompound().setTag("entries", list);
    }

    public static java.util.List<String> getEntries(ItemStack stack) {
        java.util.List<String> result = new java.util.ArrayList<>();
        if (!stack.hasTagCompound()) return result;
        NBTTagList list = stack.getTagCompound().getTagList("entries", 8);
        for (int i = 0; i < list.tagCount(); i++) {
            result.add(list.getStringTagAt(i));
        }
        return result;
    }

    public static void addGuardSchedule(ItemStack stack, int guardId, String schedule) {
        if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        stack.getTagCompound().setString("guard_" + guardId, schedule);
    }

    public static String getGuardSchedule(ItemStack stack, int guardId) {
        if (!stack.hasTagCompound()) return "";
        return stack.getTagCompound().getString("guard_" + guardId);
    }
}
