package com.prisonbreakmod.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/** Convenience methods for NBT operations. */
public class NBTHelper {

    public static void setString(ItemStack stack, String key, String value) {
        ensureTag(stack).setString(key, value);
    }

    public static String getString(ItemStack stack, String key, String def) {
        if (!stack.hasTagCompound()) return def;
        return stack.getTagCompound().hasKey(key) ? stack.getTagCompound().getString(key) : def;
    }

    public static void setInt(ItemStack stack, String key, int value) {
        ensureTag(stack).setInteger(key, value);
    }

    public static int getInt(ItemStack stack, String key, int def) {
        if (!stack.hasTagCompound()) return def;
        return stack.getTagCompound().hasKey(key) ? stack.getTagCompound().getInteger(key) : def;
    }

    public static void setBool(ItemStack stack, String key, boolean value) {
        ensureTag(stack).setBoolean(key, value);
    }

    public static boolean getBool(ItemStack stack, String key, boolean def) {
        if (!stack.hasTagCompound()) return def;
        return stack.getTagCompound().hasKey(key) ? stack.getTagCompound().getBoolean(key) : def;
    }

    public static void setLong(ItemStack stack, String key, long value) {
        ensureTag(stack).setLong(key, value);
    }

    public static long getLong(ItemStack stack, String key, long def) {
        if (!stack.hasTagCompound()) return def;
        return stack.getTagCompound().hasKey(key) ? stack.getTagCompound().getLong(key) : def;
    }

    private static NBTTagCompound ensureTag(ItemStack stack) {
        if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        return stack.getTagCompound();
    }
}
