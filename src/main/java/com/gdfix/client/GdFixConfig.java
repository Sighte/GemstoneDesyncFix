package com.gdfix.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Persistent, human-editable configuration stored at {@code config/gdfix.json}.
 * Only plain fields are serialised (via Gson, which Minecraft already bundles).
 */
public final class GdFixConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH =
            FabricLoader.getInstance().getConfigDir().resolve("gdfix.json");

    /** Zero the vanilla post-break cooldown so mining can continue immediately. */
    public boolean breakResetFix = true;

    /** Re-drive breaking on gemstone blocks the server reverts back into place. */
    public boolean gemstoneDesyncFix = true;

    /** Ticks a reverted gemstone block must persist before it counts as a ghost. */
    public int ghostThresholdTicks = 3;

    /** Ticks after a re-sync before giving up on a stubborn ghost block. */
    public int giveUpTicks = 12;

    /** Log tracking / re-sync activity to the client log. */
    public boolean debug = false;

    public static GdFixConfig load() {
        try {
            if (Files.exists(PATH)) {
                GdFixConfig cfg = GSON.fromJson(Files.readString(PATH), GdFixConfig.class);
                if (cfg != null) {
                    return cfg.sanitised();
                }
            }
        } catch (Exception e) {
            GdFixClient.LOGGER.warn("[gdfix] Could not read {}, using defaults", PATH, e);
        }
        GdFixConfig cfg = new GdFixConfig();
        cfg.save();
        return cfg;
    }

    public void save() {
        try {
            Files.createDirectories(PATH.getParent());
            Files.writeString(PATH, GSON.toJson(this));
        } catch (IOException e) {
            GdFixClient.LOGGER.warn("[gdfix] Could not write {}", PATH, e);
        }
    }

    private GdFixConfig sanitised() {
        if (ghostThresholdTicks < 1) {
            ghostThresholdTicks = 1;
        }
        if (giveUpTicks < ghostThresholdTicks) {
            giveUpTicks = ghostThresholdTicks * 4;
        }
        return this;
    }
}
