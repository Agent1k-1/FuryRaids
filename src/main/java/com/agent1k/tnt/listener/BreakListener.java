package com.agent1k.tnt.listener;

import com.agent1k.FuryRaids;
import com.agent1k.tnt.TntType;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;

public final class BreakListener implements Listener {

    private final FuryRaids plugin;

    public BreakListener(@NotNull FuryRaids plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBreak(@NotNull BlockBreakEvent event) {
        Block block = event.getBlock();
        String typeId = plugin.getTntTracker().read(block);
        if (typeId == null) return;

        plugin.getTntTracker().clear(block);
        event.setDropItems(false);

        TntType type = plugin.getTntRegistry().byId(typeId);
        if (type == null) return;
        block.getWorld().dropItemNaturally(
                block.getLocation().add(0.5, 0.5, 0.5),
                plugin.getTntItems().build(type, 1)
        );
    }
}
