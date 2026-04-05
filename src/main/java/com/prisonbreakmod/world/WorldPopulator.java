package com.prisonbreakmod.world;

import com.prisonbreakmod.PrisonBreakMod;
import com.prisonbreakmod.entity.EntityAICompanion;
import com.prisonbreakmod.entity.guards.GuardFactory;
import com.prisonbreakmod.entity.prisoners.PrisonerFactory;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Spawns the full NPC population (20 guards + 50 prisoners + 2 companions)
 * exactly once — on the first server-side world load for world dimension 0.
 *
 * <p>A marker flag {@code PrisonPopulated} is stored in world saved-data so the
 * population is not repeated on subsequent world loads.
 */
@Mod.EventBusSubscriber(modid = PrisonBreakMod.MODID)
public class WorldPopulator {

    // -----------------------------------------------------------------------
    // Guard spawn positions — 20 named positions inside / around the prison
    // Coordinates are relative to the prison schematic origin (0, 64, 0).
    // -----------------------------------------------------------------------
    private static final int[][] GUARD_POSITIONS = {
        /* S01 Kapitan Wiśniewski  */ { 10,  65,  10},
        /* S02 Waldemar Gruba      */ { 20,  65,  10},
        /* S03 Aneta Kowalczyk     */ { 30,  65,  10},
        /* S04 Ryszard Brudny      */ { 40,  65,  10},
        /* S05 Paweł Łaskawy       */ { 50,  65,  10},
        /* S06 Mirka               */ { 60,  65,  10},
        /* S07 Siemion Grubas      */ { 70,  65,  10},
        /* S08 Jegor               */ { 80,  65,  10},
        /* S09 Taras               */ { 90,  65,  10},
        /* S10 Iwan Pijak          */ {100,  65,  10},
        /* S11 Komisarz            */ { 10,  65,  30},
        /* S12 Doktor              */ { 20,  65,  30},
        /* S13 Snajper             */ { 30,  65,  30},
        /* S14 Techniczny          */ { 40,  65,  30},
        /* S15 Zdzisław Śpioch     */ { 50,  65,  30},
        /* S16 Kacap               */ { 60,  65,  30},
        /* S17 Kirow               */ { 70,  65,  30},
        /* S18 Anastazja           */ { 80,  65,  30},
        /* S19 Oleg                */ { 90,  65,  30},
        /* S20 Generał Nadzorca    */ {100,  65,  30},
    };

    // -----------------------------------------------------------------------
    // Prisoner spawn positions — 50 positions in barracks A-E
    // -----------------------------------------------------------------------
    private static final int[][] PRISONER_POSITIONS;
    static {
        PRISONER_POSITIONS = new int[50][3];
        // Blok A (W01-W10): x=10–100, z=60
        for (int i = 0; i < 10; i++) {
            PRISONER_POSITIONS[i][0] = 10 + i * 10;
            PRISONER_POSITIONS[i][1] = 65;
            PRISONER_POSITIONS[i][2] = 60;
        }
        // Blok B (W11-W20): x=10-100, z=80
        for (int i = 0; i < 10; i++) {
            PRISONER_POSITIONS[10 + i][0] = 10 + i * 10;
            PRISONER_POSITIONS[10 + i][1] = 65;
            PRISONER_POSITIONS[10 + i][2] = 80;
        }
        // Blok C (W21-W30): x=10-100, z=100
        for (int i = 0; i < 10; i++) {
            PRISONER_POSITIONS[20 + i][0] = 10 + i * 10;
            PRISONER_POSITIONS[20 + i][1] = 65;
            PRISONER_POSITIONS[20 + i][2] = 100;
        }
        // Blok D (W31-W40): x=10-100, z=120
        for (int i = 0; i < 10; i++) {
            PRISONER_POSITIONS[30 + i][0] = 10 + i * 10;
            PRISONER_POSITIONS[30 + i][1] = 65;
            PRISONER_POSITIONS[30 + i][2] = 120;
        }
        // Blok E (W41-W50): x=10-100, z=140
        for (int i = 0; i < 10; i++) {
            PRISONER_POSITIONS[40 + i][0] = 10 + i * 10;
            PRISONER_POSITIONS[40 + i][1] = 65;
            PRISONER_POSITIONS[40 + i][2] = 140;
        }
    }

