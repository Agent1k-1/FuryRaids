package com.agent1k.tnt.listener;

import com.agent1k.FuryRaids;
import com.agent1k.tnt.TntType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class PlacementListener implements Listener {

    private final FuryRaids plugin;

    public PlacementListener(@NotNull FuryRaids plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(@NotNull BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        String typeId = plugin.getTntItems().readTypeId(item);
        if (typeId == null) return;
        TntType type = plugin.getTntRegistry().byId(typeId);
        if (type == null) {
            event.setCancelled(true);
            return;
        }
        if (type.autofire()) {
            event.setCancelled(true);
            return;
        }
        plugin.getTntTracker().mark(event.getBlock(), typeId);
    }
}
