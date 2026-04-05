package com.prisonbreakmod.blocks;

import com.prisonbreakmod.items.tools.ItemPicklock;
import com.prisonbreakmod.items.survival.ItemMasterKey;
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

public class BlockSafe extends Block {

    public BlockSafe() {
        super(Material.IRON);
        this.setHardness(-1.0F);
        this.setResistance(6000.0F);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) { return true; }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntitySafe();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
                                     EntityPlayer player, EnumHand hand,
                                     EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote) return true;
        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TileEntitySafe)) return false;
        TileEntitySafe safe = (TileEntitySafe) te;
        ItemStack held = player.getHeldItem(hand);

        if (safe.isLocked()) {
            if (ItemMasterKey.isMasterKey(held)) {
                safe.forceUnlock();
                player.sendStatusMessage(
                        new net.minecraft.util.text.TextComponentString("§aSejf otwarty!"), true);
            } else if (held.getItem() instanceof ItemPicklock) {
                ItemPicklock pick = (ItemPicklock) held.getItem();
                if (pick.getTier() >= 2) {
                    boolean opened = safe.tryUnlock(pick.getTier());
                    held.damageItem(1, player);
                    if (opened) {
                        player.sendStatusMessage(
                                new net.minecraft.util.text.TextComponentString("§aSejf otwarty!"), true);
                    } else {
                        player.sendStatusMessage(
                                new net.minecraft.util.text.TextComponentString(
                                        "§7Pracujesz nad zamkiem... (" + safe + ")"), true);
                    }
                }
            }
        } else {
            // Open — show contents
            for (ItemStack content : safe.getContents()) {
                if (!player.inventory.addItemStackToInventory(content.copy())) {
                    player.dropItem(content.copy(), false);
                }
            }
            safe.getContents().clear();
            player.sendStatusMessage(
                    new net.minecraft.util.text.TextComponentString("§aWziąłeś zawartość sejfu."), true);
        }
        return true;
    }
}
