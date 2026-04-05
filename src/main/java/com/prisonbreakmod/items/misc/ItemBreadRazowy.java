package com.prisonbreakmod.items.misc;

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

/** Dark rye bread — lasts 3 in-game days (NBT expiry tracking). */
public class ItemBreadRazowy extends Item {

    public ItemBreadRazowy() {
        this.setMaxStackSize(8);
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
            if (isExpired(stack, world.getTotalWorldTime())) {
                player.sendStatusMessage(
                        new net.minecraft.util.text.TextComponentString("§cChleb jest czerstwy i pleśniowy!"), true);
                // Poisoning effect
                player.addPotionEffect(new net.minecraft.potion.PotionEffect(
                        net.minecraft.init.MobEffects.POISON, 100, 0));
            } else {
                player.getFoodStats().addStats(4, 0.4F);
            }
        }
        stack.shrink(1);
        return stack;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) { return 32; }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) { return EnumAction.EAT; }

    public static ItemStack createFresh(long worldTime) {
        ItemStack stack = new ItemStack(com.prisonbreakmod.items.ModItems.BREAD_RAZOWY, 1);
        NBTTagCompound tag = new NBTTagCompound();
        // 3 in-game days = 72000 ticks
        tag.setLong("expiry", worldTime + 72000);
        stack.setTagCompound(tag);
        return stack;
    }

    public static boolean isExpired(ItemStack stack, long worldTime) {
        if (!stack.hasTagCompound()) return false;
        long expiry = stack.getTagCompound().getLong("expiry");
        return expiry > 0 && worldTime > expiry;
    }
}
