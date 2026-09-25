# Rethinking Voxels on native Vulkan: test record

Tested 25 September 2026 on Minecraft 26.2/Fabric, Sodium 0.9.2, Vitrail
0.11.0-beta, and `rethinking-voxels_r0.1-beta9.zip`, using the RTX 4060 Laptop
GPU at 3840×2160. The isolated Prism instance is `Rethinking Voxels Vulkan
26.2`; `tools/New-VitrailProfile.ps1` recreates it without copying saves.

The official Vitrail release loads the pack but fails to compile
`vitrail:pipeline/pack/2/world0/prepare`:

```text
error: 'image variables not declared writeonly and without a format layout qualifier'
world0/prepare did not compile, nothing of this pack will be drawn
```

The source is a cross-stage resource declaration: `colorimg9` is declared
`layout(r32ui) writeonly` in the pack's fragment stage but emitted unqualified
into the vertex stage, where it is unused. The local patch in
`upstream/Vitrail-Shaders/common/src/main/java/dev/vitrail/glsl/Emitter.java`
adds `writeonly` to an otherwise unqualified storage-image declaration. A
locally built jar in this test profile gets past that first failure. This is
only a diagnostic build, not an upstream release or a complete fix.

The patched build then reports repeated `world0/shadow` geometry-shader
errors:

```text
error: 'in' : type must be an array: entityColor
error: 'in' : type must be an array: entityId
error: 'in' : type must be an array: blockEntityId
error: 'in' : type must be an array: currentRenderedItemId
world0/shadow did not compile, so the shadow pass keeps the game's own shader
```

The pack's `shaders/program/shadow.glsl` geometry stage writes
`occupancyVolume` and `voxelCols`; losing that stage plausibly deprives the
voxel lighting pipeline of its scene data. The user's screenshot shows large
black, noisy holes across the sky and world. That is consistent with the
missing stage, but causation still needs a controlled rerun after fixing the
geometry inputs. The log also warns that a 48 MiB `distanceFieldI` volume may
remain uninitialised and that the pack allocates about 1.1 GiB of colour
targets at 4K. Those are additional suspects, not proven causes.

Next validation gate: make `world0/shadow` compile with correctly arrayed
geometry-stage inputs and verify it really populates the voxel volumes. Then
compare the same view against Iris's output for correctness, at a lower test
resolution before returning to 4K. Only after the pack renders correctly
should DLSS buffer extraction/integration be investigated. Vitrail currently
has camera-only motion vectors, not the object motion and renderer guide data
needed for reliable Ray Reconstruction and native Frame Generation.
