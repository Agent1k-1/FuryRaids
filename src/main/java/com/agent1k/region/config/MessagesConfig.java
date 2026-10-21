package com.agent1k.region.config;

import com.agent1k.FuryRaids;
import com.agent1k.file.FileLoader;
import com.agent1k.region.action.ActionType;
import com.agent1k.region.util.ColorUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public final class MessagesConfig {
    private final FuryRaids plugin;
    private final FileLoader loader;
    private FileConfiguration messages;

    public MessagesConfig(@NotNull FuryRaids plugin) {
        this.plugin = plugin;
        this.loader = plugin.getFiles().get("region/messages.yml");
        load();
    }

    public void load() {
        messages = loader.load();
    }

    public void reload() {
        load();
    }

    public @NotNull FileConfiguration getMessages() {
        return messages;
    }

    public @NotNull FileConfiguration raw() {
        return messages;
    }

    public @NotNull List<String> getMessage(@NotNull String key) {
        return getLines("messages." + key);
    }

    public void send(@NotNull CommandSender sender, @NotNull String key) {
        send(sender, key, Map.of());
    }

    public void send(@NotNull CommandSender sender, @NotNull String key,
                     @NotNull Map<String, String> placeholders) {
        Player player = sender instanceof Player online && isPapiEnabled() ? online : null;
        for (String line : getMessage(key)) {
            String text = line;
            for (var entry : placeholders.entrySet()) {
                text = text.replace("%" + entry.getKey() + "%", entry.getValue());
            }
            if (player != null) text = PlaceholderAPI.setPlaceholders(player, text);
            ActionType.dispatch(sender, text);
        }
    }

    public @NotNull String getText(@NotNull String key) {
        List<String> lines = getLines("placeholder." + key);
        return lines.isEmpty() ? "" : ColorUtil.apply(ActionType.stripPrefix(lines.getFirst()));
    }

    public boolean isPapiEnabled() {
        return Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

    private List<String> getLines(String path) {
        Object value = messages.get(path);
        return value instanceof String line ? List.of(line) : messages.getStringList(path);
    }
}
