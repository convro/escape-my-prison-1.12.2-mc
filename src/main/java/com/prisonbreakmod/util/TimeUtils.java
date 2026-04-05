package com.prisonbreakmod.util;

import net.minecraftforge.fml.common.FMLCommonHandler;

/** Utility to get human-readable game time strings. */
public class TimeUtils {

    /**
     * Returns game time as "HH:MM" string based on MC world time.
     * MC time 0 = 6:00, 6000 = 12:00, 12000 = 18:00, 18000 = 0:00
     */
    public static String getGameTimeStr() {
        net.minecraft.server.MinecraftServer server =
                FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null) return "??:??";
        long worldTime = server.getEntityWorld().getWorldTime() % 24000;
        int totalMinutes = (int)((worldTime / 24000.0) * 1440);
        int gameHour = (totalMinutes / 60 + 6) % 24;
        int gameMin = totalMinutes % 60;
        return String.format("%02d:%02d", gameHour, gameMin);
    }

    /** Returns current day number. */
    public static int getGameDay() {
        net.minecraft.server.MinecraftServer server =
                FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null) return 0;
        return (int)(server.getEntityWorld().getTotalWorldTime() / 24000);
    }

    /** Convert game hour (0-23) to MC world tick (0-23999). */
    public static int hourToTick(int hour) {
        int adjusted = ((hour - 6) + 24) % 24;
        return adjusted * 1000;
    }

    /** Returns true if it's currently nighttime in MC (ticks 13000-23999). */
    public static boolean isNight() {
        net.minecraft.server.MinecraftServer server =
                FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null) return false;
        long dayTick = server.getEntityWorld().getWorldTime() % 24000;
        return dayTick >= 13000;
    }
}
