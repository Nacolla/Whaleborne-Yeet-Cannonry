# Whaleborne: Yeet Cannonry! 🚀

> Load yourself into the whale's cannon, let a friend take aim, and get launched across the ocean. You *are* the ammunition.

An add-on for **[Whaleborne](https://www.curseforge.com/minecraft/mc-mods/whaleborne)** that turns its cannon into a human-cannonball launcher: ride the barrel and get fired across the ocean, by a friend at the gun, or by yourself.

> ⚠️ **Requires [Whaleborne](https://www.curseforge.com/minecraft/mc-mods/whaleborne).** This add-on does nothing on its own.

## 🎯 Features

- **Human Cannonball**: ride the barrel and get launched.
- **Gunner Seat**: a second player mounts the cannon to aim and fire.
- **Solo Launch**: no second player needed. The barrel rider can fire the cannon and launch themselves.
- **First-Person Lock**: while a gunner aims, the rider's first-person view follows the cannon (third-person stays free).
- **Head at the Muzzle**: the loaded rider's head appears at the cannon's muzzle.
- **Persistent Rider**: a loaded rider survives world saves *and* disconnects, snapping back to the muzzle on reload/reconnect even if the whale has since sailed off.

## 🎮 How to Use

1. **Become the ammo**: right-click the cannon barrel (the muzzle). Your head appears at the muzzle.
2. **Grab a gunner** *(optional)*: a second player right-clicks the cannon body to aim. No one around? You can fire yourself.
3. **Load gunpowder**: drop gunpowder in the cannon's powder slot (spent on firing).
4. **Fire**: charge and release jump. 🚀

## 📦 Versions

| Loader | Minecraft | Loader version | Java | Branch |
|---|---|---|---|---|
| Forge | 1.20.1 | 47.2.0+ | 17 | `forge-1.20.1` |
| NeoForge | 1.21.1 | 21.1.65+ | 21 | `neoforge-1.21.1` |

Both require **Whaleborne 1.0.0+**.

## 🛠️ Building

```bash
git clone -b forge-1.20.1 https://github.com/Nacolla/Whaleborne-Yeet-Cannonry.git
cd Whaleborne-Yeet-Cannonry
./gradlew build
```

- `build/libs/…-forge-1.20.1-*.jar`: SRG-reobf'd, for normal Forge runtimes (CurseForge, Modrinth, etc.).
- `build/devlibs/…-dev.jar`: Mojmap-named, for Mojmap dev environments (e.g. ModDevGradle).

## 🔧 Technical Details

All behavior is injected via Mixins into Whaleborne's `CannonEntity` / `CannonMenu` / `CannonModel` (no base-mod edits).

- **Ammo & menu**: `clicked` + `quickMoveStack` lock the head slot, and `containerChanged` ejects the rider if the head is pulled.
- **Mounting & aim**: `interact` / `tick` / `positionRider` / `getControllingPassenger` / `canAddPassenger` handle gunner priority, re-seating and muzzle placement. `CannonModel.setupAnim` interpolates barrel pitch.
- **Firing**: `fireCannon` launches the loaded rider instead of an item.
- **Persistence**: `add`/`readAdditionalSaveData` store the rider UUID, and the rider remounts on reload/reconnect.
- **Rendering**: `WhaleWidgetRenderer.render` draws the head at the muzzle.

On Forge these are written against Mojmap names and translated to SRG via a MixinGradle refmap. On NeoForge they resolve natively.

| Aspect | Forge 1.20.1 | NeoForge 1.21.1 |
|---|---|---|
| Runtime mappings | SRG (refmap) | Mojmap (native) |
| Head item data | NBT (`SkullOwner`) | DataComponents (`ResolvableProfile`) |
| PartEntity package | `net.minecraftforge.entity` | `net.neoforged.neoforge.entity` |

## 👥 Credits

Add-on by **Nacolla** · original concept by **FloofHips** · built on **[Whaleborne](https://www.curseforge.com/minecraft/mc-mods/whaleborne)**.
MIT licensed. See LICENSE. Found a bug? [Open an issue](https://github.com/Nacolla/Whaleborne-Yeet-Cannonry/issues).
