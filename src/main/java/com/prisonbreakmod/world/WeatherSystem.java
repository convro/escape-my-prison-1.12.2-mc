package com.prisonbreakmod.world;

import com.prisonbreakmod.ai.SharedPrisonState;
import com.prisonbreakmod.config.PrisonConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages temperature for each player, blizzard events, and cold damage.
 * Called every server tick from PrisonEventHandler.
 */
public class WeatherSystem {

    private static WeatherSystem instance;

    private boolean blizzardActive = false;
    private long blizzardEndTick = -1;
    private long nextBlizzardCheckTick = 0;

    // Per-player temperature (0-100)
    private final Map<UUID, Integer> playerTemp = new HashMap<>();

    // Constants
    private static final int MAX_TEMP = 100;
    private static final int DAMAGE_THRESHOLD = 20;
    private static final long BLIZZARD_CHECK_INTERVAL = 72000L; // every hour
    private static final int BLIZZARD_MIN_TICKS = 6000;  // 5 min
    private static final int BLIZZARD_MAX_TICKS = 18000; // 15 min
    private static final int OUTDOOR_DRAIN = 1; // per 200 ticks = every 10s
    private static final int BLIZZARD_DRAIN_MULT = 3;
    private static final int NIGHT_DRAIN = 1; // extra at night

    public static WeatherSystem getInstance() {
        if (instance == null) instance = new WeatherSystem();
        return instance;
    }

    public void tick(World world) {
        long time = world.getTotalWorldTime();

        // Blizzard logic
        if (!blizzardActive && time >= nextBlizzardCheckTick) {
            nextBlizzardCheckTick = time + BLIZZARD_CHECK_INTERVAL;
            int chance = PrisonConfig.blizzardChancePerHour;
            if (world.rand.nextInt(100) < chance) {
                startBlizzard(world);
            }
        }
        if (blizzardActive && time >= blizzardEndTick) {
            endBlizzard(world);
        }

        // Temperature update every 200 ticks (10s)
        if (time % 200 != 0) return;

        for (EntityPlayer player : world.playerEntities) {
            updatePlayerTemp(world, player, time);
        }

        SharedPrisonState.getInstance().setBlizzard(blizzardActive);
    }

    private void updatePlayerTemp(World world, EntityPlayer player, long time) {
        int temp = getTemperature(player);
        boolean isOutdoor = isOutdoor(world, player);
        boolean isNight = world.getWorldTime() % 24000 > 13000;

        int drain = 0;
        if (isOutdoor) {
            drain += OUTDOOR_DRAIN;
            if (blizzardActive) drain += OUTDOOR_DRAIN * (BLIZZARD_DRAIN_MULT - 1);
            if (isNight) drain += NIGHT_DRAIN;
        }

        // Armor bonus
        drain -= getArmorTempBonus(player) / 10;
        drain = Math.max(0, drain);
        temp = Math.max(0, Math.min(MAX_TEMP, temp - drain));
        setTemperature(player, temp);

        // Damage below threshold
        if (temp < DAMAGE_THRESHOLD) {
            player.attackEntityFrom(net.minecraft.util.DamageSource.FREEZE, 0.5F);
        }

        // Blizzard visibility effect
        if (blizzardActive && isOutdoor) {
            player.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 100, 0, false, false));
        }

        // Fatigue from long sprinting
        // (tracked separately in PrisonEventHandler via sprint time)
    }

    private boolean isOutdoor(World world, EntityPlayer player) {
        return world.canBlockSeeSky(player.getPosition());
    }

    private int getArmorTempBonus(EntityPlayer player) {
        int bonus = 0;
        for (net.minecraft.inventory.EntityEquipmentSlot slot : new net.minecraft.inventory.EntityEquipmentSlot[]{
                net.minecraft.inventory.EntityEquipmentSlot.HEAD,
                net.minecraft.inventory.EntityEquipmentSlot.CHEST,
                net.minecraft.inventory.EntityEquipmentSlot.LEGS,
                net.minecraft.inventory.EntityEquipmentSlot.FEET}) {
            net.minecraft.item.ItemStack armor = player.getItemStackFromSlot(slot);
            if (!armor.isEmpty()) {
                if (armor.getItem() instanceof com.prisonbreakmod.items.survival.ItemSurvivalSuit) {
                    bonus += com.prisonbreakmod.items.survival.ItemSurvivalSuit.TEMP_BONUS;
                } else if (armor.getItem() instanceof com.prisonbreakmod.items.survival.ItemWarmJacket) {
                    bonus += com.prisonbreakmod.items.survival.ItemWarmJacket.TEMP_BONUS;
                }
            }
        }
        return bonus;
    }

    public void adjustTemperature(EntityPlayer player, int delta) {
        int current = getTemperature(player);
        setTemperature(player, Math.max(0, Math.min(MAX_TEMP, current + delta)));
    }

    public int getTemperature(EntityPlayer player) {
        return playerTemp.getOrDefault(player.getUniqueID(), 70);
    }

    public void setTemperature(EntityPlayer player, int temp) {
        playerTemp.put(player.getUniqueID(), Math.max(0, Math.min(MAX_TEMP, temp)));
    }

    private void startBlizzard(World world) {
        blizzardActive = true;
        int duration = BLIZZARD_MIN_TICKS + world.rand.nextInt(BLIZZARD_MAX_TICKS - BLIZZARD_MIN_TICKS);
        blizzardEndTick = world.getTotalWorldTime() + duration;
        SharedPrisonState.getInstance().addEvent("BLIZZARD_START");
        // Vanilla rain for visual
        world.getWorldInfo().setRaining(true);
        world.getWorldInfo().setThundering(false);
    }

    private void endBlizzard(World world) {
        blizzardActive = false;
        blizzardEndTick = -1;
        SharedPrisonState.getInstance().addEvent("BLIZZARD_END");
        world.getWorldInfo().setRaining(false);
    }

    public boolean isBlizzard() { return blizzardActive; }

    public void loadState(boolean blizzard, long endTick) {
        this.blizzardActive = blizzard;
        this.blizzardEndTick = endTick;
    }
}
