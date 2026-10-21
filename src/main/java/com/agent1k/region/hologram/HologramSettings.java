package com.agent1k.region.hologram;

import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

public record HologramSettings(String type, List<String> lines, double height) {

    public static HologramSettings load(ConfigurationSection config) {
        String path = "hologram-setting.";
        String type = config.getString(path + "hologram-type.type");
        List<String> lines = config.getStringList(path + "hologram.line");
        double height = config.getDouble(path + "hologram.height", 0.5);
        if (!Double.isFinite(height)) height = 0.5;
        return new HologramSettings(type, List.copyOf(lines), height);
    }
}
