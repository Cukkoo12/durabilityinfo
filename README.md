# DurabilityInfo 2.0

DurabilityInfo is a fully client-side Minecraft mod for understanding equipment durability without changing item behavior. The current release is **2.0.0**.

![HUD and Tooltip Preview](screenshots/ingame.png)

## 2.0 feature overview

### Tooltip styles

Damageable items use one shared, integer-safe durability calculation and consistent colors everywhere. Available styles are Off, Compact, Vanilla+, Detailed, Bar Only, and Custom. Custom mode keeps individual numbers, percentage, and text-bar toggles for migrated 1.x users. Options include remaining or damage-taken values, Unbreakable output, bar width, and hiding fully repaired items.

### Customizable HUD

Helmet, chestplate, leggings, boots, main hand, and offhand groups can be enabled independently. The HUD supports mini bar, percentage, remaining, remaining/max, and combined displays; vertical, horizontal, and compact-grid layouts; scale, spacing, background, icons, text shadow, alignment, equipment ordering, anchors, and offsets.

The native HUD editor provides a simulated six-slot preview, mouse dragging, vertical/horizontal layout selection, scale and background controls, optional edge/center snapping, safe-screen boundaries, keyboard arrow nudging, resize clamping, position reset, and Apply/Cancel/Done behavior. Cancel and Escape restore changes made since the last Apply.

### Smart visibility

Visibility modes are Always, Damaged Only, Below Threshold, Recently Changed, and Smart. Smart mode briefly shows changed damaged items, keeps critical equipment visible, and hides healthy unchanged items. Creative, Spectator, debug-screen, container-screen, hidden-game-HUD, held-only, and armor-only filters are configurable.

### Multi-level alerts

Armor and held items have independent Warning, Low, Critical, and Last Chance thresholds. Each threshold can be disabled. Sound, action bar, chat, and HUD flash channels are independently configurable.

Alerts run from a client tick tracker, not rendering. They trigger only on downward crossings, reset after repair or replacement, track slots independently, continue when DurabilityInfo's HUD is disabled, and never swap items or prevent breakage.

### Damage and repair notifications

Optional bounded notifications distinguish damage from repair, merge rapid repeated changes, retain at most three visible entries, and can track equipped armor, both hands, and optionally the whole hotbar. They clear across player/world replacement, disconnect, dimension transition, and shutdown.

### Hotbar and inventory overlays

Hotbar, player-inventory, and safely identified player-owned container slots support Off, Percentage, Remaining, Mini Bar, and Colored Border modes. Full-durability hiding, threshold filtering, text shadow, scale, and border thickness are configurable. Vanilla durability bars remain enabled unless the user explicitly selects replacement.

### Presets

Minimal, Vanilla+, Mining, Combat, and Detailed presets provide coherent starting points. Preset selection shows a confirmation preview and only saves through Apply or Done. Editing an individual controlled value switches the draft to Custom without discarding it.

## Native settings access

The settings screen opens on a deliberately small essentials page with Tooltip Style, Show HUD, Warning Level, Edit HUD Position, and Advanced Settings. Expert options remain available in six focused Advanced categories: Tooltip, HUD, Warnings, Indicators, Popups, and Presets. Native cycle controls, sliders, toggles, responsive scrolling, keyboard focus, narration, and Apply/Cancel/Done semantics are preserved.

- **Fabric:** Mod Menu is optional. Its Configure button opens DurabilityInfo's native screen when installed. Without Mod Menu, edit the JSON manually.
- **Forge:** Open DurabilityInfo from the Mods screen and choose Config.
- **NeoForge:** Open DurabilityInfo from the Mods screen and choose Config.

There is no settings key binding and no mandatory GUI/configuration library. Cloth Config support and dependencies were removed for 2.0.

## Configuration

Path: `.minecraft/config/durabilityinfo.json`

The schema version is 2. Configuration loads once during client initialization and remains in memory during tooltip, HUD, alert, notification, hotbar, and inventory work. Saves are pretty-printed and atomic. Missing fields use defaults, unknown fields are ignored, numeric values are validated, and malformed files are preserved with a timestamped `.corrupt` suffix before safe defaults are restored.

Existing 1.x fields are migrated to their closest 2.0 equivalents. New installations use the Vanilla+ tooltip style, an always-visible HUD at 85% scale, a 10% primary warning, and disabled overlays, chat warnings, and damage/repair popups. Empty slots and non-damageable items never create HUD rows.

## Supported builds

| Loader | Minecraft |
|---|---|
| Fabric | 26.1.2 |
| Fabric | 26.2 |
| Forge | 26.1.2 |
| Forge | 26.2 |
| NeoForge | 26.1.2 |
| NeoForge | 26.2 |

All six builds use Java 25, mod ID `durabilityinfo`, version `2.0.0`, identical schema/defaults/presets/translations, and narrow 26.1.2/26.2 rendering adapters.

## Client-side safety

DurabilityInfo does not modify ItemStack durability, cancel damage, alter inventories, send packets, require server installation, change gameplay rules, modify attributes, or force chunk loading. Forge and NeoForge isolate client GUI/event registration from their main mod class.

## Verification

The shared tests cover migration, validation, corrupt backup, atomic save, no access-time IO, presets, durability math, tooltip styles, layouts, visibility, alerts, notification bounds/merging, overlays, and lifecycle resets. `tools/verify-2.0.ps1` checks dependency cleanup, safety, source parity, mixin parity, translations, metadata, and forbidden operations.

Manual in-game verification is still required before a final 2.0.0 release for visual placement, GUI scaling, narration quality, compatibility with other HUD/container mods, and every notification channel.

## License

MIT — see [LICENSE](LICENSE).
