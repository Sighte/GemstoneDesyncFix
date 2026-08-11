package com.gdfix.client;

import com.gdfix.logic.DesyncTracker;
import com.gdfix.logic.GemstoneBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point and runtime coordinator for the two fixes.
 *
 * <ul>
 *   <li><b>Break reset fix</b> is applied inside
 *       {@code ClientPlayerInteractionManagerMixin} which calls {@link #config()}.</li>
 *   <li><b>Gemstone desync fix</b> is driven here: the mixin reports gemstone break
 *       attempts via {@link #onBreakBlockAttempt(BlockPos)}, and a client tick handler
 *       watches for the block being reverted by the server (a ghost) and re-drives the
 *       break through the vanilla interaction manager (which handles packet sequencing).</li>
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
     * Called from the mixin at the start of {@code breakBlock}. If the block being
     * broken is a gemstone and the desync fix is enabled, start watching that position.
     */
    public static void onBreakBlockAttempt(BlockPos pos) {
        if (config == null || !config.gemstoneDesyncFix || pos == null) {
            return;
        }
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null) {
            return;
        }
        if (isGemstone(mc, pos)) {
            trackedPos = pos.toImmutable();
            tracker.onGemstoneBroken(key(pos), tickCounter);
            if (config.debug) {
                LOGGER.info("[gdfix] tracking gemstone break at {}", trackedPos);
            }
        }
    }

    private static void onEndTick(MinecraftClient mc) {
        tickCounter++;
        if (config == null || !config.gemstoneDesyncFix || !tracker.isTracking()) {
            return;
        }
        if (mc.world == null || mc.getNetworkHandler() == null
                || mc.interactionManager == null || trackedPos == null) {
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
     * Re-drive the break on a reverted gemstone block. Using the interaction manager
     * (rather than hand-crafted packets) keeps Minecraft's block-prediction sequence
     * numbers correct, which is exactly what avoids introducing a fresh desync.
     */
    private static void resync(MinecraftClient mc, BlockPos pos) {
        Direction face = faceToward(mc, pos);
        mc.interactionManager.attackBlock(pos, face);
        if (config.debug) {
            LOGGER.info("[gdfix] re-syncing ghost gemstone block at {} (face {})", pos, face);
        }
    }

    private static boolean isGemstone(MinecraftClient mc, BlockPos pos) {
        String id = Registries.BLOCK.getId(mc.world.getBlockState(pos).getBlock()).toString();
        return GemstoneBlocks.isGemstoneBlockId(id);
    }

    private static Direction faceToward(MinecraftClient mc, BlockPos pos) {
        HitResult hit = mc.crosshairTarget;
        if (hit instanceof BlockHitResult blockHit && blockHit.getBlockPos().equals(pos)) {
            return blockHit.getSide();
        }
        return Direction.UP;
    }

    private static String key(BlockPos p) {
        return p.getX() + "," + p.getY() + "," + p.getZ();
    }
}
