package com.prisonbreakmod.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Unstable ice patch — random chance of collapsing (damage + slowness) when walked on. */
public class BlockIcePatch extends Block {

    public BlockIcePatch() {
        super(Material.ICE);
        this.setHardness(0.5F);
        this.slipperiness = 0.98F; // Very slippery
    }

    @Override
    public void onEntityWalk(World world, BlockPos pos, Entity entity) {
        if (!world.isRemote && entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            // 15% chance to collapse
            if (world.rand.nextFloat() < 0.15F) {
                world.setBlockToAir(pos);
                // Trap player briefly (fall into water/air below)
                player.attackEntityFrom(net.minecraft.util.DamageSource.FALL, 3.0F);
                player.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 60, 2));
            }
        }
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) { return false; }
}
