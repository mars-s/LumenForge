# Caustica torch-light experiment

The installed Beautiful profile uses Caustica 0.1.1. Inspection of that jar
shows material-override format 1 (`emission.strength` from 0 to 4), but no
`RtLightGrid`/`RtLightCollector` classes or `[lights]` config section. The
current upstream source has explicit RIS sampling of glowing blocks and a
stronger default torch material, but it is not the installed release. The
upstream source also requires Slang/Vulkan build tools and NVIDIA NGX SDK
inputs; its newer renderer has not been tested on this machine.

The checked-in [light boost pack](../packs/caustica-torch-boost/pack.mcmeta)
uses format-1 overrides at strength 4.0 for regular, soul, and copper torch
sprites, lit candles, lanterns, and end rods. It is installed as the highest-priority resource pack in the isolated
`DLSS Beautiful 26.2` Prism instance. The original Caustica jar, DLSS
settings, and all worlds are unchanged. To undo it, disable `LumenForge Torch
Boost` in Resource Packs; no mod reinstall is needed.

`SPBR-22.zip` is enabled directly below Torch Boost. The observed resource
reload listed both packs, and SPBR contains matching textures for every
overridden sprite. This retains the LabPBR material data while the
Caustica-specific override sets emission strength.

This pack raises the torch emitter intensity within what Caustica 0.1.1
supports. It does not increase Minecraft's block-light radius or add the
newer direct-light sampling algorithm. At a fixed brightness threshold, more
emitter intensity may make walls farther from a torch visible, but the effect
needs an A/B comparison in the museum map. Global exposure brightens dark
surfaces without improving actual torch contribution; path-bounce count alone
does not solve missing direct-light sampling.

The initial museum test succeeded technically: Caustica logged `RT material
overrides: format=1, rules=3` and `matchedOverrides=3`, with SPBR loaded. The
provided museum screenshot still shows luminous fixtures surrounded by dark
walls and floor. The original three-rule pack therefore does not solve the
room-lighting problem. The expanded 27-rule pack targets visible candle and
lantern fixtures as well; all 27 overrides matched after reloading, yet museum
screenshots still show dark interiors. The installed 0.1.1 override caps
emission strength at 4.0. Truly extending fixture illumination requires a
newer Caustica build with explicit emissive-light sampling.

For visibility in the installed release, Manual +6 EV made the museum interior
usable but would overexpose outdoors. Caustica 0.1.1 computes an Auto target
from measured luminance, then clamps it at +3 EV by default. The Beautiful
preset now raises only Auto's upper ceiling to +6 EV; the Sodium Visuals page
exposes it as **Auto Interior Ceiling**. Outdoor scenes below the ceiling keep
their original auto exposure. This cannot create missing indirect light, and
it may not fully brighten a room whose Auto target never reaches the cap.

The first outdoor test was washed out because Caustica 0.1.1 also treats its
`manual-ev` setting as Auto's global `evBias`. After testing Manual exposure,
the shared value had reached +10.8 EV while the mode was Auto. Returning that
bias to 0.0 EV restored the intended baseline. LumenForge now persists separate
Auto Bias and Manual EV values and applies only the active one when changing
modes, preventing this cross-mode carryover through its Visuals page.

