package com.prisonbreakmod.blocks;

import com.prisonbreakmod.gui.GuiHiddenInventory;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockDeadDrop extends Block {

    public BlockDeadDrop() {
        super(Material.WOOD);
        this.setHardness(1.0F);
        this.setLightOpacity(0); // looks like normal floor block
    }

    @Override
    public boolean hasTileEntity(IBlockState state) { return true; }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityDeadDrop();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
                                     EntityPlayer player, EnumHand hand,
                                     EnumFacing facing, float hitX, float hitY, float hitZ) {
        // Shift + empty hand → open personal hidden stash GUI
        if (player.isSneaking() && player.getHeldItem(hand).isEmpty()) {
            if (world.isRemote) {
                GuiHiddenInventory.open(player);
            }
            return true;
        }

        if (world.isRemote) return true;
        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TileEntityDeadDrop)) return false;
        TileEntityDeadDrop drop = (TileEntityDeadDrop) te;

        ItemStack held = player.getHeldItem(hand);
        if (!held.isEmpty()) {
            // Deposit item
            boolean ok = drop.deposit(held.splitStack(1));
            if (ok) {
                player.sendStatusMessage(
                        new net.minecraft.util.text.TextComponentString(
                                "§7Zostawiłeś item w skrytce."), true);
            }
        } else {
            // Retrieve item
            ItemStack retrieved = drop.retrieve();
            if (!retrieved.isEmpty()) {
                if (!player.inventory.addItemStackToInventory(retrieved)) {
                    player.dropItem(retrieved, false);
                }
                player.sendStatusMessage(
                        new net.minecraft.util.text.TextComponentString(
                                "§7Wyjąłeś item ze skrytki."), true);
            } else {
                player.sendStatusMessage(
                        new net.minecraft.util.text.TextComponentString(
                                "§7Skrytka jest pusta."), true);
            }
        }
        return true;
    }
}
