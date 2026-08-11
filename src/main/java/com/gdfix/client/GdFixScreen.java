package com.gdfix.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * A deliberately small screen: two toggle buttons (one per fix) and a Done button.
 * Opened by {@code /gdfix}. Changes are saved immediately on each toggle.
 */
public final class GdFixScreen extends Screen {

    private static final int BUTTON_WIDTH = 220;
    private static final int BUTTON_HEIGHT = 20;

    private final Screen parent;

    public GdFixScreen(Screen parent) {
        super(Text.literal("Gemstone Desync Fix"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        GdFixConfig cfg = GdFixClient.config();
        int left = this.width / 2 - BUTTON_WIDTH / 2;
        int top = this.height / 2 - 32;

        addDrawableChild(ButtonWidget.builder(breakResetLabel(cfg), button -> {
            cfg.breakResetFix = !cfg.breakResetFix;
            cfg.save();
            button.setMessage(breakResetLabel(cfg));
        }).dimensions(left, top, BUTTON_WIDTH, BUTTON_HEIGHT).build());

        addDrawableChild(ButtonWidget.builder(gemstoneLabel(cfg), button -> {
            cfg.gemstoneDesyncFix = !cfg.gemstoneDesyncFix;
            cfg.save();
            button.setMessage(gemstoneLabel(cfg));
        }).dimensions(left, top + 24, BUTTON_WIDTH, BUTTON_HEIGHT).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), button -> close())
                .dimensions(left, top + 56, BUTTON_WIDTH, BUTTON_HEIGHT).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title,
                this.width / 2, this.height / 2 - 58, 0xFFFFFF);
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }

    private static Text breakResetLabel(GdFixConfig cfg) {
        return Text.literal("Break Reset Fix: ").append(onOff(cfg.breakResetFix));
    }

    private static Text gemstoneLabel(GdFixConfig cfg) {
        return Text.literal("Gemstone Desync Fix: ").append(onOff(cfg.gemstoneDesyncFix));
    }

    private static Text onOff(boolean value) {
        return value
                ? Text.literal("ON").formatted(Formatting.GREEN)
                : Text.literal("OFF").formatted(Formatting.RED);
    }
}
