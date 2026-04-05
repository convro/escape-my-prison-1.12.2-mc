package com.prisonbreakmod.items.weapons;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

public class ItemTazer extends Item {

    public ItemTazer() {
        this.setMaxStackSize(1);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, net.minecraft.entity.Entity entity) {
        if (!player.world.isRemote && entity instanceof EntityLivingBase) {
            EntityLivingBase target = (EntityLivingBase) entity;
            // Range check 2 blocks
            if (player.getDistanceTo(target) > 2.5) return false;
            // 2HP damage
            target.attackEntityFrom(net.minecraft.util.DamageSource.causePlayerDamage(player), 2.0F);
            // Slowness IV + Mining Fatigue for 5s
            target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 100, 3));
            target.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 100, 0));
            // Electric spark particles
            World world = player.world;
            for (int i = 0; i < 6; i++) {
                world.spawnParticle(EnumParticleTypes.REDSTONE,
                        target.posX + (world.rand.nextDouble()-0.5)*0.5,
                        target.posY + 1 + world.rand.nextDouble()*0.5,
                        target.posZ + (world.rand.nextDouble()-0.5)*0.5,
                        0, 0, 0);
            }
            // Sound
            SoundEvent snd = SoundEvent.REGISTRY.getObject(
                    new ResourceLocation("prisonbreakmod:tazer_zap"));
            if (snd != null) {
                world.playSound(null, player.posX, player.posY, player.posZ,
                        snd, SoundCategory.PLAYERS, 1.0F, 1.0F);
            } else {
                world.playSound(null, player.posX, player.posY, player.posZ,
                        net.minecraft.init.SoundEvents.ENTITY_LIGHTNING_THUNDER,
                        SoundCategory.PLAYERS, 0.3F, 2.0F);
            }
            player.getCooldownTracker().setCooldown(this, 40);
        }
        return false;
    }
}
