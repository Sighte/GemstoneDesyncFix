package com.gdfix.client;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;

/**
 * Registers the {@code /gdfix} client command. With no arguments it opens the small toggle
 * GUI; {@code /gdfix breakreset} and {@code /gdfix gemstonedesync} flip a toggle without
 * opening the screen.
 */
public final class GdFixCommand {

    private GdFixCommand() {
    }

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(literal("gdfix")
                        .executes(ctx -> {
                            openScreen();
                            return 1;
                        })
                        .then(literal("breakreset").executes(ctx -> {
                            GdFixConfig c = GdFixClient.config();
                            c.breakResetFix = !c.breakResetFix;
                            c.save();
                            return 1;
                        }))
                        .then(literal("gemstonedesync").executes(ctx -> {
                            GdFixConfig c = GdFixClient.config();
                            c.gemstoneDesyncFix = !c.gemstoneDesyncFix;
                            c.save();
                            return 1;
                        }))));
    }

    private static void openScreen() {
        Minecraft mc = Minecraft.getInstance();
        // Defer to the end of the tick so the command's own chat screen is closed first.
        mc.execute(() -> mc.setScreen(new GdFixScreen(null)));
    }
}
