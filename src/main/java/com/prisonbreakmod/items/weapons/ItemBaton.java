package com.prisonbreakmod.items.weapons;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

public class ItemBaton extends Item {

    public ItemBaton() {
        this.setMaxStackSize(1);
        this.setMaxDamage(0);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, net.minecraft.entity.Entity entity) {
        if (!player.world.isRemote && entity instanceof EntityLivingBase) {
            EntityLivingBase target = (EntityLivingBase) entity;
            // 4HP damage
            target.attackEntityFrom(net.minecraft.util.DamageSource.causePlayerDamage(player), 4.0F);
            // Slowness II for 3s (60 ticks)
            target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 60, 1));
            // Play sound
            player.world.playSound(null, player.posX, player.posY, player.posZ,
                    net.minecraft.util.SoundEvent.REGISTRY.getObject(
                            new net.minecraft.util.ResourceLocation("prisonbreakmod:baton_hit")),
                    SoundCategory.PLAYERS, 1.0F, 1.0F);
            // CRIT particle
            if (player.world instanceof net.minecraft.world.WorldServer) {
                ((net.minecraft.world.WorldServer) player.world).spawnParticle(
                        EnumParticleTypes.CRIT, target.posX, target.posY + 1, target.posZ,
                        5, 0.2, 0.2, 0.2, 0.1);
            }
        }
        return false;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, net.minecraft.block.state.IBlockState state) {
        return 1.0F;
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        return true;
    }
}
