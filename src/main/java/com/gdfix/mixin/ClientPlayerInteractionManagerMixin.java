package com.gdfix.mixin;

import com.gdfix.client.GdFixClient;
import com.gdfix.client.GdFixConfig;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Implements both fixes on the client's block interaction manager.
 *
 * <ul>
 *   <li><b>Gemstone desync fix</b> — {@code breakBlock} is invoked (inside a sequenced
 *       packet) exactly when a block is actually broken. The HEAD injection reports the
 *       still-solid position so {@link GdFixClient} can watch for the server reverting it.</li>
 *   <li><b>Break reset fix</b> — after a break, vanilla
 *       {@code updateBlockBreakingProgress} sets {@code blockBreakingCooldown = 5}, a
 *       5-tick stall before the next block can be mined. When the fix is enabled we
 *       rewrite that constant to {@code 0} so mining continues immediately. Both the
 *       creative and survival assignments of {@code 5} are the same cooldown, so
 *       replacing every occurrence is correct.</li>
 * </ul>
 */
@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {

    @Inject(method = "breakBlock", at = @At("HEAD"))
    private void gdfix$onBreakBlockHead(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        GdFixClient.onBreakBlockAttempt(pos);
    }

    @ModifyConstant(method = "updateBlockBreakingProgress", constant = @Constant(intValue = 5))
    private int gdfix$breakResetCooldown(int original) {
        GdFixConfig config = GdFixClient.config();
        return config != null && config.breakResetFix ? 0 : original;
    }
}
