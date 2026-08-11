package com.gdfix;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gdfix.logic.GemstoneBlocks;
import org.junit.jupiter.api.Test;

class GemstoneBlocksTest {

    @Test
    void recognisesStainedGlass() {
        assertTrue(GemstoneBlocks.isGemstoneBlockId("minecraft:red_stained_glass"));
        assertTrue(GemstoneBlocks.isGemstoneBlockId("minecraft:lime_stained_glass"));
        assertTrue(GemstoneBlocks.isGemstoneBlockId("white_stained_glass"));
    }

    @Test
    void recognisesStainedGlassPanes() {
        assertTrue(GemstoneBlocks.isGemstoneBlockId("minecraft:blue_stained_glass_pane"));
        assertTrue(GemstoneBlocks.isGemstoneBlockId("purple_stained_glass_pane"));
    }

    @Test
    void rejectsNonGemstoneBlocks() {
        assertFalse(GemstoneBlocks.isGemstoneBlockId("minecraft:stone"));
        assertFalse(GemstoneBlocks.isGemstoneBlockId("minecraft:glass"));
        assertFalse(GemstoneBlocks.isGemstoneBlockId("minecraft:glass_pane"));
        assertFalse(GemstoneBlocks.isGemstoneBlockId("minecraft:air"));
    }

    @Test
    void toleratesNullAndEmpty() {
        assertFalse(GemstoneBlocks.isGemstoneBlockId(null));
        assertFalse(GemstoneBlocks.isGemstoneBlockId(""));
    }
}
