package com.gdfix;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gdfix.logic.DesyncTracker;
import org.junit.jupiter.api.Test;

class DesyncTrackerTest {

    private DesyncTracker newTracker() {
        return new DesyncTracker(3, 9);
    }

    @Test
    void notTrackingByDefault() {
        DesyncTracker t = newTracker();
        assertFalse(t.isTracking());
        assertFalse(t.tick(100, true));
    }

    @Test
    void doesNotFireBeforeThreshold() {
        DesyncTracker t = newTracker();
        t.onGemstoneBroken("10,20,30", 0);
        assertTrue(t.isTracking());
        assertFalse(t.tick(1, true));
        assertFalse(t.tick(2, true));
    }

    @Test
    void firesExactlyOnceAtThreshold() {
        DesyncTracker t = newTracker();
        t.onGemstoneBroken("10,20,30", 0);
        assertFalse(t.tick(2, true));
        assertTrue(t.tick(3, true), "should fire resync once threshold reached");
        assertFalse(t.tick(4, true), "must not fire again while ghost persists");
        assertFalse(t.tick(5, true));
    }

    @Test
    void clearsWhenServerConfirmsBreak() {
        DesyncTracker t = newTracker();
        t.onGemstoneBroken("10,20,30", 0);
        assertFalse(t.tick(1, false)); // block gone -> synced
        assertFalse(t.isTracking());
    }

    @Test
    void givesUpAfterGiveUpTicksSoItCanRetrack() {
        DesyncTracker t = newTracker();
        t.onGemstoneBroken("10,20,30", 0);
        assertTrue(t.tick(3, true));   // resync fired
        assertFalse(t.tick(8, true));  // still ghost, not yet give-up
        assertTrue(t.isTracking());
        assertFalse(t.tick(9, true));  // give-up reached -> reset
        assertFalse(t.isTracking());
    }

    @Test
    void retrackingResetsState() {
        DesyncTracker t = newTracker();
        t.onGemstoneBroken("1,1,1", 0);
        assertTrue(t.tick(3, true));
        t.onGemstoneBroken("2,2,2", 10); // new target
        assertFalse(t.tick(11, true));
        assertTrue(t.tick(13, true));
    }

    @Test
    void rejectsInvalidConstructorArgs() {
        assertThrows(IllegalArgumentException.class, () -> new DesyncTracker(0, 5));
        assertThrows(IllegalArgumentException.class, () -> new DesyncTracker(5, 4));
    }

    @Test
    void rejectsNullPos() {
        assertThrows(IllegalArgumentException.class, () -> newTracker().onGemstoneBroken(null, 0));
    }
}