    // Companion positions
    private static final int[] MAREK_POS   = { 55, 65, 110 };
    private static final int[] ZBYSZEK_POS = { 65, 65, 110 };

    // NBT key used to flag the world as already populated
    private static final String POPULATED_KEY = "PrisonPopulated";

    // -----------------------------------------------------------------------
    // Event handler
    // -----------------------------------------------------------------------

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        World world = event.getWorld();
        // Server side, overworld only
        if (world.isRemote || world.provider.getDimension() != 0) return;

        NBTTagCompound worldData = world.getWorldInfo().getAdditionalProperties();
        if (worldData == null) worldData = new NBTTagCompound();

        if (worldData.getBoolean(POPULATED_KEY)) {
            PrisonBreakMod.LOGGER.info("[WorldPopulator] Already populated, skipping.");
            return;
        }

        PrisonBreakMod.LOGGER.info("[WorldPopulator] First load — spawning NPC population...");
        populate(world);

        worldData.setBoolean(POPULATED_KEY, true);
        // WorldInfo additional properties are saved automatically with the world
    }

    // -----------------------------------------------------------------------
    // Population
    // -----------------------------------------------------------------------

    private static void populate(World world) {
        int guards    = spawnGuards(world);
        int prisoners = spawnPrisoners(world);
        int companions = spawnCompanions(world);
        PrisonBreakMod.LOGGER.info("[WorldPopulator] Spawned {} guards, {} prisoners, {} companions.",
                guards, prisoners, companions);
    }

    private static int spawnGuards(World world) {
        int count = 0;
        for (int i = 1; i <= 20; i++) {
            try {
                int[] pos = GUARD_POSITIONS[i - 1];
                Entity guard = GuardFactory.createGuard(world, i);
                if (guard != null) {
                    guard.setPosition(pos[0], pos[1], pos[2]);
                    world.spawnEntity(guard);
                    count++;
                }
            } catch (Exception e) {
                PrisonBreakMod.LOGGER.warn("[WorldPopulator] Failed to spawn guard {}: {}", i, e.getMessage());
            }
        }
        return count;
    }

    private static int spawnPrisoners(World world) {
        int count = 0;
        for (int i = 1; i <= 50; i++) {
            try {
                int[] pos = PRISONER_POSITIONS[i - 1];
                Entity prisoner = PrisonerFactory.create(world, i);
                if (prisoner != null) {
                    prisoner.setPosition(pos[0], pos[1], pos[2]);
                    world.spawnEntity(prisoner);
                    count++;
                }
            } catch (Exception e) {
                PrisonBreakMod.LOGGER.warn("[WorldPopulator] Failed to spawn prisoner {}: {}", i, e.getMessage());
            }
        }
        return count;
    }

    private static int spawnCompanions(World world) {
        int count = 0;
        try {
            EntityAICompanion marek = new EntityAICompanion(world, EntityAICompanion.CompanionType.MAREK);
            marek.setPosition(MAREK_POS[0], MAREK_POS[1], MAREK_POS[2]);
            world.spawnEntity(marek);
            count++;
        } catch (Exception e) {
            PrisonBreakMod.LOGGER.warn("[WorldPopulator] Failed to spawn Marek: {}", e.getMessage());
        }
        try {
            EntityAICompanion zbyszek = new EntityAICompanion(world, EntityAICompanion.CompanionType.ZBYSZEK);
            zbyszek.setPosition(ZBYSZEK_POS[0], ZBYSZEK_POS[1], ZBYSZEK_POS[2]);
            world.spawnEntity(zbyszek);
            count++;
        } catch (Exception e) {
            PrisonBreakMod.LOGGER.warn("[WorldPopulator] Failed to spawn Zbyszek: {}", e.getMessage());
        }
        return count;
    }
}
