package dev.lumenforge.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.comfyfluffy.caustica.CausticaConfig;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Keeps Auto bias independent from the manual EV that Caustica stores in the same setting. */
final class ExposureProfiles {
    private static final Logger LOGGER = LoggerFactory.getLogger("LumenForge");
    private static final Path FILE = FabricLoader.getInstance().getConfigDir()
            .resolve("lumenforge-exposure.json");

    private static boolean loaded;
    private static int autoBiasTenths;
    private static int manualEvTenths = 60;

    private ExposureProfiles() {
    }

    static synchronized int getAutoBiasTenths() {
        load();
        return autoBiasTenths;
    }

    static synchronized void setAutoBiasTenths(int value) {
        load();
        autoBiasTenths = Math.clamp(value, -30, 30);
    }

    static synchronized int getManualEvTenths() {
        load();
        return manualEvTenths;
    }

    static synchronized void setManualEvTenths(int value) {
        load();
        manualEvTenths = Math.clamp(value, -150, 150);
    }

    static synchronized void restoreRecommended() {
        load();
        autoBiasTenths = 0;
        manualEvTenths = 60;
        CausticaConfig.Rt.Exposure.MODE.set("auto");
        applyAndSave();
    }

    static synchronized void applyAndSave() {
        load();
        boolean manual = "manual".equalsIgnoreCase(CausticaConfig.Rt.Exposure.MODE.get());
        CausticaConfig.Rt.Exposure.MANUAL_EV.set((manual ? manualEvTenths : autoBiasTenths) / 10.0f);
        CausticaConfig.save();

        JsonObject root = new JsonObject();
        root.addProperty("autoBiasEv", autoBiasTenths / 10.0f);
        root.addProperty("manualEv", manualEvTenths / 10.0f);
        try {
            Files.createDirectories(FILE.getParent());
            Path temporary = Files.createTempFile(FILE.getParent(), ".lumenforge-exposure-", ".json");
            try {
                Files.writeString(temporary, root.toString() + System.lineSeparator());
                Files.move(temporary, FILE, StandardCopyOption.REPLACE_EXISTING);
            } finally {
                Files.deleteIfExists(temporary);
            }
        } catch (IOException e) {
            LOGGER.error("Could not save LumenForge exposure profiles to {}", FILE, e);
        }
    }

    private static void load() {
        if (loaded) {
            return;
        }
        loaded = true;
        float activeEv = CausticaConfig.Rt.Exposure.MANUAL_EV.value();
        if (Float.isFinite(activeEv)) {
            int tenths = Math.round(activeEv * 10.0f);
            if ("manual".equalsIgnoreCase(CausticaConfig.Rt.Exposure.MODE.get())) {
                manualEvTenths = Math.clamp(tenths, -150, 150);
            } else if (tenths >= -30 && tenths <= 30) {
                // Preserve an existing safe Auto bias, but never import a large Manual EV into Auto.
                autoBiasTenths = tenths;
            }
        }
        if (!Files.isRegularFile(FILE)) {
            return;
        }
        try {
            JsonObject root = JsonParser.parseString(Files.readString(FILE)).getAsJsonObject();
            if (root.has("autoBiasEv")) {
                autoBiasTenths = Math.clamp(Math.round(root.get("autoBiasEv").getAsFloat() * 10.0f), -30, 30);
            }
            if (root.has("manualEv")) {
                manualEvTenths = Math.clamp(Math.round(root.get("manualEv").getAsFloat() * 10.0f), -150, 150);
            }
        } catch (RuntimeException | IOException e) {
            LOGGER.warn("Could not read LumenForge exposure profiles {}; using defaults", FILE, e);
        }
    }
}

