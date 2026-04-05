package com.prisonbreakmod.blocks;

import com.prisonbreakmod.events.AlertSystem;
import com.prisonbreakmod.items.survival.ItemMasterKey;
import com.prisonbreakmod.items.tools.ItemPicklock;
import com.prisonbreakmod.world.BlockProtectionSystem;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Barred/grated door — requires picklock Mk1/Mk2 or Master Key to open. */
public class BlockBarredDoor extends Block {

    /** Metadata: 0=closed tier1, 1=closed tier2, 2=open */
    public BlockBarredDoor() {
        super(Material.IRON);
        this.setHardness(5.0F);
        this.setResistance(2000.0F);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
                                     EntityPlayer player, EnumHand hand,
                                     EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote) return true;
        ItemStack held = player.getHeldItem(hand);
        int meta = world.getBlockMetadata(pos);

        if (ItemMasterKey.isMasterKey(held)) {
            openDoor(world, pos);
            return true;
        }

        if (held.getItem() instanceof ItemPicklock) {
            ItemPicklock pick = (ItemPicklock) held.getItem();
            int required = meta == 0 ? 1 : 2;
            if (pick.getTier() >= required) {
                openDoor(world, pos);
                held.damageItem(1, player);
                return true;
            }
        }

        // No valid tool — alert
        AlertSystem.raise(world, 1, "Attempt to open barred door at " + pos);
        return false;
    }

    private void openDoor(World world, BlockPos pos) {
        // Replace with air (open) — real impl would animate
        world.setBlockToAir(pos);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) { return false; }
    @Override
    public boolean isFullCube(IBlockState state) { return false; }
}
