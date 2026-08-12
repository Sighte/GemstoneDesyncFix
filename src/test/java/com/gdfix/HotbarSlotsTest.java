package com.gdfix;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gdfix.logic.HotbarSlots;
import org.junit.jupiter.api.Test;

class HotbarSlotsTest {

    @Test
    void matchesHeldHotbarSlot() {
        // container 0, slot 36 == hotbar index 0
        assertTrue(HotbarSlots.isHeldHotbarSlot(0, 36, 0));
        // slot 44 == hotbar index 8
        assertTrue(HotbarSlots.isHeldHotbarSlot(0, 44, 8));
        // slot 40 == hotbar index 4
        assertTrue(HotbarSlots.isHeldHotbarSlot(0, 40, 4));
    }

    @Test
    void rejectsWhenNotTheSelectedSlot() {
        assertFalse(HotbarSlots.isHeldHotbarSlot(0, 36, 1));
        assertFalse(HotbarSlots.isHeldHotbarSlot(0, 44, 0));
    }

    @Test
    void rejectsNonHotbarSlots() {
        assertFalse(HotbarSlots.isHeldHotbarSlot(0, 35, 0)); // just below hotbar
        assertFalse(HotbarSlots.isHeldHotbarSlot(0, 45, 8)); // just above hotbar
        assertFalse(HotbarSlots.isHeldHotbarSlot(0, 9, 0));  // main inventory
    }

    @Test
    void rejectsOtherContainers() {
        // Same slot numbers but a chest/menu is open (containerId != 0).
        assertFalse(HotbarSlots.isHeldHotbarSlot(1, 40, 4));
    }
}
