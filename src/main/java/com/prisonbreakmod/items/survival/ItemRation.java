package com.prisonbreakmod.items.survival;

import com.prisonbreakmod.world.WeatherSystem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

/** Polar ration — restores 8 food bars + 10 temperature when eaten. */
public class ItemRation extends Item {

    private static final int USE_DURATION = 32;

    public ItemRation() {
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
    public ItemStack onItemUseFinish(ItemStack stack, World world, net.minecraft.entity.EntityLivingBase entity) {
        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            player.getFoodStats().addStats(8, 0.8F);
            WeatherSystem.getInstance().adjustTemperature(player, 10);
        }
        stack.shrink(1);
        return stack;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) { return USE_DURATION; }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) { return EnumAction.EAT; }
}
