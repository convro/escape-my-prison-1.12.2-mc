package com.prisonbreakmod.items.weapons;

import com.prisonbreakmod.items.projectiles.EntityGasCloud;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

public class ItemGasCan extends Item {

    public ItemGasCan() {
        this.setMaxStackSize(1);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!world.isRemote) {
            // Spawn gas cloud in front of player
            double dist = 4.0;
            double cx = player.posX + player.getLookVec().x * dist;
            double cy = player.posY + player.getEyeHeight();
            double cz = player.posZ + player.getLookVec().z * dist;
            EntityGasCloud cloud = new EntityGasCloud(world, cx, cy, cz);
            world.spawnEntity(cloud);
            // Sound
            SoundEvent snd = SoundEvent.REGISTRY.getObject(
                    new ResourceLocation("prisonbreakmod:gas_spray"));
            if (snd != null) {
                world.playSound(null, player.posX, player.posY, player.posZ,
                        snd, SoundCategory.PLAYERS, 1.0F, 1.0F);
            } else {
                world.playSound(null, player.posX, player.posY, player.posZ,
                        net.minecraft.init.SoundEvents.ENTITY_CREEPER_PRIMED,
                        SoundCategory.PLAYERS, 1.0F, 1.5F);
            }
        }
        player.getCooldownTracker().setCooldown(this, 60);
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }
}
