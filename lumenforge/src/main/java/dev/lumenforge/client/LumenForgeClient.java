package dev.lumenforge.client;

import dev.comfyfluffy.caustica.CausticaConfig;
import net.fabricmc.api.ClientModInitializer;

public final class LumenForgeClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CausticaConfig.ensureRegistered();
        ExposureProfiles.applyAndSave();
    }
}
