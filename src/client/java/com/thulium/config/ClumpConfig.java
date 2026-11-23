package com.thulium.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ClumpConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // Main mod toggle
    private boolean enabled = true;

    private double radius = 3;

    private int maxRenderedPerGroup = 4;

    // Boost process priority (Windows / cautious attempt on Linux)
    private boolean boostProcessPriority = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = Math.max(0.1, radius);
    }

    public int getMaxRenderedPerGroup() {
        return maxRenderedPerGroup;
    }

    public void setMaxRenderedPerGroup(int maxRenderedPerGroup) {
        this.maxRenderedPerGroup = Math.max(1, maxRenderedPerGroup);
    }

    public boolean isBoostProcessPriority() {
        return boostProcessPriority;
    }

    public void setBoostProcessPriority(boolean boostProcessPriority) {
        this.boostProcessPriority = boostProcessPriority;
    }

    public static ClumpConfig loadOrCreate(Path configPath) {
        if (Files.exists(configPath)) {
            try (Reader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
                ClumpConfig loaded = GSON.fromJson(reader, ClumpConfig.class);
                if (loaded != null) {
                    loaded.setRadius(loaded.radius);
                    loaded.setMaxRenderedPerGroup(loaded.maxRenderedPerGroup);
                    return loaded;
                }
            } catch (Exception e) {
                System.err.println("[Thulium] Failed to read config, creating a new one: " + e.getMessage());
            }
        }

        ClumpConfig config = new ClumpConfig();
        config.save(configPath);
        return config;
    }

    public void save(Path configPath) {
        try {
            Path parent = configPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (Writer writer = Files.newBufferedWriter(configPath, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            System.err.println("[Thulium] Failed to save config: " + e.getMessage());
        }
    }
}