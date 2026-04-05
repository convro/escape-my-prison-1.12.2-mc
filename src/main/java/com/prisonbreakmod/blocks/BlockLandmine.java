package com.prisonbreakmod.blocks;

import com.prisonbreakmod.ai.SharedPrisonState;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class BlockLandmine extends Block {

    public BlockLandmine() {
        super(Material.GROUND);
        this.setHardness(0.1F);
        // Render as invisible (handled client-side by rendering override)
    }

    @Override
    public void onEntityWalk(World world, BlockPos pos, Entity entity) {
        if (!world.isRemote && entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            // Trigger mine
            world.setBlockToAir(pos);
            world.createExplosion(null, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                    3.0F, true);
            player.attackEntityFrom(DamageSource.GENERIC, 15.0F);
            SharedPrisonState.getInstance().raiseAlert(2, "Mine triggered at " + pos);
            SharedPrisonState.getInstance().addEvent("MINE_TRIGGERED@" + pos.getX() + "," + pos.getZ());
        }
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) { return false; }

    @Override
    public boolean isFullCube(IBlockState state) { return false; }
}
