package com.agent1k.region.hologram;

import com.agent1k.FuryRaids;
import com.agent1k.region.data.RegionData;
import com.agent1k.region.hologram.impl.DecentHologramsAPI;
import com.agent1k.region.hologram.impl.TextDisplayAPI;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class HologramManager implements Listener {

    private final FuryRaids plugin;
    private final Map<ChunkKey, Set<String>> chunks = new HashMap<>();
    private final Map<String, List<String>> active = new HashMap<>();
    private final Set<String> failures = new HashSet<>();
    private HologramSettings settings;
    private HologramProvider provider;
    private BukkitTask task;

    public HologramManager(FuryRaids plugin) {
        this.plugin = plugin;
    }

    public void start() {
        reload();
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::refreshActive, 20L, 20L);
    }

    public void reload() {
        clear();
        settings = HologramSettings.load(plugin.getConfig());
        provider = pickProvider();
        for (World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) TextDisplayAPI.cleanChunk(plugin, chunk);
        }
        for (RegionData region : plugin.getRegionManager().all()) add(region);
    }

    public void add(RegionData region) {
        chunks.computeIfAbsent(key(region), ignored -> new HashSet<>()).add(region.getId());
        refresh(region);
    }

    public void remove(RegionData region) {
        Set<String> ids = chunks.get(key(region));
        if (ids != null) {
            ids.remove(region.getId());
            if (ids.isEmpty()) chunks.remove(key(region));
        }
        hide(region.getId());
        failures.remove(region.getId());
    }

    public void shutdown() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        clear();
    }

    private void clear() {
        if (provider != null) {
            try {
                provider.removeAll();
            } catch (RuntimeException | LinkageError e) {
                plugin.getLogger().warning("Не удалось удалить голограммы: " + e.getMessage());
            }
        }
        active.clear();
        chunks.clear();
        failures.clear();
    }

    private HologramProvider pickProvider() {
        if (settings.type().equalsIgnoreCase("DecentHolograms")) {
            if (DecentHologramsAPI.isAvailable()) return new DecentHologramsAPI();
            plugin.getLogger().warning("DecentHolograms не установлен или отключён. Используется TextDisplay.");
        } else if (!settings.type().equalsIgnoreCase("TextDisplay")) {
            plugin.getLogger().warning("Неизвестный тип голограмм: " + settings.type() + ". Используется TextDisplay.");
        }
        return new TextDisplayAPI(plugin);
    }

    private void refreshActive() {
        for (String id : new ArrayList<>(active.keySet())) {
            RegionData region = plugin.getRegionManager().byId(id);
            if (region == null) hide(id);
            else refresh(region);
        }
    }

    private void refresh(RegionData region) {
        if (provider == null || settings.lines().isEmpty()) return;
        World world = Bukkit.getWorld(region.getWorld());
        if (world == null || !world.isChunkLoaded(region.getX() >> 4, region.getZ() >> 4)) {
            hide(region.getId());
            return;
        }
        try {
            List<String> lines = settings.lines();
            if (plugin.getPapiHook() != null && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                lines = plugin.getPapiHook().apply(region, lines);
            }
            if (!provider.exists(region.getId())) {
                Location location = new Location(world, region.getX() + 0.5,
                        region.getY() + 1.0 + settings.height(), region.getZ() + 0.5);
                provider.create(region.getId(), location, lines);
            } else if (!lines.equals(active.get(region.getId()))) {
                provider.update(region.getId(), lines);
            }
            active.put(region.getId(), lines);
            failures.remove(region.getId());
        } catch (RuntimeException | LinkageError e) {
            active.putIfAbsent(region.getId(), List.of());
            if (failures.add(region.getId())) {
                plugin.getLogger().warning("Не удалось обновить голограмму региона " + region.getId() + ": " + e.getMessage());
            }
        }
    }

    private void hide(String id) {
        active.remove(id);
        if (provider == null) return;
        try {
            provider.remove(id);
        } catch (RuntimeException | LinkageError e) {
            if (failures.add(id)) plugin.getLogger().warning("Не удалось удалить голограмму региона " + id + ": " + e.getMessage());
        }
    }

    private ChunkKey key(RegionData region) {
        return new ChunkKey(region.getWorld(), region.getX() >> 4, region.getZ() >> 4);
    }

    private void showChunk(Chunk chunk) {
        if (!chunk.getWorld().isChunkLoaded(chunk.getX(), chunk.getZ())) return;
        TextDisplayAPI.cleanChunk(plugin, chunk);
        Set<String> ids = chunks.get(new ChunkKey(chunk.getWorld().getName(), chunk.getX(), chunk.getZ()));
        if (ids == null) return;
        for (String id : ids) {
            RegionData region = plugin.getRegionManager().byId(id);
            if (region != null) refresh(region);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        Chunk chunk = event.getChunk();
        Bukkit.getScheduler().runTask(plugin, () -> showChunk(chunk));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkUnload(ChunkUnloadEvent event) {
        Chunk chunk = event.getChunk();
        Set<String> ids = chunks.get(new ChunkKey(chunk.getWorld().getName(), chunk.getX(), chunk.getZ()));
        if (ids != null) for (String id : ids) hide(id);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldLoad(WorldLoadEvent event) {
        for (Chunk chunk : event.getWorld().getLoadedChunks()) showChunk(chunk);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldUnload(WorldUnloadEvent event) {
        for (var entry : chunks.entrySet()) {
            if (entry.getKey().world().equals(event.getWorld().getName())) {
                for (String id : entry.getValue()) hide(id);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginEnable(PluginEnableEvent event) {
        if (event.getPlugin().getName().equals("DecentHolograms")
                && settings.type().equalsIgnoreCase("DecentHolograms")) reload();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginDisable(PluginDisableEvent event) {
        if (event.getPlugin().getName().equals("DecentHolograms") && provider instanceof DecentHologramsAPI) {
            clear();
            provider = new TextDisplayAPI(plugin);
            for (RegionData region : plugin.getRegionManager().all()) add(region);
        }
    }

    private record ChunkKey(String world, int x, int z) { }
}
