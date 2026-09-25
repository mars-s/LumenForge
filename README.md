# Minecraft 26.2 Caustica/DLSS profiles

This workspace builds two isolated Prism Launcher profiles from an existing
Minecraft 26.2 Fabric instance. It does not modify or copy saved worlds.

## What is technically possible

The active work is Caustica's Vulkan renderer and its DLSS features, with a
separate Sodium performance mode. Earlier Rethinking Voxels experiments were
retired because its Vitrail output was corrupted; neither experiment is an
active Prism profile. The remaining image-quality problem is indoor torch
illumination in Caustica.

The practical two-mode design is:

| Profile | Renderer | Reconstruction / generation | Look |
| --- | --- | --- | --- |
| Beautiful | Caustica Vulkan path tracer | DLSS Ray Reconstruction + DLSS Frame Generation + Reflex | Path-traced lighting with SPBR LabPBR materials |
| Performance | Sodium on Minecraft Vulkan | NVIDIA Smooth Motion (driver frame generation) | Default Minecraft |

The Beautiful profile exposes Caustica's controls for DLSS quality, samples per
pixel, path bounces, exposure, entities, particles, water, materials, opacity
micromaps, HDR output, and debug buffers in Video Settings. The prominent
**LumenForge → Caustica Renderer → Master Switch** turns Caustica ray tracing
on or off after a full Minecraft restart; Vulkan RT device features cannot be
added or removed while the game is running. Its **Visuals → Fixture Glow** slider edits the
enabled light-boost resource pack and reloads assets when you apply it. The
**Auto Interior Ceiling** slider allows higher exposure in dark scenes without
raising the exposure of brighter outdoor scenes that remain below the ceiling.
Auto Bias and Manual EV are stored separately; switching back to Auto no longer
carries the bright manual setting into daylight.
The **Presets → Restore Recommended Settings** button restores the working
Beautiful setup after confirmation. It resets LumenForge-controlled Caustica
values and Fixture Glow, but leaves Sodium options, SPBR selection, and worlds
alone.

## Credit and relationship to Caustica

Thank you to [ComfyFluffy and the Caustica contributors](https://github.com/ComfyFluffy/Caustica)
for the Vulkan path tracer, DLSS integration, and configuration API that make
LumenForge possible. LumenForge is an independent control-panel companion and
resource-pack experiment, not an official Caustica release or a replacement
for Caustica. Caustica itself is licensed under LGPL-3.0-or-later; its source
is not bundled in this repository. Install the upstream mod separately and
follow its license and setup instructions.

The `SPBR-22.zip` resource pack and Prism worlds are not distributed here.

## Build the profiles

Close Minecraft and Prism Launcher, then run:

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\New-PrismProfiles.ps1
```

The script clones only the pack configuration and client assets. It excludes
worlds, screenshots, logs, crash reports, caches, and downloads. Existing target
profiles are never overwritten.

## Beautiful profile

The checked-in preset is aimed at an RTX 4060 Laptop GPU driving a 4K display:

- DLSS RR Performance mode (1080p internal at 4K output)
- one path-traced sample and four secondary bounces
- one generated frame per rendered frame (2x)
- Reflex enabled with boost off
- automatic exposure with a +6 EV ceiling for very dark interiors

At runtime, use **Options → Video Settings → Ray Tracing** to tune image quality.
Start with DLSS Performance at 4K; Balanced is the next quality step if the base
frame rate remains comfortably above 45–60 FPS.

For dark interiors, `LumenForge Torch Boost` is installed as an optional
resource-pack experiment in the Beautiful profile. It raises emission from
torches, lit candles, lanterns, and end rods within the limits of the installed
Caustica 0.1.1 renderer. The museum test showed that boosting torches alone
does not adequately light the room. See [the test notes](docs/caustica-torch-test.md).
The `SPBR-22.zip` LabPBR resource pack is enabled and loaded ahead of the
torch-boost override. Its torch textures include specular/emission maps.

## Performance profile

Caustica and Iris are disabled, leaving Sodium on Minecraft's Vulkan backend.
Enable **Smooth Motion** for the Java runtime in NVIDIA App under
**Graphics → Program settings → Driver Settings**. NVIDIA Smooth Motion supports
Vulkan and RTX 40-series GPUs and supplies driver-level 2x frame generation
without requiring game motion vectors.

Select this profile's dedicated executable in NVIDIA App:

`%APPDATA%\PrismLauncher\java\java-runtime-epsilon\bin\javaw-dlss-performance.exe`

Do not enable Smooth Motion globally for `javaw.exe`. The dedicated executable
keeps driver FG out of the Beautiful profile, where it would otherwise stack on
top of Caustica's native FG.

Do not enable Caustica's native DLSS FG in this profile: its implementation uses
Caustica's own depth, motion-vector, HUD-less, and UI buffers and is not a
drop-in post-process for Sodium's vanilla renderer.

## Current caveat

Caustica's native FG path logs that strict FIFO presentation is required so each
generated frame reaches the display. Minecraft selected FIFO_RELAXED in the
observed 4K fullscreen run. Treat Caustica FG as experimental until an in-game
test confirms generated presents are not being replaced or dropped; the
Performance profile's driver Smooth Motion path is the reliable fallback.

