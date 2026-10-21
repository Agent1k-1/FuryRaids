package com.agent1k.region.gui.menu.loader;

import com.agent1k.region.gui.menu.config.MainMenu;
import com.agent1k.FuryRaids;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.logging.Logger;

public final class MenuLoader {
    private final File directory;
    private final FuryRaids plugin;
    private final Logger logger;
    private MainMenu mainMenu;

    public MenuLoader(FuryRaids plugin) {
        this.plugin = plugin;
        this.directory = new File(plugin.getDataFolder(), "region/menu");
        this.logger = plugin.getLogger();
    }

    public boolean reload() {
        try {
            Files.createDirectories(directory.toPath());
            File file = new File(directory, "menu.yml");
            YamlConfiguration config = plugin.getFiles().get("region/menu/menu.yml").load();
            MainMenu loaded = MainMenu.from(config);
            if (installAddMemberButton(config, loaded)) {
                loaded = MainMenu.from(config);
                config.save(file);
            }
            mainMenu = loaded;
            return true;
        } catch (IOException | InvalidConfigurationException | IllegalArgumentException | IllegalStateException e) {
            logger.severe("Не удалось загрузить region/menu/menu.yml." + (e.getMessage() == null ? "" : " " + e.getMessage()));
            return false;
        }
    }

    private boolean installAddMemberButton(YamlConfiguration config, MainMenu menu)
            throws IOException, InvalidConfigurationException {
        String path = "menu-settings.add-member";
        if (config.contains(path)) return false;
        int slot = 16;
        if (slot >= menu.size() || menu.items().containsKey(slot)) {
            slot = -1;
            for (int candidate = 0; candidate < menu.size(); candidate++) {
                if (!menu.items().containsKey(candidate)) {
                    slot = candidate;
                    break;
                }
            }
        }
        if (slot < 0) return false;
        try (InputStream stream = plugin.getResource("region/menu/menu.yml")) {
            if (stream == null) return false;
            YamlConfiguration defaults = new YamlConfiguration();
            defaults.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
            ConfigurationSection button = defaults.getConfigurationSection(path);
            if (button == null) return false;
            for (var entry : button.getValues(false).entrySet()) config.set(path + "." + entry.getKey(), entry.getValue());
            config.set(path + ".slot", slot);
            return true;
        }
    }

    public MainMenu getMainMenu() { return mainMenu; }
}
