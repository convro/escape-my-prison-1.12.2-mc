package com.prisonbreakmod.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockCamera extends Block {

    public BlockCamera() {
        super(Material.IRON);
        this.setHardness(-1.0F); // unbreakable normally
        this.setResistance(6000.0F);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) { return true; }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityCamera();
    }
}
