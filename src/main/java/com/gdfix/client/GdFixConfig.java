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

    /** Keep the mining/render item caches in sync with server slot updates. */
    public boolean breakResetFix = true;

    /** Re-sync neighbouring pane shapes when a gemstone (stained glass) becomes air. */
    public boolean gemstoneDesyncFix = true;

    /** Log fix activity to the client log. */
    public boolean debug = false;

    public static GdFixConfig load() {
        try {
            if (Files.exists(PATH)) {
                GdFixConfig cfg = GSON.fromJson(Files.readString(PATH), GdFixConfig.class);
                if (cfg != null) {
                    return cfg;
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
}
