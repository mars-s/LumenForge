package dev.lumenforge.client;

import dev.comfyfluffy.caustica.CausticaConfig;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.StorageEventHandler;
import net.caffeinemc.mods.sodium.api.config.option.OptionFlag;
import net.caffeinemc.mods.sodium.api.config.option.OptionImpact;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.EnumOptionBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.ModOptionsBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Locale;

public final class LumenForgeConfigEntryPoint implements ConfigEntryPoint {
    private static final String MOD_ID = "lumenforge";
    private static final StorageEventHandler SAVE = CausticaConfig::save;
    private static final StorageEventHandler SAVE_EXPOSURE = ExposureProfiles::applyAndSave;

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    private static Component text(String key) {
        return Component.translatable("lumenforge." + key);
    }

    @Override
    public void registerConfigLate(ConfigBuilder builder) {
        CausticaConfig.ensureRegistered();

        ModOptionsBuilder lumenForge = builder.registerOwnModOptions()
                .setName("LumenForge")
                .setColorTheme(builder.createColorTheme().setFullThemeRGB(0xD99022, 0xFFC857, 0x765A30));

        lumenForge.addPage(builder.createOptionPage()
                .setName(text("page.renderer"))
                .addOptionGroup(builder.createOptionGroup()
                        .setName(text("group.caustica_renderer"))
                        .addOption(builder.createBooleanOption(id("caustica_enabled"))
                                .setName(text("option.caustica_enabled"))
                                .setTooltip(text("option.caustica_enabled.tooltip"))
                                .setImpact(OptionImpact.HIGH)
                                .setFlags(OptionFlag.REQUIRES_GAME_RESTART)
                                .setStorageHandler(SAVE)
                                .setBinding(CausticaConfig.Rt.ENABLED::set,
                                        CausticaConfig.Rt.ENABLED::get)
                                .setDefaultValue(true))));

        lumenForge.addPage(builder.createOptionPage()
                .setName(text("page.quality"))
                .addOptionGroup(builder.createOptionGroup()
                        .setName(text("group.reconstruction"))
                        .addOption(builder.createBooleanOption(id("ray_reconstruction"))
                                .setName(text("option.rr"))
                                .setTooltip(text("option.rr.tooltip"))
                                .setStorageHandler(SAVE)
                                .setBinding(CausticaConfig.Rt.DlssRr.ENABLED::set,
                                        CausticaConfig.Rt.DlssRr.ENABLED::get)
                                .setDefaultValue(true))
                        .addOption(builder.createEnumOption(id("reconstruction_quality"), QualityMode.class)
                                .setName(text("option.quality"))
                                .setTooltip(text("option.quality.tooltip"))
                                .setImpact(OptionImpact.HIGH)
                                .setStorageHandler(SAVE)
                                .setBinding(mode -> CausticaConfig.Rt.DlssRr.QUALITY.set(mode.causticaValue),
                                        () -> QualityMode.fromCaustica(CausticaConfig.Rt.DlssRr.QUALITY.value()))
                                .setDefaultValue(QualityMode.PERFORMANCE)
                                .setElementNameProvider(mode -> text("quality." + mode.translation))))
                .addOptionGroup(builder.createOptionGroup()
                        .setName(text("group.path_tracing"))
                        .addOption(builder.createIntegerOption(id("samples_per_pixel"))
                                .setName(text("option.spp"))
                                .setTooltip(text("option.spp.tooltip"))
                                .setImpact(OptionImpact.HIGH)
                                .setStorageHandler(SAVE)
                                .setBinding(CausticaConfig.Rt.Composite.SPP::set,
                                        CausticaConfig.Rt.Composite.SPP::get)
                                .setDefaultValue(1)
                                .setRange(1, 8, 1)
                                .setValueFormatter(value -> Component.literal(Integer.toString(value))))
                        .addOption(builder.createIntegerOption(id("path_bounces"))
                                .setName(text("option.bounces"))
                                .setTooltip(text("option.bounces.tooltip"))
                                .setImpact(OptionImpact.HIGH)
                                .setStorageHandler(SAVE)
                                .setBinding(CausticaConfig.Rt.Composite.MAX_BOUNCES::set,
                                        CausticaConfig.Rt.Composite.MAX_BOUNCES::get)
                                .setDefaultValue(4)
                                .setRange(2, 8, 1)
                                .setValueFormatter(value -> Component.literal(Integer.toString(value))))
                        .addOption(booleanOption(builder, "entities", "option.entities",
                                CausticaConfig.Rt.Entities.ENABLED, true, OptionImpact.HIGH))
                        .addOption(booleanOption(builder, "particles", "option.particles",
                                CausticaConfig.Rt.Entities.PARTICLES_ENABLED, true, OptionImpact.MEDIUM))
                        .addOption(booleanOption(builder, "animated_water", "option.water",
                                CausticaConfig.Rt.Composite.WATER_WAVES, true, OptionImpact.LOW)))
                .addOptionGroup(builder.createOptionGroup()
                        .setName(text("group.materials"))
                        .addOption(booleanOption(builder, "entity_pbr", "option.entity_pbr",
                                CausticaConfig.Rt.EntityTextures.PBR, true, OptionImpact.MEDIUM))
                        .addOption(booleanOption(builder, "opacity_micromaps", "option.opacity_micromaps",
                                CausticaConfig.Rt.Omm.ENABLED, true, OptionImpact.MEDIUM))
                        .addOption(builder.createIntegerOption(id("opacity_detail"))
                                .setName(text("option.opacity_detail"))
                                .setTooltip(text("option.opacity_detail.tooltip"))
                                .setImpact(OptionImpact.MEDIUM)
                                .setStorageHandler(SAVE)
                                .setBinding(CausticaConfig.Rt.Omm.SUBDIVISION::set,
                                        CausticaConfig.Rt.Omm.SUBDIVISION::get)
                                .setDefaultValue(4)
                                .setRange(0, 6, 1)
                                .setValueFormatter(value -> Component.literal(Integer.toString(value))))));

        lumenForge.addPage(builder.createOptionPage()
                .setName(text("page.visuals"))
                .addOptionGroup(builder.createOptionGroup()
                        .setName(text("group.torch_lighting"))
                        .addOption(builder.createIntegerOption(id("torch_emission"))
                                .setName(text("option.torch_emission"))
                                .setTooltip(text("option.torch_emission.tooltip"))
                                .setImpact(OptionImpact.MEDIUM)
                                .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                                .setStorageHandler(() -> {})
                                .setBinding(TorchlightSettings::setStrengthTenths,
                                        TorchlightSettings::getStrengthTenths)
                                .setDefaultValue(40)
                                .setRange(0, 40, 1)
                                .setValueFormatter(value -> Component.literal(
                                        String.format(Locale.ROOT, "%.1fx", value / 10.0f)))))
                .addOptionGroup(builder.createOptionGroup()
                        .setName(text("group.look"))
                        .addOption(builder.createEnumOption(id("exposure_mode"), ExposureMode.class)
                                .setName(text("option.exposure"))
                                .setTooltip(text("option.exposure.tooltip"))
                                .setStorageHandler(SAVE_EXPOSURE)
                                .setBinding(mode -> CausticaConfig.Rt.Exposure.MODE.set(mode.configValue),
                                        () -> ExposureMode.fromConfig(CausticaConfig.Rt.Exposure.MODE.get()))
                                .setDefaultValue(ExposureMode.AUTO)
                                .setElementNameProvider(mode -> text("exposure." + mode.configValue)))
                        .addOption(builder.createIntegerOption(id("auto_exposure_bias"))
                                .setName(text("option.auto_exposure_bias"))
                                .setTooltip(text("option.auto_exposure_bias.tooltip"))
                                .setStorageHandler(SAVE_EXPOSURE)
                                .setBinding(ExposureProfiles::setAutoBiasTenths,
                                        ExposureProfiles::getAutoBiasTenths)
                                .setDefaultValue(0)
                                .setRange(-30, 30, 1)
                                .setValueFormatter(value -> Component.literal(
                                        String.format(Locale.ROOT, "%+.1f EV", value / 10.0f))))
                        .addOption(builder.createIntegerOption(id("exposure_ev"))
                                .setName(text("option.exposure_ev"))
                                .setTooltip(text("option.exposure_ev.tooltip"))
                                .setStorageHandler(SAVE_EXPOSURE)
                                .setBinding(ExposureProfiles::setManualEvTenths,
                                        ExposureProfiles::getManualEvTenths)
                                .setDefaultValue(60)
                                .setRange(-150, 150, 1)
                                .setValueFormatter(value -> Component.literal(
                                        String.format(Locale.ROOT, "%+.1f EV", value / 10.0f))))
                        .addOption(builder.createIntegerOption(id("auto_interior_ceiling"))
                                .setName(text("option.auto_interior_ceiling"))
                                .setTooltip(text("option.auto_interior_ceiling.tooltip"))
                                .setStorageHandler(SAVE)
                                .setBinding(value -> CausticaConfig.Rt.Exposure.MAX_EV.set(value / 10.0f),
                                        () -> Math.clamp(Math.round(CausticaConfig.Rt.Exposure.MAX_EV.value() * 10.0f), 0, 80))
                                .setDefaultValue(60)
                                .setRange(0, 80, 1)
                                .setValueFormatter(value -> Component.literal(
                                        String.format(Locale.ROOT, "+%.1f EV ceiling", value / 10.0f)))))
                .addOptionGroup(builder.createOptionGroup()
                        .setName(text("group.auto_metering"))
                        .addOption(builder.createIntegerOption(id("auto_target_gray"))
                                .setName(text("option.auto_target_gray"))
                                .setTooltip(text("option.auto_target_gray.tooltip"))
                                .setStorageHandler(SAVE)
                                .setBinding(value -> CausticaConfig.Rt.Exposure.KEY.set(value / 100.0f),
                                        () -> Math.clamp(Math.round(CausticaConfig.Rt.Exposure.KEY.value() * 100.0f), 5, 40))
                                .setDefaultValue(18)
                                .setRange(5, 40, 1)
                                .setValueFormatter(value -> Component.literal(value + "%")))
                        .addOption(builder.createIntegerOption(id("auto_exterior_floor"))
                                .setName(text("option.auto_exterior_floor"))
                                .setTooltip(text("option.auto_exterior_floor.tooltip"))
                                .setStorageHandler(SAVE)
                                .setBinding(value -> CausticaConfig.Rt.Exposure.MIN_EV.set(value / 10.0f),
                                        () -> Math.clamp(Math.round(CausticaConfig.Rt.Exposure.MIN_EV.value() * 10.0f), -60, 0))
                                .setDefaultValue(-15)
                                .setRange(-60, 0, 1)
                                .setValueFormatter(value -> Component.literal(
                                        String.format(Locale.ROOT, "%+.1f EV floor", value / 10.0f))))
                        .addOption(builder.createIntegerOption(id("adapt_dark"))
                                .setName(text("option.adapt_dark"))
                                .setTooltip(text("option.adapt_dark.tooltip"))
                                .setStorageHandler(SAVE)
                                .setBinding(value -> CausticaConfig.Rt.Exposure.ADAPT_UP.set(value / 100.0f),
                                        () -> Math.clamp(Math.round(CausticaConfig.Rt.Exposure.ADAPT_UP.value() * 100.0f), 1, 200))
                                .setDefaultValue(12)
                                .setRange(1, 200, 1)
                                .setValueFormatter(value -> Component.literal(
                                        String.format(Locale.ROOT, "%.2f s", value / 100.0f))))
                        .addOption(builder.createIntegerOption(id("adapt_bright"))
                                .setName(text("option.adapt_bright"))
                                .setTooltip(text("option.adapt_bright.tooltip"))
                                .setStorageHandler(SAVE)
                                .setBinding(value -> CausticaConfig.Rt.Exposure.ADAPT_DOWN.set(value / 100.0f),
                                        () -> Math.clamp(Math.round(CausticaConfig.Rt.Exposure.ADAPT_DOWN.value() * 100.0f), 1, 200))
                                .setDefaultValue(35)
                                .setRange(1, 200, 1)
                                .setValueFormatter(value -> Component.literal(
                                        String.format(Locale.ROOT, "%.2f s", value / 100.0f))))));

        lumenForge.addPage(builder.createOptionPage()
                .setName(text("page.hdr"))
                .addOptionGroup(builder.createOptionGroup()
                        .setName(text("group.hdr_output"))
                        .addOption(builder.createBooleanOption(id("hdr_enabled"))
                                .setName(text("option.hdr_enabled"))
                                .setTooltip(text("option.hdr_enabled.tooltip"))
                                .setFlags(OptionFlag.REQUIRES_GAME_RESTART)
                                .setStorageHandler(SAVE)
                                .setBinding(CausticaConfig.Rt.Hdr.ENABLED::set,
                                        CausticaConfig.Rt.Hdr.ENABLED::get)
                                .setDefaultValue(false))
                        .addOption(builder.createIntegerOption(id("paper_white"))
                                .setName(text("option.paper_white"))
                                .setTooltip(text("option.paper_white.tooltip"))
                                .setStorageHandler(SAVE)
                                .setBinding(value -> CausticaConfig.Rt.Hdr.PAPER_WHITE_NITS.set((float) value),
                                        () -> Math.round(CausticaConfig.Rt.Hdr.PAPER_WHITE_NITS.value()))
                                .setDefaultValue(200)
                                .setRange(80, 500, 10)
                                .setValueFormatter(value -> Component.literal(value + " nits")))
                        .addOption(builder.createIntegerOption(id("peak_nits"))
                                .setName(text("option.peak_nits"))
                                .setTooltip(text("option.peak_nits.tooltip"))
                                .setStorageHandler(SAVE)
                                .setBinding(value -> CausticaConfig.Rt.Hdr.PEAK_NITS.set((float) value),
                                        () -> Math.round(CausticaConfig.Rt.Hdr.PEAK_NITS.value()))
                                .setDefaultValue(1000)
                                .setRange(80, 5000, 20)
                                .setValueFormatter(value -> Component.literal(value + " nits")))));

        lumenForge.addPage(builder.createOptionPage()
                .setName(text("page.frame_generation"))
                .addOptionGroup(builder.createOptionGroup()
                        .setName(text("group.generation"))
                        .addOption(booleanOption(builder, "frame_generation", "option.frame_generation",
                                CausticaConfig.Rt.Fg.ENABLED, true, OptionImpact.LOW))
                        .addOption(builder.createIntegerOption(id("generated_frames"))
                                .setName(text("option.generated_frames"))
                                .setTooltip(text("option.generated_frames.tooltip"))
                                .setStorageHandler(SAVE)
                                .setBinding(CausticaConfig.Rt.Fg.MULTI_FRAME_COUNT::set,
                                        CausticaConfig.Rt.Fg.MULTI_FRAME_COUNT::get)
                                .setDefaultValue(1)
                                .setRange(1, 3, 1)
                                .setValueFormatter(value -> Component.literal(value + " (" + (value + 1) + "x)"))))
                .addOptionGroup(builder.createOptionGroup()
                        .setName(text("group.latency"))
                        .addOption(booleanOption(builder, "reflex", "option.reflex",
                                CausticaConfig.Rt.Reflex.ENABLED, true, OptionImpact.LOW))
                        .addOption(booleanOption(builder, "reflex_boost", "option.reflex_boost",
                                CausticaConfig.Rt.Reflex.LOW_LATENCY_BOOST, false, OptionImpact.LOW))
                        .addOption(builder.createIntegerOption(id("rendered_fps_target"))
                                .setName(text("option.fps_target"))
                                .setTooltip(text("option.fps_target.tooltip"))
                                .setStorageHandler(SAVE)
                                .setBinding(LumenForgeConfigEntryPoint::setFpsTarget,
                                        LumenForgeConfigEntryPoint::getFpsTarget)
                                .setDefaultValue(0)
                                .setRange(0, 240, 5)
                                .setValueFormatter(value -> value == 0
                                        ? Component.literal("Unlimited")
                                        : Component.literal(value + " FPS")))));

        lumenForge.addPage(builder.createOptionPage()
                .setName(text("page.diagnostics"))
                .addOptionGroup(builder.createOptionGroup()
                        .setName(text("group.views"))
                        .addOption(builder.createEnumOption(id("debug_view"), DebugView.class)
                                .setName(text("option.debug_view"))
                                .setTooltip(text("option.debug_view.tooltip"))
                                .setStorageHandler(SAVE)
                                .setBinding(view -> CausticaConfig.Rt.Composite.DEBUG_VIEW.set(view.causticaValue),
                                        () -> DebugView.fromCaustica(CausticaConfig.Rt.Composite.DEBUG_VIEW.value()))
                                .setDefaultValue(DebugView.OFF)
                                .setElementNameProvider(view -> text("debug." + view.translation))))
                .addOptionGroup(builder.createOptionGroup()
                        .setName(text("group.telemetry"))
                        .addOption(booleanOption(builder, "frame_stats", "option.frame_stats",
                                CausticaConfig.Rt.FrameStats.ENABLED, false, OptionImpact.LOW))
                        .addOption(booleanOption(builder, "omm_stats", "option.omm_stats",
                                CausticaConfig.Rt.Omm.STATS, false, OptionImpact.LOW))
                        .addOption(builder.createBooleanOption(id("heavy_crash_diagnostics"))
                                .setName(text("option.crash_diagnostics"))
                                .setTooltip(text("option.crash_diagnostics.tooltip"))
                                .setImpact(OptionImpact.HIGH)
                                .setFlags(OptionFlag.REQUIRES_GAME_RESTART)
                                .setStorageHandler(SAVE)
                                .setBinding(CausticaConfig.Rt.Diagnostics.HEAVY_CRASH_DIAGNOSTICS::set,
                                        CausticaConfig.Rt.Diagnostics.HEAVY_CRASH_DIAGNOSTICS::get)
                                .setDefaultValue(false))));

        lumenForge.addPage(builder.createOptionPage()
                .setName(text("page.presets"))
                .addOptionGroup(builder.createOptionGroup()
                        .setName(text("group.recommended"))
                        .addOption(builder.createExternalButtonOption(id("restore_recommended"))
                                .setName(text("option.restore_recommended"))
                                .setTooltip(text("option.restore_recommended.tooltip"))
                                .setScreenConsumer(RecommendedPreset::confirmFrom))));
    }

