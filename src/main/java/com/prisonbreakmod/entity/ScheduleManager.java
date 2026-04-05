package com.prisonbreakmod.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages NPC schedules. Game time mapping:
 * 24000 ticks = 1 MC day = 20 real minutes (per config).
 * MC time 0 = 6:00, 6000 = 12:00, 12000 = 18:00, 18000 = 0:00 (midnight).
 */
public class ScheduleManager {

    private static ScheduleManager instance;
    private final Map<Integer, List<ScheduleEntry>> guardSchedules = new HashMap<>();
    private final Map<Integer, String> currentAssignments = new HashMap<>();

    public static class ScheduleEntry {
        public final int startTick; // 0-23999
        public final int endTick;
        public final String location;
        public final String activity;

        public ScheduleEntry(int startHour, int endHour, String location, String activity) {
            // MC time: hour 6 = tick 0, so shift by -6h
            this.startTick = hourToTick(startHour);
            this.endTick = hourToTick(endHour);
            this.location = location;
            this.activity = activity;
        }

        private static int hourToTick(int hour) {
            // MC 0 = 6:00 real, so real 0:00 = tick 18000
            int adjusted = ((hour - 6) + 24) % 24;
            return (adjusted * 1000);
        }
    }

    public static ScheduleManager getInstance() {
        if (instance == null) {
            instance = new ScheduleManager();
            instance.initSchedules();
        }
        return instance;
    }

    public void initSchedules() {
        // S01 - Wiśniewski
        List<ScheduleEntry> s01 = new ArrayList<>();
        s01.add(new ScheduleEntry(6, 7, "biuro", "odprawa"));
        s01.add(new ScheduleEntry(7, 9, "baraki", "obchod_cel"));
        s01.add(new ScheduleEntry(9, 12, "dziedzinica", "patrol"));
        s01.add(new ScheduleEntry(13, 16, "kompleks", "patrol_dokumentacja"));
        s01.add(new ScheduleEntry(16, 20, "biuro", "zmiana"));
        guardSchedules.put(1, s01);

        // S02 - Gruba
        List<ScheduleEntry> s02 = new ArrayList<>();
        s02.add(new ScheduleEntry(7, 8, "stolowka", "setup"));
        s02.add(new ScheduleEntry(12, 14, "stolowka", "nadzor_obiadu"));
        s02.add(new ScheduleEntry(8, 12, "kompleks", "obchod_luzy"));
        s02.add(new ScheduleEntry(14, 20, "kompleks", "obchod_luzy"));
        guardSchedules.put(2, s02);

        // S03 - Kowalczyk
        List<ScheduleEntry> s03 = new ArrayList<>();
        s03.add(new ScheduleEntry(7, 19, "baraki_korytarze", "patrol_intensywny"));
        guardSchedules.put(3, s03);

        // S04 - Ryszard Noc
        List<ScheduleEntry> s04 = new ArrayList<>();
        s04.add(new ScheduleEntry(20, 24, "nocny_patrol", "cele_godzinowo"));
        s04.add(new ScheduleEntry(0, 6, "nocny_patrol", "zmeczony_patrol"));
        guardSchedules.put(4, s04);

        // S05 - Byk
        List<ScheduleEntry> s05 = new ArrayList<>();
        s05.add(new ScheduleEntry(8, 20, "barak_b_dziedzinica", "aktywny_patrol"));
        guardSchedules.put(5, s05);

        // S06 - Cień
        List<ScheduleEntry> s06 = new ArrayList<>();
        s06.add(new ScheduleEntry(6, 22, "wyjscie_z_barakow", "obserwacja"));
        guardSchedules.put(6, s06);

        // S07 - Wieża NW
        List<ScheduleEntry> s07 = new ArrayList<>();
        s07.add(new ScheduleEntry(6, 10, "wieza_nw", "obserwacja"));
        s07.add(new ScheduleEntry(10, 14, "wieza_nw", "rotacja_ze_s08"));
        s07.add(new ScheduleEntry(14, 18, "wieza_nw", "obserwacja"));
        s07.add(new ScheduleEntry(18, 22, "wieza_nw", "obserwacja"));
        guardSchedules.put(7, s07);

        // S08 - Wieża NE
        List<ScheduleEntry> s08 = new ArrayList<>();
        s08.add(new ScheduleEntry(6, 10, "wieza_ne", "obserwacja"));
        s08.add(new ScheduleEntry(10, 14, "wieza_ne", "rotacja_ze_s07"));
        s08.add(new ScheduleEntry(14, 22, "wieza_ne", "obserwacja"));
        guardSchedules.put(8, s08);

        // S09 - Mur
        List<ScheduleEntry> s09 = new ArrayList<>();
        s09.add(new ScheduleEntry(6, 22, "mur_zewnetrzny", "patrol_okrazeniowy"));
        guardSchedules.put(9, s09);

        // S10 - Brama
        List<ScheduleEntry> s10 = new ArrayList<>();
        s10.add(new ScheduleEntry(6, 14, "brama_glowna", "weryfikacja"));
        s10.add(new ScheduleEntry(14, 22, "brama_glowna", "weryfikacja"));
        guardSchedules.put(10, s10);

        // S11 - Halina
        List<ScheduleEntry> s11 = new ArrayList<>();
        s11.add(new ScheduleEntry(7, 19, "ogrod_bufor", "patrol_zewnetrzny"));
        guardSchedules.put(11, s11);

        // S12 - Bogdan Pies
        List<ScheduleEntry> s12 = new ArrayList<>();
        s12.add(new ScheduleEntry(21, 24, "zewnatrz_z_psem", "patrol_nocny"));
        s12.add(new ScheduleEntry(0, 6, "zewnatrz_z_psem", "patrol_nocny"));
        guardSchedules.put(12, s12);

        // S13 - Kruk
        List<ScheduleEntry> s13 = new ArrayList<>();
        s13.add(new ScheduleEntry(6, 22, "gabinet", "rezydent"));
        // Friday 10:00-10:45 inspection handled separately
        guardSchedules.put(13, s13);

        // S14 - Monika
        List<ScheduleEntry> s14 = new ArrayList<>();
        s14.add(new ScheduleEntry(8, 20, "szpital", "dyżur"));
        guardSchedules.put(14, s14);

        // S15 - Zdzisław
        List<ScheduleEntry> s15 = new ArrayList<>();
        s15.add(new ScheduleEntry(9, 17, "archiwum", "sluzba_senat"));
        guardSchedules.put(15, s15);

        // S16 - Mirka
        List<ScheduleEntry> s16 = new ArrayList<>();
        s16.add(new ScheduleEntry(5, 10, "kuchnia", "przygotowanie"));
        s16.add(new ScheduleEntry(10, 10, "biuro", "raport")); // 10-10:20 break
        s16.add(new ScheduleEntry(10, 19, "kuchnia", "nadzor"));
        guardSchedules.put(16, s16);

        // S17 - Inspektor (no fixed schedule)
        guardSchedules.put(17, new ArrayList<>());

        // S18 - Nocny Patrolowy
        List<ScheduleEntry> s18 = new ArrayList<>();
        s18.add(new ScheduleEntry(20, 24, "dziedzinica_baraki", "patrol_nocny"));
        s18.add(new ScheduleEntry(0, 6, "dziedzinica_baraki", "patrol_nocny"));
        guardSchedules.put(18, s18);

        // S19 - Nocny Wieże
        List<ScheduleEntry> s19 = new ArrayList<>();
        s19.add(new ScheduleEntry(20, 24, "wieze_se_sw", "rotacja_nocna"));
        s19.add(new ScheduleEntry(0, 6, "wieze_se_sw", "rotacja_nocna"));
        guardSchedules.put(19, s19);

        // S20 - Nocny Wewnętrzny
        List<ScheduleEntry> s20 = new ArrayList<>();
        s20.add(new ScheduleEntry(20, 24, "posterunek_glowny", "straz_nocna"));
        s20.add(new ScheduleEntry(0, 6, "posterunek_glowny", "straz_nocna"));
        guardSchedules.put(20, s20);
    }

