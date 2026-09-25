[CmdletBinding(SupportsShouldProcess)]
param(
    [string]$PrismRoot = (Join-Path $env:APPDATA 'PrismLauncher'),
    [string]$BaseInstance = 'Fabulously Optimized',
    [string]$BeautifulName = 'DLSS Beautiful 26.2',
    [string]$PerformanceName = 'DLSS Performance 26.2'
)

$ErrorActionPreference = 'Stop'
$source = Join-Path (Join-Path $PrismRoot 'instances') $BaseInstance
if (-not (Test-Path -LiteralPath $source -PathType Container)) {
    throw "Prism base instance not found: $source"
}

$minecraftSource = Join-Path $source 'minecraft'
if (-not (Test-Path -LiteralPath $minecraftSource -PathType Container)) {
    throw "Minecraft directory not found: $minecraftSource"
}

$repoRoot = Split-Path -Parent $PSScriptRoot
$beautifulConfig = Join-Path $repoRoot 'profiles\beautiful\caustica.toml'

function Write-Utf8NoBom {
    param([string]$Path, [string[]]$Content)
    $text = if ($Content -is [array]) { $Content -join [Environment]::NewLine } else { [string]$Content }
    [IO.File]::WriteAllText($Path, $text + [Environment]::NewLine, [Text.UTF8Encoding]::new($false))
}

function Copy-ProfileBase {
    param([string]$Name)

    $target = Join-Path (Join-Path $PrismRoot 'instances') $Name
    if (Test-Path -LiteralPath $target) {
        throw "Refusing to overwrite existing Prism instance: $target"
    }
    if (-not $PSCmdlet.ShouldProcess($target, "Create Prism profile from '$BaseInstance'")) {
        return $null
    }

    New-Item -ItemType Directory -Path $target | Out-Null
    Copy-Item -LiteralPath (Join-Path $source 'mmc-pack.json') -Destination $target
    Copy-Item -LiteralPath (Join-Path $source 'instance.cfg') -Destination $target

    $minecraftTarget = Join-Path $target 'minecraft'
    New-Item -ItemType Directory -Path $minecraftTarget | Out-Null
    foreach ($directory in @('mods', 'config', 'resourcepacks', 'shaderpacks', 'caustica-ngx')) {
        $from = Join-Path $minecraftSource $directory
        if (Test-Path -LiteralPath $from) {
            Copy-Item -LiteralPath $from -Destination $minecraftTarget -Recurse
        }
    }
    foreach ($file in @('options.txt', 'servers.dat', 'servers.dat_old')) {
        $from = Join-Path $minecraftSource $file
        if (Test-Path -LiteralPath $from -PathType Leaf) {
            Copy-Item -LiteralPath $from -Destination $minecraftTarget
        }
    }

    $instanceCfg = Join-Path $target 'instance.cfg'
    $cfg = Get-Content -LiteralPath $instanceCfg -Raw
    $cfg = $cfg -replace '(?m)^ManagedPack=true\r?$', 'ManagedPack=false'
    $cfg = $cfg -replace '(?m)^ManagedPackID=.*\r?$', 'ManagedPackID='
    $cfg = $cfg -replace '(?m)^ManagedPackType=.*\r?$', 'ManagedPackType='
    $cfg = $cfg -replace '(?m)^ManagedPackName=.*\r?$', 'ManagedPackName='
    $cfg = $cfg -replace '(?m)^ManagedPackVersionID=.*\r?$', 'ManagedPackVersionID='
    $cfg = $cfg -replace '(?m)^ManagedPackVersionName=.*\r?$', 'ManagedPackVersionName='
    $cfg = $cfg -replace '(?m)^name=.*\r?$', "name=$Name"
    Write-Utf8NoBom -Path $instanceCfg -Content $cfg
    return $target
}

function Disable-Mod {
    param([string]$MinecraftPath, [string]$Pattern)
    Get-ChildItem -LiteralPath (Join-Path $MinecraftPath 'mods') -Filter $Pattern -File -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -notlike '*.disabled' } |
        ForEach-Object { Rename-Item -LiteralPath $_.FullName -NewName ($_.Name + '.disabled') }
}

function Set-Option {
    param([string]$OptionsPath, [string]$Key, [string]$Value)
    $content = Get-Content -LiteralPath $OptionsPath
    $replacement = "${Key}:${Value}"
    if ($content -match ('^' + [regex]::Escape($Key) + ':')) {
        $content = $content | ForEach-Object {
            if ($_ -match ('^' + [regex]::Escape($Key) + ':')) { $replacement } else { $_ }
        }
    } else {
        $content += $replacement
    }
    Write-Utf8NoBom -Path $OptionsPath -Content $content
}

function Set-PerformanceJavaAlias {
    param([string]$InstancePath)
    $instanceCfg = Join-Path $InstancePath 'instance.cfg'
    $cfg = Get-Content -LiteralPath $instanceCfg -Raw
    $match = [regex]::Match($cfg, '(?m)^JavaPath=(.+?)\r?$')
    if (-not $match.Success) {
        throw "JavaPath is missing from $instanceCfg"
    }
    $javaPath = $match.Groups[1].Value.Trim()
    if (-not (Test-Path -LiteralPath $javaPath -PathType Leaf)) {
        throw "Configured Java executable not found: $javaPath"
    }
    $alias = Join-Path (Split-Path -Parent $javaPath) 'javaw-dlss-performance.exe'
    if (-not (Test-Path -LiteralPath $alias)) {
        Copy-Item -LiteralPath $javaPath -Destination $alias
    }
    $cfg = $cfg -replace '(?m)^JavaPath=.+?\r?$', ('JavaPath=' + ($alias -replace '\\', '/'))
    Write-Utf8NoBom -Path $instanceCfg -Content $cfg
}

$beautiful = Copy-ProfileBase -Name $BeautifulName
if ($beautiful) {
    $mc = Join-Path $beautiful 'minecraft'
    Disable-Mod -MinecraftPath $mc -Pattern 'iris-*.jar'
    Copy-Item -LiteralPath $beautifulConfig -Destination (Join-Path $mc 'config\caustica.toml') -Force
    Set-Option -OptionsPath (Join-Path $mc 'options.txt') -Key 'preferredGraphicsBackend' -Value '"vulkan"'
    Set-Option -OptionsPath (Join-Path $mc 'options.txt') -Key 'enableVsync' -Value 'true'
}

$performance = Copy-ProfileBase -Name $PerformanceName
if ($performance) {
    $mc = Join-Path $performance 'minecraft'
    Disable-Mod -MinecraftPath $mc -Pattern 'caustica-*.jar'
    Disable-Mod -MinecraftPath $mc -Pattern 'iris-*.jar'
    Set-Option -OptionsPath (Join-Path $mc 'options.txt') -Key 'preferredGraphicsBackend' -Value '"vulkan"'
    Set-Option -OptionsPath (Join-Path $mc 'options.txt') -Key 'graphicsPreset' -Value '"custom"'
    Set-Option -OptionsPath (Join-Path $mc 'options.txt') -Key 'enableVsync' -Value 'false'
    Set-Option -OptionsPath (Join-Path $mc 'options.txt') -Key 'maxFps' -Value '260'
    Set-PerformanceJavaAlias -InstancePath $performance
}

if (-not $WhatIfPreference) {
    Write-Host "Created Prism profiles:"
    Write-Host "  $BeautifulName"
    Write-Host "  $PerformanceName"
    Write-Host 'Saved worlds were not copied.'
}
