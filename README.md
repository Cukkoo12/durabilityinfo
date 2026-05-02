# DurabilityInfo

**See your item's durability directly in the tooltip — numbers, percentage, and a color-coded bar.**

![DurabilityInfo Preview](https://raw.githubusercontent.com/Cukkoo12/durabilityinfo/master/foto.png)

## What it does

DurabilityInfo adds durability information to every item tooltip. No more guessing how close your tools are to breaking.

- 🔢 **Durability numbers** — e.g. `Durability: 235 / 500`
- 📊 **Percentage** — e.g. `Durability: 47%`
- 🟩 **Color-coded bar** — green → yellow → red as durability drops
- ⚙️ **Configurable** — toggle each element independently

## Preview

![Preview](https://raw.githubusercontent.com/Cukkoo12/durabilityinfo/master/foto.png)

## Configuration

Config file: `.minecraft/config/durabilityinfo.json`

| Option | Default | Description |
|--------|---------|-------------|
| `showDurabilityNumbers` | `true` | Show current / max durability |
| `showPercentage` | `true` | Show durability percentage |
| `showBar` | `true` | Show color-coded durability bar |
| `showOnUnbreakable` | `false` | Show info on unbreakable items |

If [Cloth Config](https://modrinth.com/mod/cloth-config) and [Mod Menu](https://modrinth.com/mod/modmenu) are installed, you can configure the mod in-game from the Mods screen.

## Requirements

- Minecraft 26.1.x
- [Fabric Loader](https://fabricmc.net/) ≥ 0.18.5
- [Fabric API](https://modrinth.com/mod/fabric-api)

## Optional

- [Cloth Config](https://modrinth.com/mod/cloth-config) — in-game config screen
- [Mod Menu](https://modrinth.com/mod/modmenu) — access config from the Mods list

## License

MIT — see [LICENSE](LICENSE)
