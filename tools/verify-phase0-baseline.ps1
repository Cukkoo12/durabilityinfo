$ErrorActionPreference = 'Stop'

$projects = @(
    'fabric/26.1.2', 'fabric/26.2',
    'forge/26.1.2', 'forge/26.2',
    'neoforge/26.1.2', 'neoforge/26.2'
)

foreach ($project in $projects) {
    if (-not (Test-Path -LiteralPath "$project/src/main/java")) {
        throw "Missing source layout: $project"
    }
    if (-not (Test-Path -LiteralPath "$project/src/main/resources/durabilityinfo.mixins.json")) {
        throw "Missing mixin metadata: $project"
    }
}

$java = Get-ChildItem fabric,forge,neoforge -Recurse -File -Filter *.java |
    Where-Object { $_.FullName -notmatch '[\\/](build|\.gradle|run)[\\/]' }
$content = ($java | ForEach-Object { Get-Content -Raw -LiteralPath $_.FullName }) -join "`n"

foreach ($required in @('ModConfig.load()', 'extractRenderState', 'ItemTooltip')) {
    if (-not $content.Contains($required)) {
        throw "Baseline hook not found: $required"
    }
}

$loadCount = ([regex]::Matches($content, 'ModConfig\.load\(\)')).Count
$arrayListCount = ([regex]::Matches($content, 'new ArrayList')).Count
$renderSoundCount = ([regex]::Matches($content, 'playSound\(SoundEvents\.ITEM_BREAK')).Count

if ($loadCount -lt 12) { throw "Expected duplicated baseline config IO, found $loadCount calls" }
if ($arrayListCount -ne 6) { throw "Expected six per-frame ArrayList allocations, found $arrayListCount" }
if ($renderSoundCount -ne 6) { throw "Expected six render-coupled alerts, found $renderSoundCount" }

Write-Output "Phase 0 baseline verified: projects=6, ModConfig.load calls=$loadCount, HUD ArrayLists=$arrayListCount, render sounds=$renderSoundCount"
