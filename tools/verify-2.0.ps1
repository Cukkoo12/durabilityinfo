$ErrorActionPreference = 'Stop'

$projects = @(
    @{ Path = 'fabric/26.1.2'; Loader = 'fabric'; Minecraft = '26.1.2' },
    @{ Path = 'fabric/26.2'; Loader = 'fabric'; Minecraft = '26.2' },
    @{ Path = 'forge/26.1.2'; Loader = 'forge'; Minecraft = '26.1.2' },
    @{ Path = 'forge/26.2'; Loader = 'forge'; Minecraft = '26.2' },
    @{ Path = 'neoforge/26.1.2'; Loader = 'neoforge'; Minecraft = '26.1.2' },
    @{ Path = 'neoforge/26.2'; Loader = 'neoforge'; Minecraft = '26.2' }
)

foreach ($project in $projects) {
    $properties = Get-Content -Raw -LiteralPath "$($project.Path)/gradle.properties"
    if ($properties -notmatch '(?m)^mod_version=2\.0\.0$') { throw "Wrong release version: $($project.Path)" }
    $build = Get-Content -Raw -LiteralPath "$($project.Path)/build.gradle"
    foreach ($source in @('../../shared/src/main/java', '../../minecraft/common/src/main/java', '../../minecraft/${project.minecraft_version}/src/main/java')) {
        if (-not $build.Contains($source)) { throw "Missing canonical source path $source in $($project.Path)" }
    }
    $expectedJar = "durabilityinfo-`${project.mod_version}-$($project.Loader)-`${project.minecraft_version}.jar"
    if (-not $build.Contains($expectedJar)) { throw "Missing unique artifact name in $($project.Path)" }
    $mixins = Get-Content -Raw -LiteralPath "$($project.Path)/src/main/resources/durabilityinfo.mixins.json" | ConvertFrom-Json
    $expectedMixins = @('DurabilityInfoHUDMixin', 'MinecraftClientMixin', 'HotbarOverlayMixin', 'ContainerOverlayMixin', 'ItemStackVanillaBarMixin')
    if ((Compare-Object @($mixins.client) $expectedMixins).Count -ne 0) { throw "Mixin parity failure: $($project.Path)" }
}

$scanRoots = @('shared', 'minecraft', 'fabric', 'forge', 'neoforge')
$cloth = rg -n -i 'cloth.?config|shedaniel' $scanRoots -g '!**/build/**' -g '!**/.gradle/**' -g '!**/run/**'
if ($LASTEXITCODE -eq 0) { throw "Cloth Config remains: $cloth" }
$latest = rg -n 'latest\.release' $scanRoots -g '*.gradle' -g '*.properties' -g '*.json'
if ($LASTEXITCODE -eq 0) { throw "Unpinned dependency remains: $latest" }
$keys = rg -n -i 'KeyMapping|KeyBinding|registerKey|keybind' $scanRoots -g '*.java' -g '*.json' -g '!**/build/**'
if ($LASTEXITCODE -eq 0) { throw "Unexpected key binding: $keys" }
$packets = rg -n 'sendPacket|Serverbound|Clientbound|PacketDistributor|SimpleChannel|CustomPacket' $scanRoots -g '*.java' -g '!**/build/**'
if ($LASTEXITCODE -eq 0) { throw "Networking operation found: $packets" }
$writes = rg -n '\.(setDamageValue|hurtAndBreak|setItem|setChanged|setCount)\(' minecraft shared fabric forge neoforge -g '*.java' -g '!**/build/**'
if ($LASTEXITCODE -eq 0) { throw "Gameplay/inventory write found: $writes" }
$renderIo = rg -n 'Files\.|JsonParser|Gson' minecraft/common/src/main/java/com/cukkoo/durabilityinfo/client -g '*.java'
if ($LASTEXITCODE -eq 0) { throw "Render/tooltip IO or JSON work found: $renderIo" }
$unexpectedDiskIo = rg -n 'Files\.(read|write|move|create|delete|exists|notExists)' $scanRoots -g '*.java' -g '!DurabilityInfoConfigManager.java' -g '!**/test/**' -g '!**/build/**' -g '!**/run/**'
if ($LASTEXITCODE -eq 0) { throw "Config or filesystem IO escaped the config manager: $unexpectedDiskIo" }
$initializers = @(rg -n 'DurabilityInfoConfigManager\.initialize\(' fabric forge neoforge -g '*.java' -g '!**/build/**' -g '!**/run/**')
if ($initializers.Count -ne 6) { throw "Expected six client config initialization call sites, found $($initializers.Count)" }
$renderCollections = rg -n 'new (ArrayList|HashMap|LinkedList)|\.stream\(' minecraft/common/src/main/java/com/cukkoo/durabilityinfo/client/HudRenderer.java
if ($LASTEXITCODE -eq 0) { throw "Per-frame HUD collection work found: $renderCollections" }
$renderSounds = rg -n 'playSound' minecraft/common/src/main/java/com/cukkoo/durabilityinfo/client/HudRenderer.java minecraft/common/src/main/java/com/cukkoo/durabilityinfo/client/SlotOverlayRenderer.java
if ($LASTEXITCODE -eq 0) { throw "Render-triggered sound found: $renderSounds" }

