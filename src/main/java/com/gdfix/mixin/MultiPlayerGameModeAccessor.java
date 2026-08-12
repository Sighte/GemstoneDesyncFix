package com.gdfix.mixin;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes the private {@code destroyingItem} field of {@link MultiPlayerGameMode}.
 *
 * <p>{@code destroyingItem} is the stack captured when the current break started;
 * {@code continueDestroyBlock} keeps mining only while it still matches the held item
 * ({@code ItemStack.isSameItemSameComponents}). When the server pushes a fresh stack for
 * the held item (e.g. a durability or Skyblock update), the comparison fails and mining
 * resets. Break Reset Fix rewrites this field to the new stack so mining continues.
 */
@Mixin(MultiPlayerGameMode.class)
public interface MultiPlayerGameModeAccessor {

    @Accessor("destroyingItem")
    void gdfix$setDestroyingItem(ItemStack stack);
}
