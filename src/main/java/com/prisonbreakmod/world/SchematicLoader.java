package com.prisonbreakmod.world;

import com.prisonbreakmod.PrisonBreakMod;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.io.InputStream;

/**
 * Loads MCEdit .schematic files and pastes them into the world at a given origin.
 * Falls back to a generated placeholder if no schematic is found.
 */
public class SchematicLoader {

    /**
     * Load and paste a .schematic from the mod's resources/schematics/ directory.
     * Falls back to procedural generation if file not found.
     */
    public static void loadSchematic(World world, String filename, BlockPos origin) {
        try {
            InputStream is = SchematicLoader.class.getResourceAsStream(
                    "/schematics/" + filename);
            if (is == null) {
                PrisonBreakMod.LOGGER.warn("Schematic {} not found, using procedural fallback", filename);
                if (filename.contains("prison")) {
                    generatePrisonPlaceholder(world, origin);
                } else if (filename.contains("port")) {
                    generatePortPlaceholder(world, origin);
                }
                return;
            }

            NBTTagCompound nbt = CompressedStreamTools.readCompressed(is);
            short width = nbt.getShort("Width");
            short height = nbt.getShort("Height");
            short length = nbt.getShort("Length");
            byte[] blockIds = nbt.getByteArray("Blocks");
            byte[] blockData = nbt.getByteArray("Data");

            PrisonBreakMod.LOGGER.info("Loading schematic {} ({}x{}x{})", filename, width, height, length);

            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    for (int x = 0; x < width; x++) {
                        int index = y * width * length + z * width + x;
                        int blockId = blockIds[index] & 0xFF;
                        int meta = blockData[index] & 0xFF;
                        BlockPos pos = origin.add(x, y, z);
                        // Use legacy block IDs (MC 1.12.2 supports this)
                        IBlockState state = net.minecraft.block.Block.getStateById((blockId << 4) | meta);
                        world.setBlockState(pos, state, 2);
                    }
                }
            }
            is.close();
            PrisonBreakMod.LOGGER.info("Schematic {} loaded successfully.", filename);
        } catch (Exception e) {
            PrisonBreakMod.LOGGER.error("Error loading schematic {}: {}", filename, e.getMessage());
            if (filename.contains("prison")) generatePrisonPlaceholder(world, origin);
        }
    }

    /** Simple 200x200 prison placeholder with walls. */
    private static void generatePrisonPlaceholder(World world, BlockPos origin) {
        int w = 200, l = 200, h = 8;
        int ox = origin.getX(), oz = origin.getZ(), oy = origin.getY();

        // Obsidian walls (4 thick, 8 high)
        for (int y = 0; y < h; y++) {
            for (int i = 0; i < w; i++) {
                for (int t = 0; t < 4; t++) {
                    // North wall
                    world.setBlockState(new BlockPos(ox + i, oy + y, oz + t),
                            Blocks.OBSIDIAN.getDefaultState(), 2);
                    // South wall
                    world.setBlockState(new BlockPos(ox + i, oy + y, oz + l - 1 - t),
                            Blocks.OBSIDIAN.getDefaultState(), 2);
                    // West wall
                    world.setBlockState(new BlockPos(ox + t, oy + y, oz + i),
                            Blocks.OBSIDIAN.getDefaultState(), 2);
                    // East wall
                    world.setBlockState(new BlockPos(ox + w - 1 - t, oy + y, oz + i),
                            Blocks.OBSIDIAN.getDefaultState(), 2);
                }
            }
        }

        // Interior floor
        for (int x = 4; x < w - 4; x++) {
            for (int z = 4; z < l - 4; z++) {
                world.setBlockState(new BlockPos(ox + x, oy, oz + z),
                        Blocks.COBBLESTONE.getDefaultState(), 2);
            }
        }

        // Mark all obsidian as absolute protected
        for (int y = 0; y < h; y++) {
            for (int i = 0; i < w; i++) {
                for (int t = 0; t < 4; t++) {
                    BlockProtectionSystem.markAbsolute(new BlockPos(ox + i, oy + y, oz + t));
                    BlockProtectionSystem.markAbsolute(new BlockPos(ox + i, oy + y, oz + l - 1 - t));
                    BlockProtectionSystem.markAbsolute(new BlockPos(ox + t, oy + y, oz + i));
                    BlockProtectionSystem.markAbsolute(new BlockPos(ox + w - 1 - t, oy + y, oz + i));
                }
            }
        }

        PrisonBreakMod.LOGGER.info("Prison placeholder generated at {}", origin);
    }

    /** Port placeholder 100x100. */
    private static void generatePortPlaceholder(World world, BlockPos origin) {
        int ox = origin.getX(), oz = origin.getZ(), oy = origin.getY();
        for (int x = 0; x < 100; x++) {
            for (int z = 0; z < 100; z++) {
                world.setBlockState(new BlockPos(ox + x, oy, oz + z),
                        Blocks.PLANKS.getDefaultState(), 2);
            }
        }
        // Dock marker
        world.setBlockState(new BlockPos(ox + 50, oy + 1, oz + 50),
                Blocks.BEACON.getDefaultState(), 2);
        PrisonBreakMod.LOGGER.info("Port placeholder generated at {}", origin);
    }
}