$fabricMetadata = Get-ChildItem fabric -Recurse -File -Filter fabric.mod.json
foreach ($file in $fabricMetadata) {
    $metadata = Get-Content -Raw -LiteralPath $file.FullName | ConvertFrom-Json
    if ($metadata.environment -ne 'client') { throw "Fabric environment is not client: $($file.FullName)" }
    if ($metadata.suggests.PSObject.Properties.Name -contains 'cloth-config2') { throw "Cloth suggestion remains: $($file.FullName)" }
    if (-not ($metadata.suggests.PSObject.Properties.Name -contains 'modmenu')) { throw "Optional Mod Menu suggestion missing: $($file.FullName)" }
}

$language = Get-Content -Raw -LiteralPath 'minecraft/common/src/main/resources/assets/durabilityinfo/lang/en_us.json' | ConvertFrom-Json
if (@($language.PSObject.Properties).Count -lt 90) { throw 'Canonical translation set is unexpectedly incomplete' }
$requiredUiKeys = @(
    'durabilityinfo.config.subtitle',
    'durabilityinfo.config.advanced_title',
    'durabilityinfo.config.section.tooltip',
    'durabilityinfo.config.section.hud',
    'durabilityinfo.config.section.warnings',
    'durabilityinfo.config.section.overlays',
    'durabilityinfo.config.section.notifications',
    'durabilityinfo.config.section.presets',
    'durabilityinfo.config.option.tooltip_style',
    'durabilityinfo.config.option.show_hud',
    'durabilityinfo.config.option.warning_level',
    'durabilityinfo.config.option.edit_hud_position',
    'durabilityinfo.config.option.advanced_settings',
    'durabilityinfo.config.hud_editor.layout_help',
    'durabilityinfo.config.hud_editor.scale_help',
    'durabilityinfo.config.hud_editor.background_help',
    'durabilityinfo.value.vanilla_plus',
    'durabilityinfo.value.remaining_and_max',
    'durabilityinfo.value.damaged_only',
    'durabilityinfo.preset.minimal.description',
    'durabilityinfo.preset.vanilla_plus.description',
    'durabilityinfo.preset.mining.description',
    'durabilityinfo.preset.combat.description',
    'durabilityinfo.preset.detailed.description',
    'durabilityinfo.config.preset.summary.tooltip',
    'durabilityinfo.config.preset.summary.hud_visibility',
    'durabilityinfo.config.preset.summary.alerts',
    'durabilityinfo.config.preset.summary.overlays',
    'durabilityinfo.config.preset.summary.notifications'
    'durabilityinfo.notification.damage'
    'durabilityinfo.notification.damage_repeated'
    'durabilityinfo.notification.repair'
    'durabilityinfo.notification.repair_repeated'
    'durabilityinfo.alert.message'
    'durabilityinfo.alert.level.warning'
    'durabilityinfo.alert.level.low'
    'durabilityinfo.alert.level.critical'
    'durabilityinfo.alert.level.last_chance'
)
foreach ($key in $requiredUiKeys) {
    if (-not ($language.PSObject.Properties.Name -contains $key)) { throw "Missing required UI translation: $key" }
}
$presetDescriptions = @{
    'durabilityinfo.preset.minimal.description' = 'Shows only essential durability information with minimal screen clutter.'
    'durabilityinfo.preset.vanilla_plus.description' = 'Adds useful durability details while keeping the interface close to vanilla.'
    'durabilityinfo.preset.mining.description' = 'Focuses on tools, hotbar durability and early warnings during long mining sessions.'
    'durabilityinfo.preset.combat.description' = 'Emphasizes armor, weapons, offhand items and stronger critical warnings.'
    'durabilityinfo.preset.detailed.description' = 'Displays the full HUD, detailed tooltips, overlays and multi-level alerts.'
}
foreach ($entry in $presetDescriptions.GetEnumerator()) {
    if ($language.($entry.Key) -cne $entry.Value) { throw "Wrong preset description: $($entry.Key)" }
}
$screenSource = Get-Content -Raw -LiteralPath 'minecraft/common/src/main/java/com/cukkoo/durabilityinfo/screen/BaseConfigScreen.java'
$rowMethods = 'mainEnumRow|mainToggleRow|mainSliderRow|mainActionRow|sectionRow|toggleRow|enumRow|intRow|doubleRow|actionRow'
$rowPattern = '(?:' + $rowMethods + ')\("([^"]+)"'
$rowLabels = [regex]::Matches($screenSource, $rowPattern) | ForEach-Object { $_.Groups[1].Value } | Sort-Object -Unique
foreach ($label in $rowLabels) {
    $normalized = ($label.ToLowerInvariant() -replace '[^a-z0-9]+', '_').Trim('_')
    $key = "durabilityinfo.config.option.$normalized"
    if (-not ($language.PSObject.Properties.Name -contains $key)) { throw "Missing translated UI label: $label ($key)" }
}
$rawEnumLabels = rg -n 'Component\.literal\([^\r\n]*\.name\(\)' minecraft/common/src/main/java/com/cukkoo/durabilityinfo/screen -g '*.java'
if ($LASTEXITCODE -eq 0) { throw "Raw enum label found in settings UI: $rawEnumLabels" }
$runtimeEnglish = rg -n 'Component\.literal\([^\r\n]*(Durability|Repaired)|replace\(''_''[^\r\n]*alert' minecraft/common/src/main/java/com/cukkoo/durabilityinfo/client -g '*.java'
if ($LASTEXITCODE -eq 0) { throw "Hard-coded runtime English remains: $runtimeEnglish" }
$hudRenderer = Get-Content -Raw -LiteralPath 'minecraft/common/src/main/java/com/cukkoo/durabilityinfo/client/HudRenderer.java'
$hudEditor = Get-Content -Raw -LiteralPath 'minecraft/common/src/main/java/com/cukkoo/durabilityinfo/screen/BaseHudLayoutEditorScreen.java'
foreach ($sharedGeometryCall in @('HudGeometry.calculate', 'HudGeometry.value')) {
    if (-not $hudRenderer.Contains($sharedGeometryCall)) { throw "Live HUD is missing shared geometry call: $sharedGeometryCall" }
    if (-not $hudEditor.Contains($sharedGeometryCall)) { throw "HUD preview is missing shared geometry call: $sharedGeometryCall" }
}
if ((Get-ChildItem fabric,forge,neoforge -Recurse -File -Filter en_us.json |
        Where-Object { $_.FullName -match '[\\/]src[\\/]main[\\/]' } | Measure-Object).Count -ne 0) {
    throw 'Loader-specific translations would break parity'
}

$placeholders = rg -n -i 'TODO|placeholder|not implemented' shared minecraft fabric forge neoforge -g '*.java' -g '*.json' -g '*.toml' -g '!**/build/**'
if ($LASTEXITCODE -eq 0) { throw "Placeholder remains: $placeholders" }

Write-Output 'DurabilityInfo 2.0 source, dependency, safety, metadata, mixin, translation, and parity checks passed.'
$global:LASTEXITCODE = 0
