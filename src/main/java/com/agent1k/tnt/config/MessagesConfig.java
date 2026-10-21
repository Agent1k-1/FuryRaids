package com.agent1k.tnt.config;

import com.agent1k.FuryRaids;
import com.agent1k.file.AbstractFileConfig;
import com.agent1k.tnt.action.ActionType;
import com.agent1k.tnt.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MessagesConfig extends AbstractFileConfig {

    private final Map<String, Object> cache = new HashMap<>();
    private final boolean papiEnabled;
    private String prefix = "";

    public MessagesConfig(@NotNull FuryRaids plugin) {
        super(plugin, "tnt/messages.yml");
        this.papiEnabled = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
        load();
    }

    @Override
    protected void onLoad() {
        cache.clear();
        String rawPrefix = config.getString("prefix");
        if (rawPrefix != null) this.prefix = rawPrefix;
        for (String key : config.getKeys(false)) {
            cache.put(key, config.get(key));
        }
    }

    public void send(@NotNull CommandSender sender, @NotNull String key) {
        ActionType.dispatchAll(sender, buildLines(key, Map.of()));
    }

    public void send(@NotNull CommandSender sender, @NotNull String key, @NotNull Map<String, String> placeholders) {
        ActionType.dispatchAll(sender, buildLines(key, placeholders));
    }

    @NotNull
    public String getText(@NotNull String key) {
        String first = firstLine(key);
        String stripped = ActionType.stripPrefix(first);
        return ColorUtil.apply(stripped.replace("{prefix}", prefix));
    }

    public boolean isPapiEnabled() {
        return papiEnabled;
    }

    @NotNull
    private List<String> buildLines(@NotNull String key, @NotNull Map<String, String> placeholders) {
        List<String> raw = rawList(key);
        List<String> out = new ArrayList<>(raw.size());
        for (String line : raw) {
            String processed = line.replace("{prefix}", prefix);
            for (Map.Entry<String, String> e : placeholders.entrySet()) {
                processed = processed.replace("%" + e.getKey() + "%", e.getValue());
            }
            out.add(processed);
        }
        return out;
    }

    @NotNull
    private List<String> rawList(@NotNull String key) {
        Object raw = cache.get(key);
        if (raw instanceof String s) return List.of(s);
        if (raw instanceof List<?> list) {
            List<String> out = new ArrayList<>(list.size());
            for (Object o : list) out.add(String.valueOf(o));
            return out;
        }
        return List.of("[message]§c<missing: " + key + ">");
    }

    @NotNull
    private String firstLine(@NotNull String key) {
        Object raw = cache.get(key);
        if (raw instanceof String s) return s;
        if (raw instanceof List<?> list && !list.isEmpty()) return String.valueOf(list.get(0));
        return "§c<missing: " + key + ">";
    }
}
