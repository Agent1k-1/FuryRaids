package com.agent1k.region.hologram;

import org.bukkit.Location;

import java.util.List;

public interface HologramProvider {

    void create(String key, Location location, List<String> lines);

    void update(String key, List<String> lines);

    boolean exists(String key);

    void remove(String key);

    void removeAll();
}
