# Gemstone Desync Fix

A small, **standalone Fabric mod for Minecraft 26.1.2** that provides two client-side
gemstone-mining fixes and nothing else. Mod version **1.0.0**.

> Clean-room reimplementation from the underlying client/server block mechanics.
> Contains no third-party mod code.

## Features

| Toggle | What it does |
| --- | --- |
| **Break Reset Fix** | After you break a block, vanilla forces a 5-tick `destroyDelay` before the next block can be mined. This rewrites that cooldown to `0` so mining continues immediately with no stall. |
| **Gemstone Desync Fix** | When you break a gemstone (rendered as stained glass / panes) but the server reverts it back into place — a "ghost block" — the mod detects the block reappearing and re-drives the break through the vanilla game mode (which keeps Minecraft's packet-sequence numbers correct, so it resolves the desync instead of causing a new one). |

Both default to **on** and can be toggled independently.

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
  "ghostThresholdTicks": 3,
  "giveUpTicks": 12,
  "debug": false
}
```

- `ghostThresholdTicks` — ticks a reverted gemstone must persist before it counts as a ghost and a re-sync is sent.
- `giveUpTicks` — ticks after a re-sync before giving up on a stubborn ghost so tracking can restart.
- `debug` — logs tracking / re-sync activity to the client log.

Config tuning values are read at startup; restart the client after editing the JSON by hand.

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
- ✅ 12 unit tests pass for the pure logic (`DesyncTracker`, `GemstoneBlocks`).
- ✅ Mixin targets verified against the decompiled 26.1.2 source
  (`destroyBlock`, `continueDestroyBlock`, `destroyDelay`).
- ⚠️ In-game behaviour on Hypixel SkyBlock has **not** been tested from the build environment
  (it requires connecting to the live server). The `ghostThresholdTicks` / `giveUpTicks`
  values may want tuning against real server latency.
