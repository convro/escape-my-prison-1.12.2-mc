package com.prisonbreakmod.world;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Custom chunk generator for the Siberian gulag world.
 * Generates frozen tundra terrain with no vanilla structures.
 */
public class PrisonChunkProvider implements IChunkGenerator {

    private final World world;
    private final Random rand;
    private final long seed;

    private static final int SEA_LEVEL = 63;

    public PrisonChunkProvider(World world, long seed) {
        this.world = world;
        this.seed = seed;
        this.rand = new Random(seed);
    }

    @Override
    public Chunk generateChunk(int x, int z) {
        ChunkPrimer primer = new ChunkPrimer();
        rand.setSeed((long)x * 341873128712L + (long)z * 132897987541L + seed);

        // Simple flat-ish frozen terrain
        for (int bx = 0; bx < 16; bx++) {
            for (int bz = 0; bz < 16; bz++) {
                int worldX = x * 16 + bx;
                int worldZ = z * 16 + bz;

                // Bedrock at 0
                primer.setBlockState(bx, 0, bz, Blocks.BEDROCK.getDefaultState());

                // Stone from 1 to ~58
                for (int y = 1; y < 58; y++) {
                    primer.setBlockState(bx, y, bz, Blocks.STONE.getDefaultState());
                }

                // Dirt/gravel layer
                for (int y = 58; y < 62; y++) {
                    primer.setBlockState(bx, y, bz,
                            rand.nextFloat() < 0.3 ? Blocks.GRAVEL.getDefaultState()
                                    : Blocks.DIRT.getDefaultState());
                }

                // Surface: frozen ground
                int surfaceHeight = 62 + getHeightOffset(worldX, worldZ);
                primer.setBlockState(bx, surfaceHeight, bz, Blocks.GRASS.getDefaultState());

                // Snow layer on top
                if (surfaceHeight < 255) {
                    primer.setBlockState(bx, surfaceHeight + 1, bz, Blocks.SNOW_LAYER.getDefaultState());
                }

                // Ice patches on low areas
                if (surfaceHeight <= SEA_LEVEL && rand.nextFloat() < 0.4F) {
                    primer.setBlockState(bx, surfaceHeight, bz, Blocks.ICE.getDefaultState());
                }
            }
        }

        Chunk chunk = new Chunk(world, primer, x, z);
        return chunk;
    }

    private int getHeightOffset(int x, int z) {
        // Simple smooth noise approximation
        double noise = Math.sin(x * 0.05) * Math.cos(z * 0.05) * 4;
        noise += Math.sin(x * 0.02 + 1.5) * Math.cos(z * 0.02 - 0.8) * 8;
        return (int) noise;
    }

    @Override
    public void populate(int x, int z) {
        // Place some trees in taiga zone (500-2000 blocks from origin)
        int wx = x * 16, wz = z * 16;
        double distFromOrigin = Math.sqrt(wx * (double)wx + wz * (double)wz);
        if (distFromOrigin > 500 && distFromOrigin < 2000) {
            // Sparse spruce trees
            if (rand.nextInt(10) < 2) {
                int tx = wx + rand.nextInt(16);
                int tz = wz + rand.nextInt(16);
                BlockPos surface = world.getTopSolidOrLiquidBlock(new BlockPos(tx, 0, tz));
                if (world.getBlockState(surface.down()).getBlock() == Blocks.GRASS
                        || world.getBlockState(surface.down()).getBlock() == Blocks.DIRT) {
                    generateSpruceTree(world, surface);
                }
            }
        }

        // Place minefields in buffer zone
        if (distFromOrigin > 200 && distFromOrigin < 500) {
            MineFieldGenerator.populate(world, x, z, rand);
        }
    }

    private void generateSpruceTree(World world, BlockPos base) {
        int height = 5 + rand.nextInt(4);
        for (int y = 0; y < height; y++) {
            world.setBlockState(base.up(y), Blocks.LOG.getDefaultState(), 2);
        }
        // Leaves
        for (int lx = -2; lx <= 2; lx++) {
            for (int lz = -2; lz <= 2; lz++) {
                for (int ly = height - 3; ly <= height; ly++) {
                    if (Math.abs(lx) + Math.abs(lz) + (height - ly) <= 3) {
                        BlockPos leafPos = base.up(ly).add(lx, 0, lz);
                        if (world.isAirBlock(leafPos)) {
                            world.setBlockState(leafPos, Blocks.LEAVES.getDefaultState(), 2);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean generateStructures(Chunk chunk, int x, int z) { return false; }

    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType type, BlockPos pos) {
        return Collections.emptyList();
    }

    @Nullable
    @Override
    public BlockPos getStrongholdGen(World world, String name, BlockPos pos, boolean approx) {
        return null;
    }

    @Override
    public void recreateStructures(Chunk chunk, int x, int z) {}
}
