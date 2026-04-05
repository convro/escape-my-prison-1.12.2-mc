package com.prisonbreakmod.world;

import com.prisonbreakmod.blocks.ModBlocks;
import com.prisonbreakmod.crafting.ThermiteHandler;
import com.prisonbreakmod.events.AlertSystem;
import com.prisonbreakmod.items.survival.ItemMasterKey;
import com.prisonbreakmod.items.tools.ItemPicklock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockObsidian;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber
public class BlockProtectionSystem {

    public enum ProtectionCategory {
        PROTECTED_ABSOLUTE,  // Obsidian mur — nothing destroys it without thermite
        PROTECTED_STAFF,     // Admin doors, panels — guards only
        PROTECTED_CAMERA,    // Cameras, alarms — EMP or sabotage only
        RESTRICTED_PLAYER,   // Bars/doors — picklock or master key
        OPEN                 // Normal blocks
    }

    // Pre-defined protected zone sets (populated by SchematicLoader)
    private static final Set<BlockPos> absoluteProtected = new HashSet<>();
    private static final Set<BlockPos> staffProtected = new HashSet<>();
    private static final Set<BlockPos> cameraProtected = new HashSet<>();
    private static final Set<BlockPos> restrictedPlayer = new HashSet<>();

    public static void markAbsolute(BlockPos pos) { absoluteProtected.add(pos); }
    public static void markStaff(BlockPos pos) { staffProtected.add(pos); }
    public static void markCamera(BlockPos pos) { cameraProtected.add(pos); }
    public static void markRestricted(BlockPos pos) { restrictedPlayer.add(pos); }

    public static ProtectionCategory getCategory(World world, BlockPos pos) {
        if (absoluteProtected.contains(pos)) return ProtectionCategory.PROTECTED_ABSOLUTE;
        Block block = world.getBlockState(pos).getBlock();
        // Auto-detect obsidian as absolute
        if (block instanceof BlockObsidian) return ProtectionCategory.PROTECTED_ABSOLUTE;
        if (staffProtected.contains(pos)) return ProtectionCategory.PROTECTED_STAFF;
        if (cameraProtected.contains(pos)) return ProtectionCategory.PROTECTED_CAMERA;
        if (block == ModBlocks.CAMERA || block == ModBlocks.ALARM_PANEL)
            return ProtectionCategory.PROTECTED_CAMERA;
        if (block == ModBlocks.BARRED_DOOR) return ProtectionCategory.RESTRICTED_PLAYER;
        if (restrictedPlayer.contains(pos)) return ProtectionCategory.RESTRICTED_PLAYER;
        return ProtectionCategory.OPEN;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        EntityPlayer player = event.getPlayer();
        if (player instanceof FakePlayer) return; // Guards can break

        BlockPos pos = event.getPos();
        World world = (World) event.getWorld();
        ProtectionCategory cat = getCategory(world, pos);

        switch (cat) {
            case PROTECTED_ABSOLUTE:
                if (!ThermiteHandler.isActiveAt(pos)) {
                    event.setCanceled(true);
                }
                break;
            case PROTECTED_STAFF:
                event.setCanceled(true);
                AlertSystem.raise(world, 1, "Admin block break attempt at " + pos);
                break;
            case PROTECTED_CAMERA:
                event.setCanceled(true);
                AlertSystem.raise(world, 2, "Camera destruction attempt at " + pos);
                break;
            case RESTRICTED_PLAYER:
                ItemStack held = player.getHeldItemMainhand();
                if (!ItemPicklock.isPicklockOfTier(held, 1) && !ItemMasterKey.isMasterKey(held)) {
                    event.setCanceled(true);
                    AlertSystem.raise(world, 1, "Forced entry attempt at " + pos);
                }
                break;
            case OPEN:
            default:
                break;
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBlockPlace(BlockEvent.PlaceEvent event) {
        EntityPlayer player = event.getPlayer();
        if (player instanceof FakePlayer) return;
        // Prevent placing blocks inside the prison complex walls (simplified zone check)
        // Full implementation would check zone boundaries
    }

    /** Returns true if player is allowed to be in a particular camera zone. */
    public static boolean isPlayerAllowedInZone(EntityPlayer player, int zone) {
        // Zone 0 = general (always allowed during work hours)
        // Zone 1 = admin (never allowed)
        // Zone 2 = buffer (not allowed unless guard)
        // Zone 3 = tower (never allowed)
        if (zone == 0) return true;
        // Disguise check
        ItemStack chestItem = player.getItemStackFromSlot(net.minecraft.inventory.EntityEquipmentSlot.CHEST);
        if (!chestItem.isEmpty() &&
                chestItem.getItem() instanceof com.prisonbreakmod.items.tools.ItemGuardDisguise &&
                com.prisonbreakmod.items.tools.ItemGuardDisguise.isWorn(chestItem)) {
            return zone <= 1;
        }
        return false;
    }
}
