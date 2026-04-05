package com.prisonbreakmod.ai;

import com.prisonbreakmod.PrisonBreakMod;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Episodic memory store for a single NPC.
 *
 * <p>Stores up to {@link #MAX_ENTRIES} timestamped string entries in a
 * thread-safe {@link ConcurrentLinkedDeque}.  Entries are appended at the
 * tail; when the deque exceeds the capacity the oldest entries (head) are
 * removed by {@link #clearOld()}.
 *
 * <p>Thread safety: {@link ConcurrentLinkedDeque} provides lock-free atomicity
 * for individual operations.  {@link #clearOld()} uses a simple poll loop
 * which is safe because only one entry is added before each call.
 */
public final class NPCMemory {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    /** Maximum number of memory entries retained per NPC. */
    private static final int MAX_ENTRIES = 10;

    /** Date format used to prefix each entry (e.g. "15:04:32"). */
    private static final SimpleDateFormat TIME_FMT =
            new SimpleDateFormat("HH:mm:ss");

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------

    /** The backing store — head is oldest, tail is newest. */
    private final ConcurrentLinkedDeque<String> entries = new ConcurrentLinkedDeque<>();

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /** Creates a new, empty memory store (npcId not tracked). */
    public NPCMemory() {}

    /**
     * Creates a new, empty memory store associated with the given NPC identifier.
     * The {@code npcId} is used only for debug logging.
     *
     * @param npcId the NPC's unique string identifier
     */
    public NPCMemory(String npcId) {
        // npcId stored only for potential future debug use; not required for operation.
        PrisonBreakMod.LOGGER.debug("[NPCMemory] Created for NPC '{}'.", npcId);
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Adds a new memory entry, timestamped with the current real-world time.
     * If the store is already at capacity, the oldest entry is evicted first
     * via {@link #clearOld()}.
     *
     * @param entry the memory text to store (null / blank entries are ignored)
     */
    public void addMemory(String entry) {
        if (entry == null || entry.trim().isEmpty()) {
            return;
        }

        String timestamp = TIME_FMT.format(new Date());
        String stamped   = "[" + timestamp + "] " + entry.trim();

        entries.addLast(stamped);
        clearOld();

        PrisonBreakMod.LOGGER.debug("[NPCMemory] Added entry: {}", stamped);
    }

    /**
     * Returns all current memory entries as a single newline-delimited string,
     * ordered oldest-first.  Returns an empty string when the memory is empty.
     *
     * @return formatted memory string suitable for inclusion in an AI prompt
     */
    public String getMemoryStr() {
        if (entries.isEmpty()) {
            return "";
        }

        // Snapshot to avoid issues with weakly-consistent iterator under concurrent writes.
        List<String> snapshot = new ArrayList<>(entries);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < snapshot.size(); i++) {
            if (i > 0) sb.append('\n');
            sb.append(snapshot.get(i));
        }
        return sb.toString();
    }

    /**
     * Trims the memory to at most {@link #MAX_ENTRIES} entries by removing
     * entries from the head (oldest first).
     *
     * <p>Called automatically after each {@link #addMemory} but may also be
     * invoked explicitly (e.g. during NPC reset).
     */
    public void clearOld() {
        while (entries.size() > MAX_ENTRIES) {
            String removed = entries.pollFirst();
            if (removed != null) {
                PrisonBreakMod.LOGGER.debug("[NPCMemory] Evicted oldest entry: {}", removed);
            }
        }
    }

    /**
     * Removes all memory entries.
     */
    public void clear() {
        entries.clear();
    }

    /**
     * Returns the current number of stored entries (0 – {@link #MAX_ENTRIES}).
     *
     * @return entry count
     */
    public int size() {
        return entries.size();
    }

    /**
     * Returns {@code true} if there are no stored entries.
     *
     * @return {@code true} when empty
     */
    public boolean isEmpty() {
        return entries.isEmpty();
    }
}
