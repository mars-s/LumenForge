# Feasibility and architecture notes

## Verdict

The overall experience is feasible on Minecraft Java 26.2 and the installed RTX
4060 Laptop GPU, but not by directly composing every named component in one
renderer:

- Rethinking Voxels is an Iris/OptiFine-format shaderpack. Vitrail translates
  that format to Minecraft 26.2's Vulkan backend, but the tested beta9 pack
  currently produces broken output under Vitrail.
- Caustica replaces the world renderer and integrates NGX against Minecraft
  26.2's Vulkan backend.
- Iris itself does not support the Vulkan backend; Vitrail is a separate
  implementation of the shaderpack runtime on Vulkan.
- Vulkan alone does not imply hardware ray tracing. Rethinking Voxels' voxel
  lighting stays shader-based until its tracing is explicitly ported to Vulkan
  ray queries or ray-tracing pipelines and acceleration structures.
- DLSS Ray Reconstruction is not a generic screen filter. It consumes the
  renderer's ray-traced color, depth, motion, exposure, and guide buffers.
- Native DLSS Frame Generation likewise needs depth, motion vectors, HUD-less
  color, UI color/alpha, camera constants, and correct presentation pacing.

Consequently, a literal “Rethinking Voxels + Caustica RR” pipeline requires a
substantial renderer integration or a port of the Rethinking Voxels art
direction and effects into Caustica's Slang/Vulkan path tracer. Loading the
shaderpack alongside Caustica does not merge the renderers. Vitrail makes
Vulkan shaderpack rendering possible, but neither hardware RT nor NGX follows
from that alone.

## Implemented split

### Beautiful

Caustica owns the Vulkan world render. DLSS RR reconstructs its path-traced
buffers and native DLSS FG interpolates one frame. Reflex is enabled. SPBR gives
Caustica LabPBR material data. The tunable visual surface is Caustica's own Video
Settings panel: DLSS quality, samples, bounces, exposure, entities, particles,
water, HDR, and debug buffers.

The next true Rethinking-Voxels step is an art-direction port, not shaderpack
loading: colored emissive mappings, atmospheric sky/fog/cloud work, water
shading, bloom, and dimension-specific grading in Caustica.

### Performance

Minecraft's Vulkan backend renders the default look with Sodium. Caustica and
Iris are disabled. NVIDIA Smooth Motion supplies driver-level interpolation; it
supports Vulkan on RTX 40-series GPUs and does not require Caustica's internal
render buffers.

The Performance profile uses a uniquely named Java executable so its NVIDIA App
profile cannot accidentally enable Smooth Motion in the Beautiful profile.

## Evidence from this machine

The existing 4K Caustica run reported:

- NVIDIA GeForce RTX 4060 Laptop GPU, driver 616.92
- Vulkan 1.4.351
- NGX initialized successfully
- DLSS Ray Reconstruction available
- RR feature created at 1920×1080 input to 3840×2160 output

That establishes hardware, driver, Vulkan, NGX, and RR viability. Native FG was
disabled in that run and remains experimental.

## Known risk: presentation mode

Caustica's FG implementation warns that strict FIFO is required to guarantee
every generated present reaches a vblank. The observed fullscreen configuration
was FIFO_RELAXED. Caustica can evaluate and queue generated images, but the
display may replace/drop them unless the swapchain is forced to FIFO. A source
patch or upstream fix should force FIFO whenever native FG is enabled, followed
by frame-time and capture validation.

## Retired Vitrail experiment

The isolated `Rethinking Voxels Vulkan 26.2` Prism profile was removed from
the active instances at the user's request. The original Vitrail 0.11.0-beta
release rejects the pack's `world0/prepare` vertex stage. A local header fix
allows that stage to compile, but `world0/shadow` still fails and the picture
contains large black regions. This is not an active Beautiful mode. See the
[test record](vitrail-test.md).

## Primary references

- [Caustica source and requirements](https://github.com/ComfyFluffy/Caustica)
- [Rethinking Voxels source and Iris requirement](https://github.com/gri573/rethinking-voxels)
- [Vitrail source and compatibility notes](https://github.com/avpbynf/Vitrail-Shaders)
- [Iris Vulkan incompatibility report](https://github.com/IrisShaders/Iris/issues/3357)
- [NVIDIA DLSS-G integration checklist](https://github.com/NVIDIA-RTX/Streamline/blob/main/docs/ProgrammingGuideDLSS_G.md)
- [NVIDIA Smooth Motion setup and Vulkan/RTX 40 support](https://nvidia.custhelp.com/app/answers/detail/a_id/5621/)
- [Prism Launcher command-line interface](https://prismlauncher.org/wiki/getting-started/command-line-interface/)

