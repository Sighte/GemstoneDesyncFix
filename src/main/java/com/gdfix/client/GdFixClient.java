package com.gdfix.client;

import com.gdfix.logic.DesyncTracker;
import com.gdfix.logic.GemstoneBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point and runtime coordinator for the two fixes (Minecraft 26.1, Mojang names).
 *
 * <ul>
 *   <li><b>Break reset fix</b> is applied inside {@code MultiPlayerGameModeMixin}, which
 *       calls {@link #config()}.</li>
 *   <li><b>Gemstone desync fix</b> is driven here: the mixin reports gemstone break attempts
 *       via {@link #onBreakBlockAttempt(BlockPos)}, and a client tick handler watches for the
 *       block being reverted by the server (a ghost) and re-drives the break through the
 *       vanilla game mode (which keeps Minecraft's packet-sequence numbers correct).</li>
 * </ul>
 */
public final class GdFixClient implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("gdfix");

    private static GdFixConfig config;
    private static DesyncTracker tracker;
    private static BlockPos trackedPos;
    private static int tickCounter;

    @Override
    public void onInitializeClient() {
        config = GdFixConfig.load();
        rebuildTracker();
        ClientTickEvents.END_CLIENT_TICK.register(GdFixClient::onEndTick);
        GdFixCommand.register();
        LOGGER.info("[gdfix] Gemstone Desync Fix loaded (breakResetFix={}, gemstoneDesyncFix={})",
                config.breakResetFix, config.gemstoneDesyncFix);
    }

    public static GdFixConfig config() {
        return config;
    }

    /** Rebuild the tracker after the tuning values change in the config. */
    public static void rebuildTracker() {
        tracker = new DesyncTracker(config.ghostThresholdTicks, config.giveUpTicks);
        trackedPos = null;
    }

    /**
     * Called from the mixin at the start of {@code destroyBlock}. If the block being broken
     * is a gemstone and the desync fix is enabled, start watching that position.
     */
    public static void onBreakBlockAttempt(BlockPos pos) {
        if (config == null || !config.gemstoneDesyncFix || pos == null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        if (isGemstone(mc, pos)) {
            trackedPos = pos.immutable();
            tracker.onGemstoneBroken(key(pos), tickCounter);
            if (config.debug) {
                LOGGER.info("[gdfix] tracking gemstone break at {}", trackedPos);
            }
        }
    }

    private static void onEndTick(Minecraft mc) {
        tickCounter++;
        if (config == null || !config.gemstoneDesyncFix || !tracker.isTracking()) {
            return;
        }
        if (mc.level == null || mc.getConnection() == null
                || mc.gameMode == null || trackedPos == null) {
            tracker.clear();
            trackedPos = null;
            return;
        }
        boolean stillGemstone = isGemstone(mc, trackedPos);
        if (tracker.tick(tickCounter, stillGemstone)) {
            resync(mc, trackedPos);
        }
        if (!tracker.isTracking()) {
            trackedPos = null;
        }
    }

    /**
     * Re-drive the break on a reverted gemstone block. Using the game mode (rather than
     * hand-crafted packets) keeps Minecraft's block-prediction sequence numbers correct,
     * which is exactly what avoids introducing a fresh desync.
     */
    private static void resync(Minecraft mc, BlockPos pos) {
        Direction face = faceToward(mc, pos);
        mc.gameMode.startDestroyBlock(pos, face);
        if (config.debug) {
            LOGGER.info("[gdfix] re-syncing ghost gemstone block at {} (face {})", pos, face);
        }
    }

    private static boolean isGemstone(Minecraft mc, BlockPos pos) {
        String id = BuiltInRegistries.BLOCK.getKey(mc.level.getBlockState(pos).getBlock()).toString();
        return GemstoneBlocks.isGemstoneBlockId(id);
    }

    private static Direction faceToward(Minecraft mc, BlockPos pos) {
        HitResult hit = mc.hitResult;
        if (hit instanceof BlockHitResult blockHit && blockHit.getBlockPos().equals(pos)) {
            return blockHit.getDirection();
        }
        return Direction.UP;
    }

    private static String key(BlockPos p) {
        return p.getX() + "," + p.getY() + "," + p.getZ();
    }
}
