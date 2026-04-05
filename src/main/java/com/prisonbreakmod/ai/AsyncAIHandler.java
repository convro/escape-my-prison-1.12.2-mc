package com.prisonbreakmod.ai;

import com.prisonbreakmod.PrisonBreakMod;
import com.prisonbreakmod.config.PrisonConfig;
import com.prisonbreakmod.entity.AbstractNPC;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Manages the lifecycle of asynchronous AI queries for all active NPCs.
 *
 * <p>Usage pattern (server tick loop):
 * <ol>
 *   <li>NPC AI goal calls {@link #submitQuery(AbstractNPC, String)} when it wants
 *       a new decision from the AI.</li>
 *   <li>The main server tick handler calls {@link #processResults()} every tick
 *       to drain completed futures and apply responses back to NPCs on the
 *       server thread.</li>
 *   <li>On server shutdown, call {@link #shutdown()} to release resources.</li>
 * </ol>
 *
 * <p>All concurrency is handled internally:
 * <ul>
 *   <li>The pending-query queue is a {@link ConcurrentLinkedQueue} — safe for
 *       concurrent producers (NPC goal threads) and a single consumer
 *       (server tick thread calling {@link #processResults()}).</li>
 *   <li>The {@link DeepSeekClient} manages its own thread pool and rate limiter.</li>
 * </ul>
 */
public final class AsyncAIHandler {

    // -------------------------------------------------------------------------
    // Inner class: PendingQuery
    // -------------------------------------------------------------------------

    /**
     * Associates an in-flight {@link CompletableFuture} with the NPC that
     * submitted the query.
     */
    public static final class PendingQuery {

        /** The NPC waiting for an AI decision. */
        public final AbstractNPC npc;

        /** The future that will resolve with the AI's response. */
        public final CompletableFuture<AIResponse> future;

        PendingQuery(AbstractNPC npc, CompletableFuture<AIResponse> future) {
            this.npc    = npc;
            this.future = future;
        }
    }

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------

    /**
     * Queue of all in-flight queries.
     * Producers: any thread calling {@link #submitQuery}.
     * Consumer:  server tick thread calling {@link #processResults}.
     */
    private final ConcurrentLinkedQueue<PendingQuery> pendingQueries =
            new ConcurrentLinkedQueue<>();

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Creates a new handler.  The underlying {@link DeepSeekClient} singleton
     * is initialised lazily on first use.
     */
    public AsyncAIHandler() {
        PrisonBreakMod.LOGGER.info("[AsyncAIHandler] Initialized.");
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Submits an AI query for the given NPC and stores the pending future.
     *
     * <p>This method is non-blocking — the HTTP call is dispatched to the
     * {@link DeepSeekClient}'s internal thread pool.
     *
     * <p>The NPC's model and token budget are resolved from its type:
     * <ul>
     *   <li>The system prompt is obtained from {@link AbstractNPC#buildSystemPrompt()}.</li>
     *   <li>The user message is the {@code userMessage} parameter.</li>
     *   <li>Max tokens come from {@link PrisonConfig} via the NPC's cooldown mapping.</li>
     * </ul>
     *
     * @param npc         the NPC requesting a decision (must not be null)
     * @param userMessage the user-role message describing the current situation
     */
    public void submitQuery(AbstractNPC npc, String userMessage) {
        if (npc == null) {
            PrisonBreakMod.LOGGER.warn("[AsyncAIHandler] submitQuery called with null NPC — skipping.");
            return;
        }

        String systemPrompt = npc.buildSystemPrompt();
        int maxTokens       = resolveMaxTokens(npc);
        String model        = DeepSeekClient.MODEL_CHAT;

        PrisonBreakMod.LOGGER.debug("[AsyncAIHandler] Submitting query for NPC {} (maxTokens={}).",
                npc.getNpcId(), maxTokens);

        CompletableFuture<AIResponse> future =
                DeepSeekClient.getInstance().queryAsync(systemPrompt, userMessage, model, maxTokens);

        pendingQueries.add(new PendingQuery(npc, future));
        npc.markAIQueried();
    }

    /**
     * Drains all completed futures from the pending queue and applies each
     * resolved {@link AIResponse} to the corresponding NPC.
     *
     * <p><strong>Must be called from the server tick thread</strong> so that
     * {@link AbstractNPC#applyAIResponse} executes with safe world access.
     *
     * <p>Futures that have not yet completed are left in the queue and will be
     * checked on the next tick.
     */
    public void processResults() {
        // Iterate with removal — we must not use an enhanced for-loop here
        // because we want to conditionally remove entries while iterating a
        // ConcurrentLinkedQueue, which supports iterator.remove().
        java.util.Iterator<PendingQuery> it = pendingQueries.iterator();
        while (it.hasNext()) {
            PendingQuery pq = it.next();

            if (!pq.future.isDone()) {
                // Not ready yet — leave in the queue.
                continue;
            }

            // Remove from queue regardless of outcome.
            it.remove();

            if (pq.npc.isDead) {
                // NPC was removed from the world before the response arrived.
                PrisonBreakMod.LOGGER.debug("[AsyncAIHandler] NPC {} is dead — discarding AI response.",
                        pq.npc.getNpcId());
                continue;
            }

            AIResponse response;
            try {
                // future.get() is safe here because isDone() returned true.
                response = pq.future.get();
            } catch (Exception e) {
                PrisonBreakMod.LOGGER.error("[AsyncAIHandler] Failed to retrieve AI response for NPC {}: {}",
                        pq.npc.getNpcId(), e.getMessage());
                response = AIResponse.fallback();
            }

            if (response == null) {
                response = AIResponse.fallback();
            }

            PrisonBreakMod.LOGGER.debug("[AsyncAIHandler] Applying response to NPC {}: action={}",
                    pq.npc.getNpcId(), response.getAction());

            // Store any memory snippet the model produced.
            if (response.hasMemory()) {
                pq.npc.getNPCMemory().addMemory(response.getMemory());
            }

            // Apply global alert change.
            int alertDelta = response.getAlertChange();
            if (alertDelta > 0) {
                SharedPrisonState.getInstance().raiseAlert(alertDelta, "NPC " + pq.npc.getNpcId());
            } else if (alertDelta < 0) {
                SharedPrisonState.getInstance().lowerAlert(-alertDelta);
            }

            // Apply relation change.
            if (response.getRelChange() != 0) {
                SharedPrisonState.getInstance().adjustRelation(pq.npc.getNpcId(), response.getRelChange());
            }

            // Delegate NPC-specific behaviour to the NPC subclass.
            pq.npc.applyAIResponse(response);
        }
    }

    /**
     * Returns the number of queries currently waiting for a response.
     *
     * @return pending query count
     */
    public int getPendingCount() {
        return pendingQueries.size();
    }

    /**
     * Shuts down this handler and releases underlying AI resources.
     * Should be called once during mod/server teardown.
     */
    public void shutdown() {
        pendingQueries.clear();
        DeepSeekClient.getInstance().shutdown();
        PrisonBreakMod.LOGGER.info("[AsyncAIHandler] Shut down.");
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Determines the maximum token budget for an NPC based on the NPC's
     * cooldown tier (a proxy for NPC type).
     *
     * <p>The mapping is intentionally heuristic — once concrete NPC subclasses
     * exist, this can switch on their class or an enum type field.
     */
    private static int resolveMaxTokens(AbstractNPC npc) {
        int cooldown = npc.getAICooldownTicks();

        // Companion: short cooldown → rich responses
        if (cooldown <= PrisonConfig.companionCooldownTicks) {
            return PrisonConfig.maxCompanionTokens;
        }
        // Guard: medium cooldown
        if (cooldown <= PrisonConfig.guardCooldownTicks) {
            return PrisonConfig.maxGuardTokens;
        }
        // Prisoner: longest cooldown → most compressed responses
        return PrisonConfig.maxPrisonerTokens;
    }
}
