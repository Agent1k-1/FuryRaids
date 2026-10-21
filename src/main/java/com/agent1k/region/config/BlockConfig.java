package com.agent1k.region.config;

import com.agent1k.FuryRaids;
import com.agent1k.file.AbstractFileConfig;
import com.agent1k.region.data.RegionType;
import com.agent1k.region.data.ViewSettings;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class BlockConfig extends AbstractFileConfig {

    private final Map<Integer, RegionType> types = new LinkedHashMap<>();
    private final Set<String> blockedWorlds = new HashSet<>();
    private final Map<Integer, ViewSettings> views = new LinkedHashMap<>();
    private ViewSettings defaultView;

    public BlockConfig(@NotNull FuryRaids plugin) {
        super(plugin, "region/block.yml");
        load();
    }

    @Override
    protected void onLoad() {
        types.clear();
        blockedWorlds.clear();
        views.clear();
        ConfigurationSection globalView = plugin.getConfig().getConfigurationSection("view");
        defaultView = ViewSettings.load(null, globalView, plugin.getLogger()::warning);
        boolean changed = false;

        ConfigurationSection root = config.getConfigurationSection("regions");
        if (root != null) {
            for (String key : root.getKeys(false)) {
                int id = parseId(key);
                if (id < 0) continue;
                ConfigurationSection section = root.getConfigurationSection(key);
                if (section == null) continue;

                Material material = parseMaterial(section.getString("material"));
                String displayName = section.getString("display-name");
                List<String> lore = section.getStringList("lore");
                int radius = Math.max(1, section.getInt("radius"));

                int durability = Math.max(0, section.getInt("durability", 5));
                types.put(id, new RegionType(id, material, displayName, lore, radius, durability));
                if (!section.isConfigurationSection("view") && !section.isSet("view")) {
                    ConfigurationSection view = section.createSection("view");
                    view.set("particle", id == 2 ? "minecraft:end_rod" : id == 3 ? "minecraft:soul_fire_flame" : "minecraft:dust");
                    if (id != 2 && id != 3) view.set("color", "#55FFFF");
                    changed = true;
                }
                views.put(id, ViewSettings.load(section.getConfigurationSection("view"), globalView,
                        message -> plugin.getLogger().warning("Тип региона " + id + ": " + message)));
            }
        }
        blockedWorlds.addAll(config.getStringList("blocked-worlds"));
        if (changed) {
            try { config.save(file); }
            catch (java.io.IOException e) { plugin.getLogger().warning("Ошибка обновления block.yml: " + e.getMessage()); }
        }
    }

    private int parseId(@NotNull String raw) {
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            plugin.getLogger().warning("Некорректный id региона: " + raw);
            return -1;
        }
    }

    @NotNull
    private Material parseMaterial(@Nullable String raw) {
        Material mat = raw != null ? Material.matchMaterial(raw) : null;
        return mat != null ? mat : Material.DIAMOND_BLOCK;
    }

    @Nullable
    public RegionType byId(int id) {
        return types.get(id);
    }

    @NotNull
    public Collection<RegionType> all() {
        return Collections.unmodifiableCollection(types.values());
    }

    public boolean isBlockedWorld(@NotNull String world) {
        return blockedWorlds.contains(world);
    }

    @NotNull
    public ViewSettings viewById(int id) { return views.getOrDefault(id, defaultView); }
}
