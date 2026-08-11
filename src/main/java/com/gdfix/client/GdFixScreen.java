package com.gdfix.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * A deliberately small screen: a title, two toggle buttons (one per fix), and a Done button.
 * Opened by {@code /gdfix}. Changes are saved immediately on each toggle.
 *
 * <p>Built entirely from widgets ({@link StringWidget} + {@link Button}) added via
 * {@code addRenderableWidget}, matching Minecraft 26.1's widget-based screen rendering.
 */
public final class GdFixScreen extends Screen {

    private static final int BUTTON_WIDTH = 220;
    private static final int BUTTON_HEIGHT = 20;
    private static final int SPACING = 24;

    private final Screen parent;

    public GdFixScreen(Screen parent) {
        super(Component.literal("Gemstone Desync Fix"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        GdFixConfig cfg = GdFixClient.config();
        int left = this.width / 2 - BUTTON_WIDTH / 2;
        int top = this.height / 2 - 40;

        addRenderableWidget(new StringWidget(
                left, top - SPACING, BUTTON_WIDTH, BUTTON_HEIGHT, this.title, this.font));

        Button breakResetButton = Button.builder(breakResetLabel(cfg), button -> {
            cfg.breakResetFix = !cfg.breakResetFix;
            cfg.save();
            button.setMessage(breakResetLabel(cfg));
        }).bounds(left, top, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        addRenderableWidget(breakResetButton);

        Button gemstoneButton = Button.builder(gemstoneLabel(cfg), button -> {
            cfg.gemstoneDesyncFix = !cfg.gemstoneDesyncFix;
            cfg.save();
            button.setMessage(gemstoneLabel(cfg));
        }).bounds(left, top + SPACING, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        addRenderableWidget(gemstoneButton);

        addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose())
                .bounds(left, top + SPACING * 3, BUTTON_WIDTH, BUTTON_HEIGHT).build());
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    private static Component breakResetLabel(GdFixConfig cfg) {
        return Component.literal("Break Reset Fix: ").append(onOff(cfg.breakResetFix));
    }

    private static Component gemstoneLabel(GdFixConfig cfg) {
        return Component.literal("Gemstone Desync Fix: ").append(onOff(cfg.gemstoneDesyncFix));
    }

    private static Component onOff(boolean value) {
        return value
                ? Component.literal("ON").withStyle(ChatFormatting.GREEN)
                : Component.literal("OFF").withStyle(ChatFormatting.RED);
    }
}