    private static net.caffeinemc.mods.sodium.api.config.structure.BooleanOptionBuilder booleanOption(
            ConfigBuilder builder,
            String id,
            String translation,
            CausticaConfig.BooleanSetting setting,
            boolean defaultValue,
            OptionImpact impact) {
        return builder.createBooleanOption(LumenForgeConfigEntryPoint.id(id))
                .setName(text(translation))
                .setTooltip(text(translation + ".tooltip"))
                .setImpact(impact)
                .setStorageHandler(SAVE)
                .setBinding(setting::set, setting::get)
                .setDefaultValue(defaultValue);
    }

    private static void setFpsTarget(int fps) {
        CausticaConfig.Rt.Reflex.MINIMUM_INTERVAL_US.set(fps <= 0 ? 0 : Math.round(1_000_000.0f / fps));
    }

    private static int getFpsTarget() {
        int interval = CausticaConfig.Rt.Reflex.MINIMUM_INTERVAL_US.value();
        if (interval <= 0) {
            return 0;
        }
        return Math.clamp(5 * Math.round((1_000_000.0f / interval) / 5.0f), 5, 240);
    }

    private enum QualityMode {
        ULTRA_PERFORMANCE(3, "ultra_performance"),
        PERFORMANCE(0, "performance"),
        BALANCED(1, "balanced"),
        QUALITY(2, "quality"),
        DLAA(5, "dlaa");

