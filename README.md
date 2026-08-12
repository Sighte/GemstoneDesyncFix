# Gemstone Desync Fix

A small, **standalone Fabric mod for Minecraft 26.1.2** that provides two client-side
gemstone-mining fixes and nothing else. Mod version **1.0.0**.

## Features

| Toggle | What it does |
| --- | --- |
| **Break Reset Fix** | While mining, the server frequently re-sends the held item (durability / Skyblock stat updates). Vanilla sees a "new" `ItemStack` and resets the mining swing and re-equip animation, stalling the mine. This writes the incoming stack straight into `MultiPlayerGameMode.destroyingItem` and `ItemInHandRenderer.mainHandItem` for the held hotbar slot, so mining continues uninterrupted. |
| **Gemstone Desync Fix** | Gemstones render as stained-glass panes. Two parts: (1) when a pane is broken to air, neighbouring panes keep stale connection states whose hitboxes reach into the now-empty space — this calls `updateNeighbourShapes` on the air update so neighbours re-sync immediately; (2) an isolated ("default") gemstone pane otherwise collapses to a thin post with a tiny hitbox that's hard to aim at — this promotes it to the full connected shape (via `IronBarsBlock.updateShape`) so it has a full-size, easy-to-mine hitbox. |

Both default to **on** and can be toggled independently. The mechanism mirrors the
corresponding features in [nofrills](https://modrinth.com/mod/nofrills); this is a
standalone re-implementation for Minecraft 26.1.2.

## Usage

Open the GUI in-game with:

```
/gdfix
```

A small screen with two toggle buttons (and Done) appears. Changes save instantly.

Shortcuts that toggle without opening the screen:

```
/gdfix breakreset
/gdfix gemstonedesync
```

## Configuration

Stored at `config/gdfix.json` (created on first launch):

```json
{
  "breakResetFix": true,
  "gemstoneDesyncFix": true,
  "debug": false
}
```

- `debug` — logs fix activity to the client log.

## Requirements

- Minecraft **26.1.2** (the modern unobfuscated line — Java **25**)
- Fabric Loader **>= 0.19.3**
- Fabric API

## Building

Minecraft 26.1 requires a **Java 25** JDK. Point Gradle at one, then:

```bash
./gradlew build
```

Output: `build/libs/gemstone-desync-fix-1.0.0.jar` (drop into your `mods/` folder).

Run unit tests only:

```bash
./gradlew test
```

### Toolchain notes (Minecraft 26.1)

Minecraft 26.1 ships **unobfuscated** (real Mojang names, with parameters), so:

- there is **no `mappings` dependency** and the build uses the new non-remapping
  `net.fabricmc.fabric-loom` plugin (not `fabric-loom-remap`);
- all Minecraft references use official Mojang names (e.g. `MultiPlayerGameMode.destroyBlock`,
  `destroyDelay`), and the mixin needs no refmap;
- the stack is **Gradle 9.5.1 + Loom 1.17 + Fabric Loader 0.19.3 + Java 25**.

## Verification status

- ✅ Compiles against Minecraft 26.1.2 / Fabric (Java 25).
- ✅ Unit tests pass for the pure slot logic (`HotbarSlots`).
- ✅ All four mixin targets verified against the decompiled 26.1.2 source
  (`ClientPacketListener.handleContainerSetSlot`, `ClientLevel.setServerVerifiedBlockState`,
  `MultiPlayerGameMode.destroyingItem`, `ItemInHandRenderer.mainHandItem`).
- ⚠️ In-game behaviour on Hypixel SkyBlock has **not** been tested from the build environment
  (it requires connecting to the live server).
