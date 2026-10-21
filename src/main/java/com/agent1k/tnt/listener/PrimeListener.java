package com.agent1k.tnt.listener;

import com.agent1k.FuryRaids;
import org.bukkit.block.Block;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.jetbrains.annotations.NotNull;

public final class PrimeListener implements Listener {

    private final FuryRaids plugin;

    public PrimeListener(@NotNull FuryRaids plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onSpawn(@NotNull EntitySpawnEvent event) {
        if (!(event.getEntity() instanceof TNTPrimed tnt)) return;
        Block block = tnt.getLocation().getBlock();
        String typeId = plugin.getTntTracker().read(block);
        if (typeId == null) return;

        plugin.getTntItems().writeTypeId(tnt, typeId);
        plugin.getTntTracker().clear(block);
    }
}
