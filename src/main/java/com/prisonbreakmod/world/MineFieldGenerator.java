package com.prisonbreakmod.world;

import com.prisonbreakmod.blocks.ModBlocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

/** Places hidden landmine blocks in the buffer zone (200-500 blocks from prison). */
public class MineFieldGenerator {

    private static final float MINE_DENSITY = 0.03F; // 3% of surface blocks

    public static void populate(World world, int chunkX, int chunkZ, Random rand) {
        if (ModBlocks.LANDMINE == null) return;
        int wx = chunkX * 16, wz = chunkZ * 16;

        for (int bx = 0; bx < 16; bx++) {
            for (int bz = 0; bz < 16; bz++) {
                if (rand.nextFloat() < MINE_DENSITY) {
                    int x = wx + bx, z = wz + bz;
                    BlockPos surface = world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z));
                    if (surface.getY() > 60) {
                        // Place mine just under snow layer (hidden)
                        BlockPos minePos = surface;
                        if (world.getBlockState(minePos).getBlock() ==
                                net.minecraft.init.Blocks.SNOW_LAYER) {
                            minePos = surface.down();
                        }
                        world.setBlockState(minePos, ModBlocks.LANDMINE.getDefaultState(), 2);
                    }
                }
            }
        }
    }
}