        private final int causticaValue;
        private final String translation;

        QualityMode(int causticaValue, String translation) {
            this.causticaValue = causticaValue;
            this.translation = translation;
        }

        private static QualityMode fromCaustica(int value) {
            for (QualityMode mode : values()) {
                if (mode.causticaValue == value) {
                    return mode;
                }
            }
            return PERFORMANCE;
        }
    }

    private enum ExposureMode {
        AUTO("auto"),
        MANUAL("manual");

        private final String configValue;

        ExposureMode(String configValue) {
            this.configValue = configValue;
        }

        private static ExposureMode fromConfig(String value) {
            return "manual".equalsIgnoreCase(value) ? MANUAL : AUTO;
        }
    }

    private enum DebugView {
        OFF(0, "off"),
        NORMALS(1, "normals"),
        ALBEDO(2, "albedo"),
        DEPTH(3, "depth"),
        ROUGHNESS(4, "roughness"),
        MOTION(5, "motion"),
        SPECULAR(6, "specular"),
        SPECULAR_MOTION(7, "specular_motion"),
        EXPOSURE(8, "exposure"),
        METERING(9, "metering");

        private final int causticaValue;
        private final String translation;

        DebugView(int causticaValue, String translation) {
            this.causticaValue = causticaValue;
            this.translation = translation;
        }

        private static DebugView fromCaustica(int value) {
            for (DebugView view : values()) {
                if (view.causticaValue == value) {
                    return view;
                }
            }
            return OFF;
        }
    }
}
