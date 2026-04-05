package com.prisonbreakmod.crafting;

import com.prisonbreakmod.PrisonBreakMod;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages active thermite burns.
 * Thermite requires 5 uninterrupted minutes (6000 ticks) to burn through an obsidian block.
 */
@Mod.EventBusSubscriber(modid = PrisonBreakMod.MODID)
public class ThermiteHandler {

    private static final int BURN_DURATION = 6000; // 5 min = 6000 ticks

    // pos -> BurnEntry
    private static final ConcurrentHashMap<BlockPos, BurnEntry> activeBurns = new ConcurrentHashMap<>();

    private static class BurnEntry {
        final long startTick;
        final World world;
        int lastPlayerCheck;

        BurnEntry(long startTick, World world) {
            this.startTick = startTick;
            this.world = world;
            this.lastPlayerCheck = 0;
        }
    }

    public static boolean startBurning(World world, BlockPos pos, ItemStack thermite) {
        if (activeBurns.containsKey(pos)) return false; // already burning
        if (world.isAirBlock(pos)) return false;
        activeBurns.put(pos, new BurnEntry(world.getTotalWorldTime(), world));
        PrisonBreakMod.LOGGER.info("Thermite started at {}", pos);
        return true;
    }

    public static boolean isActiveAt(BlockPos pos) {
        return activeBurns.containsKey(pos);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        net.minecraft.server.MinecraftServer server =
                net.minecraftforge.fml.common.FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null) return;
        World world = server.getEntityWorld();
        long now = world.getTotalWorldTime();

        Iterator<Map.Entry<BlockPos, BurnEntry>> it = activeBurns.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<BlockPos, BurnEntry> entry = it.next();
            BlockPos pos = entry.getKey();
            BurnEntry burn = entry.getValue();
            long elapsed = now - burn.startTick;

            // Check for interruption: camera or guard within range
            if (now % 40 == 0) {
                boolean interrupted = checkInterruption(world, pos);
                if (interrupted) {
                    it.remove();
                    PrisonBreakMod.LOGGER.info("Thermite interrupted at {}", pos);
                    continue;
                }
            }

            // Spawn flame particles
            if (world.isRemote) {
                world.spawnParticle(EnumParticleTypes.FLAME,
                        pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5,
                        0.1, 0.1, 0.1, 0.05);
            }

            // Done burning
            if (elapsed >= BURN_DURATION) {
                it.remove();
                world.setBlockToAir(pos);
                PrisonBreakMod.LOGGER.info("Thermite completed — block destroyed at {}", pos);
            }
        }
    }

    private static boolean checkInterruption(World world, BlockPos pos) {
        // Check for players being camera-visible or guard in range 10 blocks
        java.util.List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class,
                new net.minecraft.util.math.AxisAlignedBB(pos).grow(10));
        if (!players.isEmpty()) {
            // Check if any camera or guard is watching
            java.util.List<com.prisonbreakmod.entity.EntityGuard> guards =
                    world.getEntitiesWithinAABB(com.prisonbreakmod.entity.EntityGuard.class,
                            new net.minecraft.util.math.AxisAlignedBB(pos).grow(15));
            return !guards.isEmpty();
        }
        return false;
    }
}
