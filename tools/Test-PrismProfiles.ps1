[CmdletBinding()]
param(
    [string]$PrismRoot = (Join-Path $env:APPDATA 'PrismLauncher'),
    [string]$BeautifulName = 'DLSS Beautiful 26.2',
    [string]$PerformanceName = 'DLSS Performance 26.2'
)

$ErrorActionPreference = 'Stop'
$instances = Join-Path $PrismRoot 'instances'
$beautiful = Join-Path $instances $BeautifulName
$performance = Join-Path $instances $PerformanceName

function Assert-True {
    param([bool]$Condition, [string]$Message)
    if (-not $Condition) { throw $Message }
}

function Mod-Matches {
    param([string]$Instance, [string]$Pattern)
    @(Get-ChildItem -LiteralPath (Join-Path $Instance 'minecraft\mods') -File |
        Where-Object { $_.Name -like $Pattern })
}

foreach ($profile in @($beautiful, $performance)) {
    Assert-True (Test-Path -LiteralPath $profile -PathType Container) "Missing profile: $profile"
    # Profiles can gain user-created or imported worlds after creation. The
    # creation script's copy allowlist, not a live saves directory, guards
    # against copying worlds from the source profile.
    $options = Get-Content -LiteralPath (Join-Path $profile 'minecraft\options.txt') -Raw
    Assert-True ($options -match '(?m)^preferredGraphicsBackend:"vulkan"\r?$') "$profile is not configured for Vulkan"
}

Assert-True ((Mod-Matches $beautiful 'caustica-*.jar').Count -eq 1) 'Beautiful must enable exactly one Caustica jar'
Assert-True ((Mod-Matches $beautiful 'iris-*.jar').Count -eq 0) 'Beautiful must not enable Iris'
$config = Get-Content -LiteralPath (Join-Path $beautiful 'minecraft\config\caustica.toml') -Raw
Assert-True ($config -match '(?s)\[dlss-rr\].*?enabled = true') 'Beautiful must enable DLSS RR'
Assert-True ($config -match '(?s)\[frame-generation\].*?enabled = true') 'Beautiful must enable native FG'
Assert-True ($config -match '(?s)\[reflex\].*?enabled = true') 'Beautiful must enable Reflex'

Assert-True ((Mod-Matches $performance 'caustica-*.jar').Count -eq 0) 'Performance must not enable Caustica'
Assert-True ((Mod-Matches $performance 'iris-*.jar').Count -eq 0) 'Performance must not enable Iris'
Assert-True ((Mod-Matches $performance 'sodium-fabric-*.jar').Count -eq 1) 'Performance must enable exactly one Sodium jar'
$instanceCfg = Get-Content -LiteralPath (Join-Path $performance 'instance.cfg') -Raw
Assert-True ($instanceCfg -match '(?m)^JavaPath=.+/javaw-dlss-performance\.exe\r?$') 'Performance must use its dedicated Java alias'

Write-Host 'PASS: both Prism profiles have the expected renderer, DLSS, and isolation settings.'
