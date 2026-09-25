package dev.lumenforge.client;

import dev.comfyfluffy.caustica.CausticaConfig;
import net.caffeinemc.mods.sodium.client.config.ConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class RecommendedPreset {
    private static final Logger LOGGER = LoggerFactory.getLogger("LumenForge");

    private RecommendedPreset() {
    }

    static void confirmFrom(Screen settingsScreen) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.gui.setScreen(new ConfirmScreen(confirmed -> {
            if (confirmed) {
                restore();
                // The open Sodium screen owns cached option values. Refresh them from the
                // bindings after the preset is saved so its controls show the new values.
                ConfigManager.CONFIG.resetAllOptionsFromBindings();
            }
            minecraft.gui.setScreen(settingsScreen);
        }, Component.translatable("lumenforge.reset.title"),
                Component.translatable("lumenforge.reset.message"),
                Component.translatable("lumenforge.reset.confirm"),
                Component.translatable("lumenforge.reset.cancel")));
    }

    private static void restore() {
        CausticaConfig.Rt.ENABLED.set(true);
        CausticaConfig.Rt.DlssRr.ENABLED.set(true);
        CausticaConfig.Rt.DlssRr.QUALITY.set(0);
        CausticaConfig.Rt.Composite.SPP.set(1);
        CausticaConfig.Rt.Composite.MAX_BOUNCES.set(4);
        CausticaConfig.Rt.Composite.WATER_WAVES.set(true);
        CausticaConfig.Rt.Entities.ENABLED.set(true);
        CausticaConfig.Rt.Entities.PARTICLES_ENABLED.set(true);
        CausticaConfig.Rt.EntityTextures.PBR.set(true);
        CausticaConfig.Rt.Omm.ENABLED.set(true);
        CausticaConfig.Rt.Omm.SUBDIVISION.set(4);

        CausticaConfig.Rt.Exposure.KEY.set(0.18f);
        CausticaConfig.Rt.Exposure.MIN_EV.set(-1.5f);
        CausticaConfig.Rt.Exposure.MAX_EV.set(6.0f);
        CausticaConfig.Rt.Exposure.ADAPT_UP.set(0.12f);
        CausticaConfig.Rt.Exposure.ADAPT_DOWN.set(0.35f);
        ExposureProfiles.restoreRecommended();

        CausticaConfig.Rt.Fg.ENABLED.set(true);
        CausticaConfig.Rt.Fg.MULTI_FRAME_COUNT.set(1);
        CausticaConfig.Rt.Reflex.ENABLED.set(true);
        CausticaConfig.Rt.Reflex.LOW_LATENCY_BOOST.set(false);
        CausticaConfig.Rt.Reflex.MINIMUM_INTERVAL_US.set(0);

        CausticaConfig.Rt.Hdr.ENABLED.set(false);
        CausticaConfig.Rt.Hdr.PAPER_WHITE_NITS.set(200.0f);
        CausticaConfig.Rt.Hdr.PEAK_NITS.set(1000.0f);

        CausticaConfig.Rt.Composite.DEBUG_VIEW.set(0);
        CausticaConfig.Rt.FrameStats.ENABLED.set(false);
        CausticaConfig.Rt.Omm.STATS.set(false);
        CausticaConfig.Rt.Diagnostics.HEAVY_CRASH_DIAGNOSTICS.set(false);
        CausticaConfig.save();

        try {
            if (TorchlightSettings.getStrengthTenths() != 40) {
                TorchlightSettings.setStrengthTenths(40);
                Minecraft.getInstance().reloadResourcePacks();
            }
        } catch (RuntimeException e) {
            LOGGER.warn("Could not restore LumenForge fixture glow; other settings were restored", e);
        }
    }
}
