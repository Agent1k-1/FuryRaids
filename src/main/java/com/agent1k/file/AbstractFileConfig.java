package com.agent1k.file;

import com.agent1k.FuryRaids;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public abstract class AbstractFileConfig {
    protected final FuryRaids plugin;
    protected final File file;
    protected FileConfiguration config;
    private final FileLoader loader;

    protected AbstractFileConfig(FuryRaids plugin, String path) {
        this.plugin = plugin;
        this.loader = plugin.getFiles().get(path);
        this.file = loader.getFile();
    }

    public final void load() {
        config = loader.load();
        onLoad();
    }

    public final void reload() { load(); }
    public final FileConfiguration raw() { return config; }
    protected abstract void onLoad();
}