    /** Returns the current activity string for a guard given world time (0-23999). */
    public String getCurrentActivity(int guardId, long worldTime) {
        int dayTick = (int)(worldTime % 24000);
        List<ScheduleEntry> schedule = guardSchedules.get(guardId);
        if (schedule == null || schedule.isEmpty()) {
            // S17 random inspector
            return "random_inspection";
        }
        for (ScheduleEntry entry : schedule) {
            int s = entry.startTick;
            int e = entry.endTick;
            if (s <= e) {
                if (dayTick >= s && dayTick < e) return entry.activity;
            } else {
                // wraps midnight
                if (dayTick >= s || dayTick < e) return entry.activity;
            }
        }
        return "idle";
    }

    public String getCurrentLocation(int guardId, long worldTime) {
        int dayTick = (int)(worldTime % 24000);
        List<ScheduleEntry> schedule = guardSchedules.get(guardId);
        if (schedule == null || schedule.isEmpty()) return "kompleks";
        for (ScheduleEntry entry : schedule) {
            int s = entry.startTick;
            int e = entry.endTick;
            if (s <= e) {
                if (dayTick >= s && dayTick < e) return entry.location;
            } else {
                if (dayTick >= s || dayTick < e) return entry.location;
            }
        }
        return "idle";
    }

    public boolean isMirkaOnBreak(long worldTime) {
        int dayTick = (int)(worldTime % 24000);
        // 10:00-10:20 = tick 4000-4333
        int breakStart = ScheduleEntry.hourToTick(10);
        int breakEnd = breakStart + 333;
        return dayTick >= breakStart && dayTick < breakEnd;
    }

    public boolean isZdzislawNapping(long worldTime) {
        int dayTick = (int)(worldTime % 24000);
        // 14:00-14:30 = tick 8000-8500
        int napStart = ScheduleEntry.hourToTick(14);
        int napEnd = napStart + 500;
        return dayTick >= napStart && dayTick < napEnd;
    }

    public boolean isRyszardExhausted(long worldTime) {
        int dayTick = (int)(worldTime % 24000);
        // 02:00-04:30 = tick 20000-22500
        int start = ScheduleEntry.hourToTick(2);
        int end = ScheduleEntry.hourToTick(4) + 500;
        if (start > end) return dayTick >= start || dayTick < end;
        return dayTick >= start && dayTick < end;
    }
}
