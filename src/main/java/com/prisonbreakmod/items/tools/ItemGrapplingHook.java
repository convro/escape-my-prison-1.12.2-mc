package com.prisonbreakmod.items.tools;

import net.minecraft.block.Block;
import net.minecraft.block.BlockIron;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class ItemGrapplingHook extends Item {

    public ItemGrapplingHook() {
        this.setMaxStackSize(1);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!world.isRemote) {
            GrapplingHookEntity hook = new GrapplingHookEntity(world, player);
            hook.shoot(player, player.rotationPitch, player.rotationYaw, 0.0F, 2.5F, 1.0F);
            world.spawnEntity(hook);
            world.playSound(null, player.posX, player.posY, player.posZ,
                    net.minecraft.init.SoundEvents.ENTITY_BOBBER_THROW,
                    SoundCategory.PLAYERS, 1.0F, 1.5F);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    public static class GrapplingHookEntity extends EntityThrowable {

        private EntityPlayer owner;

        public GrapplingHookEntity(World world) { super(world); }

        public GrapplingHookEntity(World world, EntityPlayer player) {
            super(world, player);
            this.owner = player;
        }

        @Override
        protected void onImpact(RayTraceResult result) {
            if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
                BlockPos hitPos = result.getBlockPos();
                Block block = world.getBlockState(hitPos).getBlock();
                boolean canAttach = block instanceof net.minecraft.block.BlockObsidian
                        || block.getUnlocalizedName().contains("iron")
                        || block.getUnlocalizedName().contains("stone");
                if (canAttach && owner != null) {
                    // Pull player toward hit point (up to 15 blocks)
                    double dx = hitPos.getX() + 0.5 - owner.posX;
                    double dy = hitPos.getY() + 0.5 - owner.posY;
                    double dz = hitPos.getZ() + 0.5 - owner.posZ;
                    double dist = Math.sqrt(dx*dx + dy*dy + dz*dz);
                    if (dist <= 15.0 && dist > 0.5) {
                        double speed = Math.min(1.5, dist * 0.3);
                        owner.motionX = (dx / dist) * speed;
                        owner.motionY = (dy / dist) * speed;
                        owner.motionZ = (dz / dist) * speed;
                    }
                    // Sound
                    world.playSound(null, hitPos,
                            net.minecraft.init.SoundEvents.BLOCK_CHAIN_PLACE,
                            SoundCategory.BLOCKS, 1.0F, 1.0F);
                }
            }
            this.setDead();
        }
    }
}
