package com.agent1k.region.hologram.impl;

import com.agent1k.region.hologram.HologramProvider;
import com.agent1k.region.util.ColorUtil;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class DecentHologramsAPI implements HologramProvider {

    private final Map<String, Hologram> holograms = new HashMap<>();

    public static boolean isAvailable() {
        return Bukkit.getPluginManager().isPluginEnabled("DecentHolograms");
    }

    @Override
    public void create(String key, Location location, List<String> lines) {
        remove(key);
        String name = name(key);
        Hologram previous = DHAPI.getHologram(name);
        if (previous != null) previous.delete();
        holograms.put(key, DHAPI.createHologram(name, location, false, colorize(lines)));
    }

    @Override
    public void update(String key, List<String> lines) {
        Hologram hologram = holograms.get(key);
        if (hologram != null) DHAPI.setHologramLines(hologram, colorize(lines));
    }

    @Override
    public boolean exists(String key) {
        Hologram hologram = holograms.get(key);
        return hologram != null && DHAPI.getHologram(name(key)) == hologram;
    }

    @Override
    public void remove(String key) {
        Hologram hologram = holograms.remove(key);
        if (hologram != null) hologram.delete();
    }

    @Override
    public void removeAll() {
        for (Hologram hologram : holograms.values()) hologram.delete();
        holograms.clear();
    }

    private String name(String key) {
        return "eregionplus_" + UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8));
    }

    private List<String> colorize(List<String> lines) {
        return lines.stream().map(ColorUtil::apply).toList();
    }
}
