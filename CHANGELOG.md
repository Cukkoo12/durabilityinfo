# Changelog

## Unreleased — 2.0.0

Development snapshot: `2.0.0-dev.1`. This section describes work in progress and is not a final release announcement.

### Added

- Canonical loader-neutral durability, configuration, visibility, alert, notification, overlay, color, layout, and preset core.
- Versioned schema-2 configuration with schema-1 migration, validation, malformed-file backups, and atomic saves.
- Compact, Vanilla+, Detailed, Bar Only, Custom, and disabled tooltip styles.
- Six-group HUD with multiple display/layout/visibility modes and extensive appearance controls.
- Native drag-and-drop HUD editor with preview, snapping, safe boundaries, nudging, resets, and rollback.
- Independent armor/held multi-level alerts driven by client ticks.
- Bounded damage/repair notifications and optional hotbar tracking.
- Hotbar, inventory, and player-owned container overlays.
- Native essentials-first settings screen, focused Advanced categories, and Minimal, Vanilla+, Mining, Combat, and Detailed presets.
- Shared automated tests and six-project safety/parity verification.

### Changed

- Configuration now loads once and remains in memory; render and tooltip paths perform no file IO.
- Durability calculations and colors are shared across every feature.
- HUD formatted values are cached and rebuilt only after item, durability, or configuration changes.
- Fabric uses only the required Fabric Item API module and exact optional Mod Menu versions.
- Forge/NeoForge client registration is isolated from dedicated-server class loading.
- All six artifacts use unique loader/Minecraft names and version `2.0.0-dev.1`.
- Settings now use a responsive centered panel, human-readable values, collapsible alert channels, keyboard-accessible scrolling, and a focusable/narratable HUD preview.
- Vanilla+ defaults now retain the Vanilla+ tooltip style, show the HUD immediately at 85% scale, and keep an exact 10% primary warning after validation.

### Removed

- Cloth Config repositories, dependencies, suggestions, imports, screen factory, and documentation.
- Render-loop alert detection, global warning cooldown, render-triggered sounds, and load-on-demand configuration.

### Known development limitations

- In-game visual, narration, GUI-scale, and third-party compatibility testing remains required before final 2.0.0.
