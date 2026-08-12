package com.gdfix.client;

import com.gdfix.logic.HotbarSlots;
import com.gdfix.mixin.ItemInHandRendererAccessor;
import com.gdfix.mixin.MultiPlayerGameModeAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point and runtime coordinator for the two fixes (Minecraft 26.1, Mojang names).
 *
 * <ul>
 *   <li><b>Break Reset Fix</b> — {@link #onContainerSetSlot(int, int, ItemStack)} is called
 *       from {@code ClientPacketListenerMixin} after a server slot update. If the update
 *       targets the held hotbar slot, the new stack is written into
 *       {@code MultiPlayerGameMode.destroyingItem} and {@code ItemInHandRenderer.mainHandItem}
 *       so mining does not treat it as an item change and reset.</li>
 *   <li><b>Gemstone Desync Fix</b> — {@link #onServerVerifiedBlockUpdate(Level, BlockPos, BlockState)}
 *       is called from {@code ClientLevelMixin} before a server block change is applied. When a
 *       stained-glass block becomes air, the neighbouring panes are told to re-sync their
 *       connection shapes so their hitboxes no longer block the next gemstone.</li>
 * </ul>
 */
public final class GdFixClient implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("gdfix");

    private static GdFixConfig config;

    @Override
    public void onInitializeClient() {
        config = GdFixConfig.load();
        GdFixCommand.register();
        LOGGER.info("[gdfix] Gemstone Desync Fix loaded (breakResetFix={}, gemstoneDesyncFix={})",
                config.breakResetFix, config.gemstoneDesyncFix);
    }

    public static GdFixConfig config() {
        return config;
    }

    /** Break Reset Fix — keep the mining/render item caches in sync with server slot updates. */
    public static void onContainerSetSlot(int containerId, int slot, ItemStack stack) {
        if (config == null || !config.breakResetFix) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gameMode == null) {
            return;
        }
        if (!HotbarSlots.isHeldHotbarSlot(containerId, slot, mc.player.getInventory().getSelectedSlot())) {
            return;
        }
        ((MultiPlayerGameModeAccessor) mc.gameMode).gdfix$setDestroyingItem(stack);
        ((ItemInHandRendererAccessor) mc.getEntityRenderDispatcher().getItemInHandRenderer())
                .gdfix$setMainHandItem(stack);
        if (config.debug) {
            LOGGER.info("[gdfix] break-reset synced held slot {} -> {}", slot, stack);
        }
    }

    /** Gemstone Desync Fix — re-sync neighbouring pane shapes when a gemstone becomes air. */
    public static void onServerVerifiedBlockUpdate(Level level, BlockPos pos, BlockState newState) {
        if (config == null || !config.gemstoneDesyncFix || !newState.isAir()) {
            return;
        }
        BlockState oldState = level.getBlockState(pos);
        if (isStainedGlass(oldState)) {
            // UPDATE_ALL == UPDATE_NEIGHBORS | UPDATE_CLIENTS == 3 (matches the reference impl).
            newState.updateNeighbourShapes(level, pos, Block.UPDATE_ALL);
            if (config.debug) {
                LOGGER.info("[gdfix] gemstone desync re-sync of neighbours at {}", pos);
            }
        }
    }

    private static boolean isStainedGlass(BlockState state) {
        return state.getBlock() instanceof StainedGlassBlock
                || state.getBlock() instanceof StainedGlassPaneBlock;
    }

    /**
     * Gemstone Desync Fix (pane shape). Called from {@code IronBarsBlockMixin} with the
     * result of a pane shape update. An isolated gemstone pane (stained glass with no
     * connections) is promoted to the full connected shape so it gets a full-size hitbox
     * that is easy to aim at while mining.
     */
    public static BlockState fixDefaultGemstonePane(BlockState state) {
        if (config != null && config.gemstoneDesyncFix && isDefaultPane(state)) {
            return asFullPane(state);
        }
        return state;
    }

    private static boolean isDefaultPane(BlockState state) {
        return isStainedGlass(state) && !isConnectedPane(state);
    }

    private static boolean isConnectedPane(BlockState state) {
        return state.getValue(CrossCollisionBlock.NORTH)
                || state.getValue(CrossCollisionBlock.EAST)
                || state.getValue(CrossCollisionBlock.SOUTH)
                || state.getValue(CrossCollisionBlock.WEST);
    }

    private static BlockState asFullPane(BlockState state) {
        return state.setValue(CrossCollisionBlock.NORTH, true)
                .setValue(CrossCollisionBlock.EAST, true)
                .setValue(CrossCollisionBlock.SOUTH, true)
                .setValue(CrossCollisionBlock.WEST, true);
    }
}
