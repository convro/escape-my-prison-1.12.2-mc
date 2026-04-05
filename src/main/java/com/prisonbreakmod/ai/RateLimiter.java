package com.prisonbreakmod.ai;

import com.prisonbreakmod.PrisonBreakMod;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread-safe token-bucket rate limiter.
 *
 * <p>A fixed number of tokens ({@code requestsPerMinute}) are added to the
 * bucket at the start of every 60-second window.  Each call to {@link #acquire()}
 * consumes one token; if the bucket is empty the caller blocks until the next
 * window opens and tokens are refilled.
 *
 * <p>The refill scheduler runs as a daemon thread and is shut down via
 * {@link #shutdown()}.
 */
public final class RateLimiter {

    private final int requestsPerMinute;

    /** Current number of available tokens in the bucket. */
    private final AtomicInteger tokens;

    /** Lock used to coordinate between the refill task and acquire() callers. */
    private final ReentrantLock lock = new ReentrantLock();

    /** Condition signalled whenever new tokens are added to the bucket. */
    private final Condition tokenAvailable = lock.newCondition();

    /** Background scheduler that refills the bucket once per minute. */
    private final ScheduledExecutorService scheduler;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Creates a new rate limiter.
     *
     * @param requestsPerMinute maximum number of requests allowed per 60-second window;
     *                          must be &gt; 0
     */
    public RateLimiter(int requestsPerMinute) {
        if (requestsPerMinute <= 0) {
            throw new IllegalArgumentException("requestsPerMinute must be > 0, got: " + requestsPerMinute);
        }
        this.requestsPerMinute = requestsPerMinute;
        this.tokens = new AtomicInteger(requestsPerMinute);

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "PrisonBreak-RateLimiterRefill");
            t.setDaemon(true);
            return t;
        });

        // Refill the bucket at the start of every new 60-second window.
        scheduler.scheduleAtFixedRate(this::refill, 60L, 60L, TimeUnit.SECONDS);

        PrisonBreakMod.LOGGER.info("[RateLimiter] Initialized: {} requests/minute.", requestsPerMinute);
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Acquires one token, blocking until one is available.
     *
     * <p>This method is safe to call from multiple threads simultaneously.
     * Callers are unblocked in FIFO order when new tokens arrive.
     *
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    public void acquire() throws InterruptedException {
        lock.lockInterruptibly();
        try {
            // Spin-wait until a token is available.
            while (tokens.get() <= 0) {
                PrisonBreakMod.LOGGER.debug("[RateLimiter] Rate limit reached — waiting for next window.");
                tokenAvailable.await();
            }
            tokens.decrementAndGet();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns the number of tokens currently available without consuming one.
     *
     * @return current token count
     */
    public int availableTokens() {
        return tokens.get();
    }

    /**
     * Returns the configured request quota per minute.
     *
     * @return requests per minute
     */
    public int getRequestsPerMinute() {
        return requestsPerMinute;
    }

    /**
     * Shuts down the background refill scheduler.
     * After this call the rate limiter must not be used.
     */
    public void shutdown() {
        scheduler.shutdownNow();
        PrisonBreakMod.LOGGER.info("[RateLimiter] Scheduler shut down.");
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /** Refills the bucket to {@link #requestsPerMinute} and wakes all waiters. */
    private void refill() {
        lock.lock();
        try {
            tokens.set(requestsPerMinute);
            // Wake all threads waiting for a token; they will re-evaluate.
            tokenAvailable.signalAll();
            PrisonBreakMod.LOGGER.debug("[RateLimiter] Bucket refilled to {} tokens.", requestsPerMinute);
        } finally {
            lock.unlock();
        }
    }
}
