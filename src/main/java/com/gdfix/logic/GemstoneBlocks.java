package com.gdfix.logic;

/**
 * Pure helper (no Minecraft dependencies) for recognising the blocks that Hypixel
 * SkyBlock uses to render mineable gemstones.
 *
 * <p>In the Crystal Hollows / Glacite tunnels gemstone crystals are shown to the
 * client as coloured stained glass ({@code *_stained_glass}) and stained glass
 * panes ({@code *_stained_glass_pane}). This class only inspects the block
 * identifier so it can be exercised by unit tests without a running client.
 */
public final class GemstoneBlocks {

    private GemstoneBlocks() {
    }

    /**
     * @param blockId a block identifier, either namespaced ({@code minecraft:red_stained_glass})
     *                or bare ({@code red_stained_glass}); {@code null} is tolerated.
     * @return {@code true} if the block is a stained-glass block or pane, i.e. how a
     *         gemstone crystal appears client-side.
     */
    public static boolean isGemstoneBlockId(String blockId) {
        if (blockId == null || blockId.isEmpty()) {
            return false;
        }
        int colon = blockId.indexOf(':');
        String path = colon >= 0 ? blockId.substring(colon + 1) : blockId;
        return path.endsWith("stained_glass") || path.endsWith("stained_glass_pane");
    }
}
