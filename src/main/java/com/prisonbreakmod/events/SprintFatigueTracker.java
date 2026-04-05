package com.prisonbreakmod.events;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Tracks player sprint time; applies Slowness II after 20 min continuous sprinting. */
public class SprintFatigueTracker {

    private static final Map<UUID, Integer> sprintTicks = new HashMap<>();
    private static final int FATIGUE_THRESHOLD = 24000; // 20 min = 24000 ticks
    private static final int RECOVERY_TICKS = 2400; // 2 min recovery

    public static void tick(EntityPlayer player) {
        UUID id = player.getUniqueID();
        if (player.isSprinting()) {
            int ticks = sprintTicks.getOrDefault(id, 0) + 1;
            sprintTicks.put(id, ticks);
            if (ticks >= FATIGUE_THRESHOLD) {
                player.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, RECOVERY_TICKS, 1));
                sprintTicks.put(id, 0);
            }
        } else {
            // Reset slowly when not sprinting
            int ticks = sprintTicks.getOrDefault(id, 0);
            if (ticks > 0) sprintTicks.put(id, Math.max(0, ticks - 2));
        }
    }
}
