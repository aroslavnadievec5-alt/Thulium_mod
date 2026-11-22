package com.thulium;

import com.thulium.config.ClumpConfig;
import com.thulium.util.ProcessPriorityManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class ThuliumClient implements ClientModInitializer {
    public static final String MOD_ID = "thulium";

    private static ClumpConfig config;

    @Override
    public void onInitializeClient() {
        Path configPath = FabricLoader.getInstance()
                .getConfigDir()
                .resolve(MOD_ID + ".json");

        config = ClumpConfig.loadOrCreate(configPath);
        ProcessPriorityManager.applyIfEnabled(config.isBoostProcessPriority());

        System.out.println("[Thulium] Initialized client mod (enabled=" + config.isEnabled() + ").");
    }

    public static ClumpConfig getConfig() {
        return config;
    }
}