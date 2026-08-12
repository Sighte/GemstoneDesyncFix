package com.gdfix.mixin;

import com.gdfix.client.GdFixClient;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Break Reset Fix hook: after the client applies a server slot update, notify
 * {@link GdFixClient} so a change to the currently-held hotbar item can be pushed straight
 * into the mining/render caches instead of resetting the swing.
 */
@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {

    @Inject(method = "handleContainerSetSlot", at = @At("TAIL"))
    private void gdfix$onContainerSetSlot(ClientboundContainerSetSlotPacket packet, CallbackInfo ci) {
        GdFixClient.onContainerSetSlot(packet.getContainerId(), packet.getSlot(), packet.getItem());
    }
}
