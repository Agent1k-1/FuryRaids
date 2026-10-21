package com.agent1k.region.gui.menu.config;

import com.agent1k.region.click.ClickType;
import com.agent1k.region.click.open.OpenClick;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public record MainMenu(String name, int size, Set<OpenClick> openClicks, Map<Integer, MenuItem> items) {
    public MainMenu {
        if (name == null) throw new IllegalArgumentException();
        if (size < 9 || size > 54 || size % 9 != 0) {
            throw new IllegalArgumentException();
        }
        openClicks = Set.copyOf(openClicks);
        items = Map.copyOf(items);
        for (var entry : items.entrySet()) {
            if (entry.getKey() < 0 || entry.getKey() >= size || entry.getKey() != entry.getValue().slot()) {
                throw new IllegalArgumentException();
            }
        }
    }

    public record MenuItem(String name, List<String> lore, int slot, Material material,
                           Map<ClickType, List<String>> actions) {
        public MenuItem {
            lore = List.copyOf(lore);
            Map<ClickType, List<String>> copy = new EnumMap<>(ClickType.class);
            actions.forEach((key, value) -> copy.put(key, List.copyOf(value)));
            actions = Map.copyOf(copy);
        }

        public List<String> actionsFor(ClickType click) { return actions.getOrDefault(click, List.of()); }
    }

    public static MainMenu from(ConfigurationSection config) {
        return from(config, material -> !material.isAir() && material.isItem());
    }

    public static MainMenu from(ConfigurationSection config, Predicate<Material> usableMaterial) {
        Set<OpenClick> openClicks = EnumSet.noneOf(OpenClick.class);
        List<String> names = config.isSet("open-click") ? strings(config, "open-click") : List.of("RIGHT");
        for (String name : names) openClicks.add(OpenClick.from(name));
        if (config.isSet("size") && !config.isInt("size")) {
            throw new IllegalArgumentException();
        }
        int size = config.getInt("size");
        ConfigurationSection settings = config.getConfigurationSection("menu-settings");
        if (settings == null) throw new IllegalArgumentException();
        Map<Integer, MenuItem> items = new LinkedHashMap<>();
        for (String id : settings.getKeys(false)) {
            ConfigurationSection item = settings.getConfigurationSection(id);
            if (item == null) throw new IllegalArgumentException();
            if (!item.isInt("slot")) throw new IllegalArgumentException();
            int slot = item.getInt("slot");
            Material material = Material.matchMaterial(item.getString("material", ""));
            if (material == null || !usableMaterial.test(material)) {
                throw new IllegalArgumentException();
            }
            Map<ClickType, List<String>> actions = new EnumMap<>(ClickType.class);
            for (ClickType click : ClickType.values()) actions.put(click, strings(item, click.getKey()));
            MenuItem button = new MenuItem(item.getString("name", id), strings(item, "lore"), slot, material, actions);
            if (items.putIfAbsent(slot, button) != null) throw new IllegalArgumentException();
        }
        return new MainMenu(config.getString("name"), size, openClicks, items);
    }

    private static List<String> strings(ConfigurationSection config, String key) {
        if (!config.isSet(key)) return List.of();
        List<?> values = config.getList(key);
        if (values == null || values.stream().anyMatch(value -> !(value instanceof String))) {
            throw new IllegalArgumentException();
        }
        return config.getStringList(key);
    }
}
