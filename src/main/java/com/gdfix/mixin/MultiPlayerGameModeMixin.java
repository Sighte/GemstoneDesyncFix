package com.gdfix.mixin;

import com.gdfix.client.GdFixClient;
import com.gdfix.client.GdFixConfig;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Implements both fixes on the client's block interaction manager.
 * Minecraft 26.1 ships unobfuscated, so these reference the real Mojang names.
 *
 * <ul>
 *   <li><b>Gemstone desync fix</b> — {@code destroyBlock} is invoked (inside a prediction
 *       lambda) exactly when a block is actually broken. The HEAD injection reports the
 *       still-solid position so {@link GdFixClient} can watch for the server reverting it.</li>
 *   <li><b>Break reset fix</b> — after a break, vanilla {@code continueDestroyBlock} sets
 *       {@code destroyDelay = 5}, a 5-tick stall before the next block can be mined. When the
 *       fix is enabled we rewrite that constant to {@code 0} so mining continues immediately.
 *       Both the creative and survival assignments in that method are the same cooldown.</li>
 * </ul>
 */
@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {

    @Inject(method = "destroyBlock", at = @At("HEAD"))
    private void gdfix$onDestroyBlockHead(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        GdFixClient.onBreakBlockAttempt(pos);
    }

    @ModifyConstant(method = "continueDestroyBlock", constant = @Constant(intValue = 5))
    private int gdfix$breakResetCooldown(int original) {
        GdFixConfig config = GdFixClient.config();
        return config != null && config.breakResetFix ? 0 : original;
    }
}
