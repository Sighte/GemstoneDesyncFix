package com.gdfix.mixin;

import com.gdfix.client.GdFixClient;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Second half of the Gemstone Desync Fix. Gemstone crystals are stained-glass panes;
 * an isolated pane collapses to the thin "post" shape, whose tiny hitbox is awkward to
 * aim at while mining. This rewrites the shape-update result so an isolated ("default")
 * gemstone pane takes the full connected shape instead, giving it a full-size hitbox.
 *
 * <p>{@code StainedGlassPaneBlock extends IronBarsBlock}, so the mixin targets
 * {@link IronBarsBlock#updateShape}.
 */
@Mixin(IronBarsBlock.class)
public abstract class IronBarsBlockMixin {

    @ModifyReturnValue(method = "updateShape", at = @At("RETURN"))
    private BlockState gdfix$fullShapeForIsolatedGemstone(BlockState original) {
        return GdFixClient.fixDefaultGemstonePane(original);
    }
}
