package com.agent1k.region.listener;

import com.agent1k.FuryRaids;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

public final class WorldSyncListener implements Listener {
    private final FuryRaids plugin;

    public WorldSyncListener(FuryRaids plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldLoad(WorldLoadEvent event) {
        Bukkit.getScheduler().runTask(plugin, () -> plugin.getRegionManager().syncWorld(event.getWorld()));
    }
}
