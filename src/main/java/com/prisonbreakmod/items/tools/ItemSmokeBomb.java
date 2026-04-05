package com.prisonbreakmod.items.tools;

import com.prisonbreakmod.ai.SharedPrisonState;
import com.prisonbreakmod.events.AlertSystem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import java.util.List;

public class ItemSmokeBomb extends Item {

    public ItemSmokeBomb() {
        this.setMaxStackSize(4);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!world.isRemote) {
            SmokeBombEntity bomb = new SmokeBombEntity(world, player);
            bomb.shoot(player, player.rotationPitch, player.rotationYaw, 0.0F, 1.5F, 1.0F);
            world.spawnEntity(bomb);
            stack.shrink(1);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    public static class SmokeBombEntity extends EntityThrowable {

        public SmokeBombEntity(World world) { super(world); }
        public SmokeBombEntity(World world, EntityLivingBase thrower) { super(world, thrower); }

        @Override
        protected void onImpact(RayTraceResult result) {
            if (!world.isRemote) {
                // Raise alert
                SharedPrisonState.getInstance().raiseAlert(1, "smoke_bomb@" +
                        (int)posX + "," + (int)posY + "," + (int)posZ);
                // Blind entities in 8 block radius
                AxisAlignedBB box = new AxisAlignedBB(
                        posX-8, posY-2, posZ-8, posX+8, posY+4, posZ+8);
                List<EntityLivingBase> targets = world.getEntitiesWithinAABB(EntityLivingBase.class, box);
                for (EntityLivingBase t : targets) {
                    t.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 400, 0));
                }
            } else {
                // Smoke particles
                for (int i = 0; i < 30; i++) {
                    world.spawnParticle(EnumParticleTypes.SMOKE_LARGE,
                            posX + (world.rand.nextDouble()-0.5)*8,
                            posY + world.rand.nextDouble()*3,
                            posZ + (world.rand.nextDouble()-0.5)*8,
                            0, 0.03, 0);
                }
            }
            this.setDead();
        }
    }
}
