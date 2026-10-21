package com.agent1k.region.data;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

public record ViewSettings(boolean enabled, Particle particle, Object data, int durationTicks,
                           int intervalTicks, double spacing, int count, double offsetX,
                           double offsetY, double offsetZ, double speed, double range, int maxPoints) {
    public static final Map<String, Object> DEFAULTS = Map.ofEntries(
            Map.entry("enabled", true), Map.entry("particle", "minecraft:dust"),
            Map.entry("color", "#55FF55"), Map.entry("to-color", "#55FFFF"), Map.entry("size", 1.0),
            Map.entry("material", "STONE"), Map.entry("value", 0.0),
            Map.entry("duration-seconds", 15), Map.entry("interval-ticks", 10),
            Map.entry("spacing", 1.0), Map.entry("count", 1),
            Map.entry("offset-x", 0.0), Map.entry("offset-y", 0.0), Map.entry("offset-z", 0.0),
            Map.entry("speed", 0.0), Map.entry("range", 128.0), Map.entry("max-points", 1200));

    public static boolean installDefaults(ConfigurationSection config, String path) {
        boolean changed = false;
        for (var entry : DEFAULTS.entrySet()) {
            String key = path + "." + entry.getKey();
            if (!config.isSet(key)) {
                config.set(key, entry.getValue());
                changed = true;
            }
        }
        return changed;
    }

    public static ViewSettings load(ConfigurationSection local, ConfigurationSection global, Consumer<String> warning) {
        Reader reader = new Reader(local, global);
        Particle particle;
        Object data;
        try {
            particle = resolveParticle(reader.string("particle"));
            data = particleData(particle, reader);
        } catch (IllegalArgumentException e) {
            warning.accept("Настройки частиц: " + e.getMessage() + ". Используется minecraft:end_rod.");
            particle = Particle.END_ROD;
            data = null;
        }
        return new ViewSettings(reader.bool("enabled"), particle, data,
                (int) reader.number("duration-seconds", 1, 60) * 20,
                (int) reader.number("interval-ticks", 5, 40), reader.number("spacing", 0.25, 16),
                (int) reader.number("count", 1, 3), reader.number("offset-x", 0, 2),
                reader.number("offset-y", 0, 2), reader.number("offset-z", 0, 2),
                reader.number("speed", 0, 1), reader.number("range", 8, 256),
                (int) reader.number("max-points", 48, 1200));
    }

    public static Particle resolveParticle(String id) {
        String normalized = id.trim().toLowerCase(Locale.ROOT);
        for (Particle particle : Particle.values()) {
            if (particle.getKey().toString().equals(normalized)
                    || particle.getKey().getKey().equals(normalized)
                    || particle.name().equalsIgnoreCase(normalized)) return particle;
        }
        throw new IllegalArgumentException("неизвестная частица " + id);
    }

    private static Object particleData(Particle particle, Reader reader) {
        Class<?> dataType = particle.getDataType();
        if (dataType == Void.class) return null;
        if (dataType == Particle.DustTransition.class) {
            return new Particle.DustTransition(color(reader.string("color")), color(reader.string("to-color")),
                    (float) reader.number("size", 0.1, 4));
        }
        if (dataType == Particle.DustOptions.class) {
            return new Particle.DustOptions(color(reader.string("color")), (float) reader.number("size", 0.1, 4));
        }
        if (dataType == Color.class) return color(reader.string("color"));
        if (dataType == Float.class) return (float) reader.number("value", 0, 360);
        if (dataType == Integer.class) return (int) reader.number("value", 0, 200);
        if (dataType == BlockData.class || dataType == ItemStack.class) {
            Material material = Material.matchMaterial(reader.string("material"));
            if (material == null || material.isAir()) throw new IllegalArgumentException("неверный материал частицы");
            if (dataType == ItemStack.class) {
                if (!material.isItem()) throw new IllegalArgumentException("материал не является предметом");
                return new ItemStack(material);
            }
            if (!material.isBlock()) throw new IllegalArgumentException("материал не является блоком");
            return Bukkit.createBlockData(material);
        }
        throw new IllegalArgumentException("частица " + particle.getKey() + " требует неподдерживаемые данные");
    }

    private static Color color(String text) {
        String hex = text.trim();
        if (hex.startsWith("#")) hex = hex.substring(1);
        if (!hex.matches("[0-9a-fA-F]{6}")) throw new IllegalArgumentException("цвет должен иметь вид #55FF55");
        return Color.fromRGB(Integer.parseInt(hex, 16));
    }

    private record Reader(ConfigurationSection local, ConfigurationSection global) {
        Object value(String key) {
            if (local != null && local.isSet(key)) return local.get(key);
            if (global != null && global.isSet(key)) return global.get(key);
            return DEFAULTS.get(key);
        }

        String string(String key) { return String.valueOf(value(key)); }

        boolean bool(String key) { return Boolean.parseBoolean(string(key)); }

        double number(String key, double min, double max) {
            double number;
            try { number = Double.parseDouble(string(key)); }
            catch (NumberFormatException e) { number = ((Number) DEFAULTS.get(key)).doubleValue(); }
            if (!Double.isFinite(number)) number = ((Number) DEFAULTS.get(key)).doubleValue();
            return Math.max(min, Math.min(max, number));
        }
    }
}
