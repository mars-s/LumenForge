# LumenForge

LumenForge is the Sodium-native control panel for the DLSS Beautiful profile. It exposes Caustica's
runtime-safe path-tracing, DLSS Ray Reconstruction, Frame Generation, Reflex, look, and diagnostics
settings as first-class pages in Sodium's Video Settings sidebar.

The Caustica Renderer master switch requires a full game restart because
Caustica selects Vulkan ray-tracing device features at startup. The Visuals
page adds auto-exposure target gray, bright-scene EV floor, and separate
dark/bright adaptation controls. Quality exposes entity PBR and opacity
micromaps; HDR Output exposes the display settings. **Restore Recommended
Settings** resets these controls to the Beautiful-profile values.

Thanks to [Caustica](https://github.com/ComfyFluffy/Caustica) and its
contributors. This is an independent companion mod; Caustica must be installed
separately and its source is not included here.

Build on the machine containing the managed Prism profile:

```powershell
.\gradlew.bat build
```

For another setup, pass explicit dependency jars:

```powershell
.\gradlew.bat build -PsodiumJar=C:\path\to\sodium.jar -PcausticaJar=C:\path\to\caustica.jar
```
