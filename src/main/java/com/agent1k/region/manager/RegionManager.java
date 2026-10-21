package com.agent1k.region.manager;

import com.agent1k.FuryRaids;
import com.agent1k.region.cache.TTLCache;
import com.agent1k.region.data.RegionData;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RegionManager {

    private final FuryRaids plugin;
    private final Map<String, RegionData> regions = new ConcurrentHashMap<>();
    private final TTLCache<String, String> locationCache;

    public RegionManager(@NotNull FuryRaids plugin) {
        this.plugin = plugin;
        long ttl = plugin.getConfig().getLong("cache.location-ttl-seconds");
        this.locationCache = new TTLCache<>(ttl);
    }

    public void loadAll() {
        regions.clear();
        locationCache.clear();
        for (RegionData r : plugin.getDatabase().loadAll()) {
            regions.put(r.getId(), r);
        }
        plugin.getLogger().info("Загружено регионов: " + regions.size());
    }

    public boolean register(@NotNull RegionData region) {
        var type = plugin.getBlocks().byId(region.getTypeId());
        region.initializeDurability(type == null ? 5 : type.durability());
        try {
            plugin.getWorldGuardHook().upsert(region);
            plugin.getDatabase().save(region).join();
        } catch (RuntimeException e) {
            plugin.getLogger().severe("Не удалось создать регион: " + e.getMessage());
            try { plugin.getWorldGuardHook().remove(region); } catch (RuntimeException ignored) { }
            return false;
        }
        regions.put(region.getId(), region);
        locationCache.clear();
        if (plugin.getHolograms() != null) plugin.getHolograms().add(region);
        return true;
    }

    public boolean unregister(@NotNull String id) {
        RegionData region = regions.get(id);
        if (region == null) return false;
        try {
            plugin.getWorldGuardHook().remove(region);
            plugin.getDatabase().delete(id).join();
        } catch (RuntimeException e) {
            plugin.getLogger().severe("Не удалось удалить регион: " + e.getMessage());
            try { plugin.getWorldGuardHook().upsert(region); } catch (RuntimeException ignored) { }
            return false;
        }
        regions.remove(id);
        locationCache.clear();
        if (plugin.getHolograms() != null) plugin.getHolograms().remove(region);
        return true;
    }

    public boolean persist(@NotNull RegionData region) {
        try {
            plugin.getWorldGuardHook().upsert(region);
            plugin.getDatabase().save(region).join();
            return true;
        } catch (RuntimeException e) {
            plugin.getLogger().severe("Не удалось сохранить регион: " + e.getMessage());
            return false;
        }
    }

    public void syncWorldGuard() {
        for (World world : Bukkit.getWorlds()) syncWorld(world);
    }

    public void syncWorld(World world) {
        try {
            plugin.getWorldGuardHook().reconcile(world, all());
        } catch (RuntimeException e) {
            plugin.getLogger().severe("Ошибка синхронизации регионов: " + e.getMessage());
        }
    }

    @Nullable
    public RegionData at(@NotNull Location loc) {
        World w = loc.getWorld();
        if (w == null) return null;
        String key = w.getName() + ':' + loc.getBlockX() + ':' + loc.getBlockY() + ':' + loc.getBlockZ();
        String cachedId = locationCache.get(key);
        if (cachedId != null) {
            RegionData cached = regions.get(cachedId);
            if (cached != null && cached.contains(loc)) return cached;
            locationCache.invalidate(key);
        }
        for (RegionData r : regions.values()) {
            if (r.contains(loc)) {
                locationCache.put(key, r.getId());
                return r;
            }
        }
        return null;
    }

    @Nullable
    public RegionData byId(@NotNull String id) {
        return regions.get(id);
    }

    @NotNull
    public List<RegionData> byOwner(@NotNull UUID owner) {
        List<RegionData> out = new ArrayList<>();
        for (RegionData r : regions.values()) {
            if (r.getOwner().equals(owner)) out.add(r);
        }
        return out;
    }

    public boolean overlaps(@NotNull String world, int x, int z, int radius, int gap) {
        for (RegionData r : regions.values()) {
            if (r.overlaps(world, x, z, radius, gap)) return true;
        }
        return false;
    }

    public boolean overlaps(@NotNull String world, int x, int y, int z, int radius, int gap) {
        for (RegionData r : regions.values()) {
            if (r.overlaps(world, x, y, z, radius, gap)) return true;
        }
        return false;
    }

    @NotNull
    public Collection<RegionData> all() {
        return Collections.unmodifiableCollection(regions.values());
    }

    public void invalidateLocationCache() {
        locationCache.clear();
    }
}
