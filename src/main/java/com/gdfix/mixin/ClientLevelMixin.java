package com.gdfix.mixin;

import com.gdfix.client.GdFixClient;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Gemstone Desync Fix hook. {@code setServerVerifiedBlockState} is the single choke point
 * both single-block and section block updates funnel through. At HEAD the level still holds
 * the old state at {@code pos}, so {@link GdFixClient} can detect a gemstone (stained glass)
 * turning into air and re-sync the neighbouring panes' connection shapes.
 */
@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {

    @Inject(method = "setServerVerifiedBlockState", at = @At("HEAD"))
    private void gdfix$onServerVerifiedBlockState(BlockPos pos, BlockState blockState, int updateFlag,
                                                  CallbackInfo ci) {
        GdFixClient.onServerVerifiedBlockUpdate((Level) (Object) this, pos, blockState);
    }
}
