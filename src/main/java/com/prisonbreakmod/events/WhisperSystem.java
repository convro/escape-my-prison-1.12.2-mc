package com.prisonbreakmod.events;

import com.prisonbreakmod.ai.SharedPrisonState;
import com.prisonbreakmod.entity.EntityPrisoner;
import com.prisonbreakmod.entity.EntityGuard;
import com.prisonbreakmod.items.misc.ItemNoteSimple;
import com.prisonbreakmod.items.misc.ItemNoteCoded;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.List;

/**
 * Manages proximity-based whisper communication.
 * 5 blocks inside prison, 25 outside, 40 in wilderness.
 */
public class WhisperSystem {

    /** Maximum whisper range inside prison (blocks). */
    public static final double RANGE_INSIDE = 5.0;
    /** Range outside prison but within gulag zone. */
    public static final double RANGE_BUFFER = 25.0;
    /** Range in open wilderness. */
    public static final double RANGE_WILD = 40.0;

    /** 30% chance guards overhear a whisper if within 8 blocks. */
    private static final double GUARD_OVERHEAR_RANGE = 8.0;
    private static final float GUARD_OVERHEAR_CHANCE = 0.30F;

    /** Returns whisper range for the given player's position. */
    public static double getWhisperRange(EntityPlayer player) {
        double distFromOrigin = Math.sqrt(
                player.posX * player.posX + player.posZ * player.posZ);
        if (distFromOrigin < 200) return RANGE_INSIDE;
        if (distFromOrigin < 500) return RANGE_BUFFER;
        return RANGE_WILD;
    }

    /**
     * Try to initiate a whisper dialogue with nearest prisoner within range.
     * @return true if a target was found and dialogue started.
     */
    public static boolean tryWhisper(EntityPlayer player, String message) {
        World world = player.world;
        double range = getWhisperRange(player);

        // Find nearest prisoner
        List<EntityPrisoner> nearby = world.getEntitiesWithinAABB(EntityPrisoner.class,
                new AxisAlignedBB(player.posX - range, player.posY - 2, player.posZ - range,
                        player.posX + range, player.posY + 2, player.posZ + range));

        if (nearby.isEmpty()) return false;

        EntityPrisoner target = nearest(nearby, player);
        if (target == null) return false;

        // Check if guard can overhear
        boolean overheard = checkGuardOverhear(world, player);
        if (overheard) {
            AlertSystem.raise(world, 1, "Guard overheard whisper near " + player.getName());
        }

        // Open dialogue GUI on client
        if (world.isRemote) {
            net.minecraft.client.Minecraft.getMinecraft().displayGuiScreen(
                    new com.prisonbreakmod.gui.GuiDialogue(player, target, message));
        }
        return true;
    }

    /**
     * Try to deliver a note item to nearest NPC.
     */
    public static boolean tryDeliverNote(EntityPlayer player, ItemStack noteStack) {
        if (noteStack.isEmpty()) return false;
        World world = player.world;
        double range = getWhisperRange(player) + 5; // notes have slightly longer range

        List<EntityPrisoner> nearby = world.getEntitiesWithinAABB(EntityPrisoner.class,
                new AxisAlignedBB(player.posX - range, player.posY - 2, player.posZ - range,
                        player.posX + range, player.posY + 2, player.posZ + range));

        EntityPrisoner target = nearest(nearby, player);
        if (target == null) {
            player.sendStatusMessage(
                    new net.minecraft.util.text.TextComponentString("§cNikt w pobliżu."), true);
            return false;
        }

        // Interception chance
        float interceptChance = (noteStack.getItem() instanceof ItemNoteCoded)
                ? ItemNoteCoded.INTERCEPT_CHANCE : ItemNoteSimple.INTERCEPT_CHANCE;

        if (world.rand.nextFloat() < interceptChance) {
            // Intercepted
            AlertSystem.raise(world, 1, "Note intercepted near " + target.getName());
            player.sendStatusMessage(
                    new net.minecraft.util.text.TextComponentString("§cGryps przechwycony!"), true);
            noteStack.shrink(1);
            return false;
        }

        String msg = ItemNoteSimple.getMessage(noteStack);
        target.memory.addMemory("Gryps od " + player.getName() + ": " + msg);
        SharedPrisonState.getInstance().addEvent("NOTE_DELIVERED:" + target.getNpcId());
        player.sendStatusMessage(
                new net.minecraft.util.text.TextComponentString("§7Dostarczyłeś gryps."), true);
        noteStack.shrink(1);
        return true;
    }

    private static boolean checkGuardOverhear(World world, EntityPlayer player) {
        List<EntityGuard> guards = world.getEntitiesWithinAABB(EntityGuard.class,
                new AxisAlignedBB(
                        player.posX - GUARD_OVERHEAR_RANGE, player.posY - 2,
                        player.posZ - GUARD_OVERHEAR_RANGE,
                        player.posX + GUARD_OVERHEAR_RANGE, player.posY + 2,
                        player.posZ + GUARD_OVERHEAR_RANGE));
        if (guards.isEmpty()) return false;
        return world.rand.nextFloat() < GUARD_OVERHEAR_CHANCE;
    }

    private static <T extends Entity> T nearest(List<T> entities, EntityPlayer player) {
        T best = null;
        double bestDist = Double.MAX_VALUE;
        for (T e : entities) {
            double d = e.getDistanceTo(player);
            if (d < bestDist) { bestDist = d; best = e; }
        }
        return best;
    }
}
