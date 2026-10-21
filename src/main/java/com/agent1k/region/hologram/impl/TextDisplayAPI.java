package com.agent1k.region.hologram.impl;

import com.agent1k.FuryRaids;
import com.agent1k.region.hologram.HologramProvider;
import com.agent1k.region.util.ColorUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Chunk;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TextDisplayAPI implements HologramProvider {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
            .character('§').hexColors().useUnusualXRepeatedCharacterHexFormat().build();

    private final NamespacedKey marker;
    private final Map<String, TextDisplay> displays = new HashMap<>();

    public TextDisplayAPI(FuryRaids plugin) {
        this.marker = new NamespacedKey(plugin, "region_hologram");
    }

    public static void cleanChunk(FuryRaids plugin, Chunk chunk) {
        NamespacedKey marker = new NamespacedKey(plugin, "region_hologram");
        for (Entity entity : chunk.getEntities()) {
            if (entity instanceof TextDisplay && entity.getPersistentDataContainer().has(marker, PersistentDataType.STRING)) {
                entity.remove();
            }
        }
    }

    @Override
    public void create(String key, Location location, List<String> lines) {
        remove(key);
        TextDisplay display = location.getWorld().spawn(location, TextDisplay.class, entity -> {
            entity.setBillboard(Display.Billboard.CENTER);
            entity.setPersistent(false);
            entity.setInvulnerable(true);
            entity.setGravity(false);
            entity.setSeeThrough(false);
            entity.setDefaultBackground(false);
            entity.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
            entity.getPersistentDataContainer().set(marker, PersistentDataType.STRING, key);
            entity.text(join(lines));
        });
        displays.put(key, display);
    }

    @Override
    public void update(String key, List<String> lines) {
        TextDisplay display = displays.get(key);
        if (display != null && display.isValid()) display.text(join(lines));
    }

    @Override
    public boolean exists(String key) {
        TextDisplay display = displays.get(key);
        return display != null && display.isValid();
    }

    @Override
    public void remove(String key) {
        TextDisplay display = displays.remove(key);
        if (display != null) display.remove();
    }

    @Override
    public void removeAll() {
        for (TextDisplay display : displays.values()) display.remove();
        displays.clear();
    }

    private Component join(List<String> lines) {
        return LEGACY.deserialize(ColorUtil.apply(String.join("\n", lines))).colorIfAbsent(NamedTextColor.WHITE);
    }
}
