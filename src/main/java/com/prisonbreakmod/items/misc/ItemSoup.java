package com.prisonbreakmod.items.misc;

import com.prisonbreakmod.world.WeatherSystem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

/** Camp soup — hot variant also gives +10 temperature. */
public class ItemSoup extends Item {

    public ItemSoup() {
        this.setMaxStackSize(4);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (player.canEat(false)) {
            player.setActiveHand(hand);
            return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
        }
        return new ActionResult<>(EnumActionResult.FAIL, player.getHeldItem(hand));
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World world, EntityLivingBase entity) {
        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            player.getFoodStats().addStats(5, 0.6F);
            if (isHot(stack)) {
                WeatherSystem.getInstance().adjustTemperature(player, 10);
            }
        }
        stack.shrink(1);
        return stack;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) { return 32; }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) { return EnumAction.EAT; }

    public static boolean isHot(ItemStack stack) {
        return stack.hasTagCompound() && stack.getTagCompound().getBoolean("hot");
    }

    public static ItemStack createHot() {
        ItemStack stack = new ItemStack(com.prisonbreakmod.items.ModItems.SOUP, 1);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean("hot", true);
        stack.setTagCompound(tag);
        return stack;
    }
}
