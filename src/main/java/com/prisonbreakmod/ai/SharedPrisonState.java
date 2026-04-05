package com.prisonbreakmod.ai;

import com.prisonbreakmod.PrisonBreakMod;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe singleton holding all global prison-state that is shared across
 * every NPC AI instance.
 *
 * <p>Design decisions:
 * <ul>
 *   <li>{@link #alertLevel} is an {@link AtomicInteger} so that the common
 *       read path is lock-free.</li>
 *   <li>All {@code ConcurrentLinkedDeque} and {@code ConcurrentHashMap} fields
 *       are individually thread-safe; methods that must perform compound
 *       check-then-act are {@code synchronized} on {@code this}.</li>
 *   <li>{@code volatile} primitives / object references are used for single-
 *       assignment flags that do not need compound atomicity.</li>
 * </ul>
 *
 * <p>Singleton initialised with double-checked locking — safe under Java 5+
 * memory model with the {@code volatile} keyword.
 */
public final class SharedPrisonState {

    // =========================================================================
    // Singleton
    // =========================================================================

    private static volatile SharedPrisonState instance;

    /** Private constructor — use {@link #getInstance()}. */
    private SharedPrisonState() {}

    /**
     * Returns the singleton, creating it on first call (double-checked locking).
     *
     * @return the single {@code SharedPrisonState} instance
     */
    public static SharedPrisonState getInstance() {
        if (instance == null) {
            synchronized (SharedPrisonState.class) {
                if (instance == null) {
                    instance = new SharedPrisonState();
                }
            }
        }
        return instance;
    }

    // =========================================================================
    // Alert level  (0 = calm · 1 = suspicious · 2 = alert · 3 = lockdown)
    // =========================================================================

    private static final int ALERT_MIN = 0;
    private static final int ALERT_MAX = 3;

    /** Current global alert level (0-3). */
    private final AtomicInteger alertLevel = new AtomicInteger(0);

    /** Human-readable reason for the most recent alert change. */
    private volatile String alertReason = "";

    /**
     * Raises the global alert level by {@code levels}, clamped to
     * {@value #ALERT_MAX}.
     *
     * @param levels positive delta
     * @param reason short explanation stored in {@link #alertReason}
     */
    public synchronized void raiseAlert(int levels, String reason) {
        if (levels <= 0) return;
        int prev = alertLevel.get();
        int next = Math.min(ALERT_MAX, prev + levels);
        alertLevel.set(next);
        if (reason != null && !reason.trim().isEmpty()) {
            alertReason = reason.trim();
        }
        PrisonBreakMod.LOGGER.info("[SharedPrisonState] Alert raised {} → {} (reason: {})",
                prev, next, alertReason);
        addEvent("ALERT_RAISED:" + next + ":" + alertReason);
    }

    /**
     * Lowers the global alert level by {@code levels}, clamped to
     * {@value #ALERT_MIN}.
     *
     * @param levels positive delta to subtract
     */
    public synchronized void lowerAlert(int levels) {
        if (levels <= 0) return;
        int prev = alertLevel.get();
        int next = Math.max(ALERT_MIN, prev - levels);
        alertLevel.set(next);
        PrisonBreakMod.LOGGER.info("[SharedPrisonState] Alert lowered {} → {}", prev, next);
        addEvent("ALERT_LOWERED:" + next);
    }

    /** @return current alert level (0-3) */
    public int getAlertLevel() { return alertLevel.get(); }

    /** @return the last reason string provided to {@link #raiseAlert} */
    public String getAlertReason() { return alertReason; }

    // =========================================================================
    // Guard positions   guardId → [x, y, z]
    // =========================================================================

    private final ConcurrentHashMap<Integer, double[]> guardPositions =
            new ConcurrentHashMap<>();

    /**
     * Updates the cached world position of the given guard.
     *
     * @param guardId mod-local entity id
     * @param x       world X
     * @param y       world Y
     * @param z       world Z
     */
    public void updateGuardPosition(int guardId, double x, double y, double z) {
        guardPositions.put(guardId, new double[]{x, y, z});
    }

    /**
     * Returns the last-known position of a guard, or {@code null} if unknown.
     *
     * @param guardId mod-local entity id
     * @return {@code double[3]} array [x,y,z] or {@code null}
     */
    public double[] getGuardPosition(int guardId) {
        return guardPositions.get(guardId);
    }

    /** Exposes the full guard-positions map (read-only view via iteration). */
    public ConcurrentHashMap<Integer, double[]> getGuardPositions() {
        return guardPositions;
    }

    // =========================================================================
    // Recent events log  (rolling, capped at 20 entries)
    // =========================================================================

    private static final int MAX_EVENTS = 20;

    private final ConcurrentLinkedDeque<String> recentEvents =
            new ConcurrentLinkedDeque<>();

    /**
     * Appends a short event string to the rolling log, evicting the oldest
     * entry if the log is at capacity.
     *
     * @param event non-null, non-blank event descriptor
     */
    public void addEvent(String event) {
        if (event == null || event.trim().isEmpty()) return;
        recentEvents.addLast(event.trim());
        // Trim to max
        while (recentEvents.size() > MAX_EVENTS) {
            recentEvents.pollFirst();
        }
    }

    /**
     * Returns the most recent {@code count} events as a newline-separated string.
     *
     * @param count maximum number of events to return
     * @return formatted string (may be empty)
     */
    public String getRecentEvents(int count) {
        if (count <= 0 || recentEvents.isEmpty()) return "";
        String[] arr = recentEvents.toArray(new String[0]);
        int start = Math.max(0, arr.length - count);
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < arr.length; i++) {
            if (i > start) sb.append('\n');
            sb.append(arr[i]);
        }
        return sb.toString();
    }

    // =========================================================================
    // Scheduled blackout
    // =========================================================================

    /** World-time tick at which a blackout was scheduled, or -1 if none. */
    private volatile long blackoutScheduledAt = -1L;

    public void scheduleBlackout(long worldTime) { blackoutScheduledAt = worldTime; }
    public void cancelBlackout()                 { blackoutScheduledAt = -1L; }
    public long getBlackoutScheduledAt()         { return blackoutScheduledAt; }
    public boolean isBlackoutScheduled()         { return blackoutScheduledAt >= 0; }

    // =========================================================================
    // Dog status
    // =========================================================================

    /** {@code true} when the tracking dog has been neutralised. */
    private volatile boolean dogNeutralized = false;

    public void setDogNeutralized(boolean value) {
        dogNeutralized = value;
        addEvent(value ? "DOG_NEUTRALIZED" : "DOG_ACTIVE");
    }
    public boolean isDogNeutralized() { return dogNeutralized; }

    // =========================================================================
    // Helicopter status
    // =========================================================================

    /** {@code true} while the surveillance helicopter is actively scanning. */
    private volatile boolean helicopterActive = false;

    public void setHelicopterActive(boolean active) {
        helicopterActive = active;
        addEvent(active ? "HELICOPTER_ACTIVE" : "HELICOPTER_INACTIVE");
    }
    public boolean isHelicopterActive() { return helicopterActive; }

    // =========================================================================
    // Footprints / scent trail   [worldTime, x, z]
    // =========================================================================

    private static final int MAX_FOOTPRINTS = 200;

    /**
     * Each element is a {@code long[3]}: [worldTimeTick, blockX*1000, blockZ*1000].
     * Multiplying double coords by 1000 and storing as long avoids boxing while
     * preserving sub-block precision.
     */
    private final ConcurrentLinkedDeque<long[]> footprints =
            new ConcurrentLinkedDeque<>();

    /**
     * Records a player footprint.
     *
     * @param time world-time tick
     * @param x    world X (double precision stored as fixed-point ×1000)
     * @param z    world Z
     */
    public void addFootprint(long time, double x, double z) {
        footprints.addLast(new long[]{time, (long)(x * 1000), (long)(z * 1000)});
        while (footprints.size() > MAX_FOOTPRINTS) {
            footprints.pollFirst();
        }
    }

    /**
     * Removes all footprint entries whose timestamp is older than
     * {@code currentTime - 6000} ticks (≈ 5 minutes at 20 TPS).
     *
     * @param currentTime current world-time tick
     */
    public void cleanOldFootprints(long currentTime) {
        long cutoff = currentTime - 6000L;
        // pollFirst while the oldest entry is beyond the cutoff.
        while (!footprints.isEmpty()) {
            long[] head = footprints.peekFirst();
            if (head != null && head[0] < cutoff) {
                footprints.pollFirst();
            } else {
                break;
            }
        }
    }

    /** Exposes raw footprint deque for iteration (weakly consistent). */
    public ConcurrentLinkedDeque<long[]> getFootprints() {
        return footprints;
    }

    // =========================================================================
    // NPC relations  (npcId → 0-100, default 50)
    // =========================================================================

    private final ConcurrentHashMap<String, Integer> relations =
            new ConcurrentHashMap<>();

    /**
     * Sets the absolute relation value for an NPC.
     *
     * @param npcId unique NPC identifier
     * @param value clamped to [0, 100]
     */
    public void setRelation(String npcId, int value) {
        if (npcId == null || npcId.isEmpty()) return;
        relations.put(npcId, Math.max(0, Math.min(100, value)));
    }

    /**
     * Returns the current relation value for an NPC.  Defaults to 50 (neutral).
     *
     * @param npcId unique NPC identifier
     * @return relation value in [0, 100]
     */
    public int getRelation(String npcId) {
        return relations.getOrDefault(npcId, 50);
    }

    /**
     * Adjusts the relation by {@code delta}, clamped to [0, 100].
     *
     * @param npcId unique NPC identifier
     * @param delta signed change
     */
    public void adjustRelation(String npcId, int delta) {
        if (npcId == null || npcId.isEmpty()) return;
        relations.merge(npcId, delta,
                (old, d) -> Math.max(0, Math.min(100, old + d)));
    }

    // =========================================================================
    // Compact JSON context for NPC prompts
    // =========================================================================

    /**
     * Produces a compact JSON string describing the current prison state for
     * inclusion in NPC AI prompts.  Includes alert level, blizzard / dog /
     * helicopter flags, up to 10 recent events, and blackout status.
     *
     * @return JSON object string, never null
     */
    public String toContextJSON() {
        String recentEvts = getRecentEvents(10)
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "|");

        return "{"
             + "\"alert\":" + alertLevel.get() + ","
             + "\"alertReason\":\"" + jsonEscape(alertReason) + "\","
             + "\"dogNeutralized\":" + dogNeutralized + ","
             + "\"helicopterActive\":" + helicopterActive + ","
             + "\"blizzard\":" + blizzardActive + ","
             + "\"hunt\":" + huntActive + ","
             + "\"blackoutAt\":" + blackoutScheduledAt + ","
             + "\"recentEvents\":\"" + recentEvts + "\""
             + "}";
    }

    // =========================================================================
    // Blizzard (set by WeatherSystem, read by NPC prompts)
    // =========================================================================

    private volatile boolean blizzardActive = false;

    public void setBlizzard(boolean active) { blizzardActive = active; }
    public boolean isBlizzard() { return blizzardActive; }

    // =========================================================================
    // Hunt active flag
    // =========================================================================

    private volatile boolean huntActive = false;

    public void setHuntActive(boolean active) {
        huntActive = active;
        addEvent(active ? "HUNT_ACTIVE" : "HUNT_ENDED");
    }
    public boolean isHuntActive() { return huntActive; }

    // =========================================================================
    // Alert level direct setter (for save/load)
    // =========================================================================

    public void setAlertLevel(int level) {
        alertLevel.set(Math.max(0, Math.min(3, level)));
    }

    public void setAlertReason(String reason) {
        if (reason != null) alertReason = reason;
    }

    // =========================================================================
    // Dog reactivation scheduling
    // =========================================================================

    private volatile long dogReactivationTick = -1L;

    public void scheduleDogReactivation(long tick) { dogReactivationTick = tick; }

    public void checkDogReactivation(long currentTick) {
        if (dogNeutralized && dogReactivationTick >= 0 && currentTick >= dogReactivationTick) {
            dogNeutralized = false;
            dogReactivationTick = -1L;
            addEvent("DOG_REACTIVATED");
        }
    }

    // =========================================================================
    // Sleeping herb tracking
    // =========================================================================

    private volatile String sleepingHerbTarget = "";
    private volatile long sleepingHerbEndTick = -1L;

    public boolean hasSleepingHerbActive() { return sleepingHerbEndTick >= 0; }

    public void setSleepingHerbTarget(String npcId, long endTick) {
        sleepingHerbTarget = npcId;
        sleepingHerbEndTick = endTick;
    }

    public String getSleepingHerbTarget() { return sleepingHerbTarget; }

    public void checkSleepingHerb(long currentTick) {
        if (sleepingHerbEndTick >= 0 && currentTick >= sleepingHerbEndTick) {
            addEvent("NPC_WOKE_UP:" + sleepingHerbTarget);
            sleepingHerbTarget = "";
            sleepingHerbEndTick = -1L;
        }
    }

    // =========================================================================
    // Footprint retrieval — latest entry
    // =========================================================================

    /**
     * Returns the most recent footprint as [worldTime, x*1000, z*1000], or null if empty.
     */
    public long[] getLatestFootprint() {
        return footprints.isEmpty() ? null : footprints.peekLast();
    }

    // =========================================================================
    // Snapshot methods for persistence
    // =========================================================================

    public java.util.Map<String, Integer> getRelationsSnapshot() {
        return new java.util.HashMap<>(relations);
    }

    public java.util.List<String> getEventsSnapshot() {
        return new java.util.ArrayList<>(recentEvents);
    }

    // =========================================================================
    // Reset
    // =========================================================================

    /**
     * Resets all shared state to initial defaults.
     * Should be called on new game start and at mod initialization.
     */
    public synchronized void reset() {
        alertLevel.set(0);
        alertReason      = "";
        blackoutScheduledAt = -1L;
        dogNeutralized   = false;
        helicopterActive = false;
        guardPositions.clear();
        recentEvents.clear();
        footprints.clear();
        relations.clear();
        PrisonBreakMod.LOGGER.info("[SharedPrisonState] State reset to defaults.");
    }

    // =========================================================================
    // Internal helpers
    // =========================================================================

    private static String jsonEscape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
