package com.agent1k.file;

import com.agent1k.FuryRaids;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public final class FileLoader {
    private final FuryRaids plugin;
    private final String path;
    private final File file;
    private YamlConfiguration config;

    public FileLoader(FuryRaids plugin, String path) {
        this.plugin = plugin;
        this.path = path;
        this.file = new File(plugin.getDataFolder(), path);
    }

    public YamlConfiguration load() {
        try {
            Files.createDirectories(file.toPath().getParent());
            if (!file.exists()) plugin.saveResource(path, false);
            YamlConfiguration loaded = new YamlConfiguration();
            loaded.load(file);
            config = loaded;
            return loaded;
        } catch (IOException | InvalidConfigurationException e) {
            throw new IllegalStateException("Не удалось загрузить " + path, e);
        }
    }

    public FileConfiguration getConfig() {
        return config == null ? load() : config;
    }

    public void save() {
        if (config == null) return;
        try {
            config.save(file);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось сохранить " + path, e);
        }
    }

    public File getFile() { return file; }
}
