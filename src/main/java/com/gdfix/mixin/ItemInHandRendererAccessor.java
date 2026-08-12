package com.gdfix.mixin;

import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes the private {@code mainHandItem} field of {@link ItemInHandRenderer}.
 *
 * <p>The renderer plays a re-equip animation whenever this cached stack differs from the
 * held item. Break Reset Fix rewrites it to the server's new stack so the mining swing is
 * not interrupted by a spurious re-equip.
 */
@Mixin(ItemInHandRenderer.class)
public interface ItemInHandRendererAccessor {

    @Accessor("mainHandItem")
    void gdfix$setMainHandItem(ItemStack stack);
}
