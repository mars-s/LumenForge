package dev.lumenforge.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/** Edits only the installed LumenForge override pack, never SPBR or a saved world. */
final class TorchlightSettings {
    private static final String PACK = "LumenForge Torch Boost";
    private static final String[] MATERIALS = {
            "torch", "soul_torch", "copper_torch",
            "candle_lit", "white_candle_lit", "orange_candle_lit", "magenta_candle_lit",
            "light_blue_candle_lit", "yellow_candle_lit", "lime_candle_lit", "pink_candle_lit",
            "gray_candle_lit", "light_gray_candle_lit", "cyan_candle_lit", "purple_candle_lit",
            "blue_candle_lit", "brown_candle_lit", "green_candle_lit", "red_candle_lit",
            "black_candle_lit", "lantern", "soul_lantern", "copper_lantern",
            "exposed_copper_lantern", "weathered_copper_lantern",
            "oxidized_copper_lantern", "end_rod"
    };

    private TorchlightSettings() {
    }

    static int getStrengthTenths() {
        Path path = materialPath(MATERIALS[0]);
        if (!Files.isRegularFile(path)) {
            return 40;
        }
        try {
            JsonObject root = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
            return Math.clamp(Math.round(root.getAsJsonObject("emission")
                    .get("strength").getAsFloat() * 10.0f), 0, 40);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read LumenForge torch pack: " + path, e);
        }
    }

    static void setStrengthTenths(int tenths) {
        if (tenths < 0 || tenths > 40) {
            throw new IllegalArgumentException("Torch strength must be between 0.0 and 4.0");
        }

        // Read every target before writing any of them. A missing pack should not create
        // an inert slider setting or overwrite an unrelated resource pack.
        List<Path> paths = new ArrayList<>();
        List<JsonObject> roots = new ArrayList<>();
        try {
            for (String material : MATERIALS) {
                Path path = materialPath(material);
                JsonObject root = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
                root.getAsJsonObject("emission").addProperty("strength", tenths / 10.0f);
                paths.add(path);
                roots.add(root);
            }
            for (int i = 0; i < paths.size(); i++) {
                Path path = paths.get(i);
                Path temporary = Files.createTempFile(path.getParent(), ".lumenforge-", ".json");
                try {
                    Files.writeString(temporary, roots.get(i).toString() + System.lineSeparator());
                    Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
                } finally {
                    Files.deleteIfExists(temporary);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not update LumenForge torch pack", e);
        }
    }

    private static Path materialPath(String material) {
        return FabricLoader.getInstance().getGameDir()
                .resolve("resourcepacks").resolve(PACK)
                .resolve("assets").resolve("caustica").resolve("caustica")
                .resolve("materials").resolve(material + ".json");
    }
}

