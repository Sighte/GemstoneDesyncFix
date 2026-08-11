package com.gdfix.logic;

/**
 * Pure state machine (no Minecraft dependencies) that decides when the
 * "gemstone desync fix" should nudge the server to re-sync a ghost block.
 *
 * <p>Mechanics being modelled: when the client breaks a gemstone block but the
 * server has not (yet) acknowledged the removal, the block keeps rendering as a
 * gemstone even though further hits do nothing — a client/server desync commonly
 * called a "ghost block". This tracker watches the last position the client
 * believes it broke; if that position still reports a gemstone block after
 * {@code ghostThresholdTicks}, it signals exactly one re-sync action. If the
 * block clears (server confirmed the break) it stops. If a re-sync does not
 * resolve the ghost within {@code giveUpTicks}, it gives up so tracking can
 * restart cleanly instead of spamming packets.
 *
 * <p>All time is expressed in client ticks supplied by the caller, which keeps
 * the class deterministic and unit-testable.
 */
public final class DesyncTracker {

    private final int ghostThresholdTicks;
    private final int giveUpTicks;

    private String trackedPos;
    private int trackedSinceTick;
    private boolean resyncEmitted;

    public DesyncTracker(int ghostThresholdTicks, int giveUpTicks) {
        if (ghostThresholdTicks < 1) {
            throw new IllegalArgumentException("ghostThresholdTicks must be >= 1");
        }
        if (giveUpTicks < ghostThresholdTicks) {
            throw new IllegalArgumentException("giveUpTicks must be >= ghostThresholdTicks");
        }
        this.ghostThresholdTicks = ghostThresholdTicks;
        this.giveUpTicks = giveUpTicks;
    }

    /** Record that the client believes it just broke a gemstone block at {@code posKey}. */
    public void onGemstoneBroken(String posKey, int tick) {
        if (posKey == null) {
            throw new IllegalArgumentException("posKey must not be null");
        }
        this.trackedPos = posKey;
        this.trackedSinceTick = tick;
        this.resyncEmitted = false;
    }

    /**
     * Advance one client tick.
     *
     * @param currentTick         monotonically increasing tick counter
     * @param trackedStillGemstone whether the tracked position still shows a gemstone block
     * @return {@code true} exactly once, on the tick a re-sync should be sent
     */
    public boolean tick(int currentTick, boolean trackedStillGemstone) {
        if (trackedPos == null) {
            return false;
        }
        if (!trackedStillGemstone) {
            // Server confirmed the break: client and server agree again.
            reset();
            return false;
        }
        int elapsed = currentTick - trackedSinceTick;
        if (resyncEmitted) {
            if (elapsed >= giveUpTicks) {
                reset();
            }
            return false;
        }
        if (elapsed >= ghostThresholdTicks) {
            resyncEmitted = true;
            return true;
        }
        return false;
    }

    /** Forget the current target (e.g. the player started mining somewhere else). */
    public void clear() {
        reset();
    }

    public boolean isTracking() {
        return trackedPos != null;
    }

    public String trackedPos() {
        return trackedPos;
    }

    private void reset() {
        this.trackedPos = null;
        this.trackedSinceTick = 0;
        this.resyncEmitted = false;
    }
}
