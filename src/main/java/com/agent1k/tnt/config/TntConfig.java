package com.agent1k.tnt.config;

import com.agent1k.FuryRaids;
import com.agent1k.file.AbstractFileConfig;
import com.agent1k.tnt.ability.AbilityEntry;
import com.agent1k.tnt.ability.AbilityKind;
import com.agent1k.tnt.TntRegistry;
import com.agent1k.tnt.TntType;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class TntConfig extends AbstractFileConfig {

    private final TntRegistry registry;

    public int defaultFuseTicks() { return Math.max(1, config.getInt("settings.default-fuse-ticks")); }
    public boolean incendiary() { return config.getBoolean("settings.incendiary"); }
    public int vanillaRegionDamage() { return Math.max(0, config.getInt("settings.region-damage")); }

    public TntConfig(@NotNull FuryRaids plugin, @NotNull TntRegistry registry) {
        super(plugin, "tnt/tnt.yml");
        this.registry = registry;
        load();
    }

    @Override
    protected void onLoad() {
        List<TntType> parsed = new ArrayList<>();
        ConfigurationSection root = config.getConfigurationSection("types");
        if (root != null) {
            for (String id : root.getKeys(false)) {
                ConfigurationSection section = root.getConfigurationSection(id);
                if (section == null) continue;
                TntType type = parse(id, section);
                if (type != null) parsed.add(type);
            }
        }
        registry.replaceAll(parsed);
        plugin.getLogger().info("Загружено типов TNT: " + parsed.size());
    }

    private TntType parse(@NotNull String id, @NotNull ConfigurationSection section) {
        String rawMaterial = section.getString("material");
        Material material = rawMaterial != null ? Material.matchMaterial(rawMaterial) : null;
        if (material == null) {
            plugin.getLogger().warning("Неверный материал для TNT '" + id + "', используется TNT");
            material = Material.TNT;
        }
        String name = section.getString("name");
        List<String> lore = section.getStringList("lore");
        int radius = Math.max(1, section.getInt("radius"));
        boolean autofire = section.getBoolean("autofire");
        List<AbilityEntry> abilities = parseAbilities(id, section.getList("abilities"));
        return new TntType(id, name, lore, material, radius, autofire, abilities,
                Math.max(0, section.getInt("region-damage")));
    }

    @NotNull
    private List<AbilityEntry> parseAbilities(@NotNull String typeId, List<?> raw) {
        List<AbilityEntry> out = new ArrayList<>();
        if (raw == null) return out;
        for (Object item : raw) {
            if (!(item instanceof Map<?, ?> map)) continue;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                AbilityKind kind = AbilityKind.from(String.valueOf(entry.getKey()));
                if (kind == null) {
                    plugin.getLogger().warning("Неизвестная способность '" + entry.getKey() + "' у TNT '" + typeId + "'");
                    continue;
                }
                int size;
                try {
                    size = Integer.parseInt(String.valueOf(entry.getValue()));
                } catch (NumberFormatException ex) {
                    plugin.getLogger().warning("Неверный размер способности '" + kind.getKey() + "' у TNT '" + typeId + "'");
                    continue;
                }
                out.add(new AbilityEntry(kind, Math.max(0, size)));
            }
        }
        return out;
    }
}
