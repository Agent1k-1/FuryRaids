package com.agent1k.region.listener;

import com.agent1k.FuryRaids;
import com.agent1k.region.data.RegionData;
import com.agent1k.api.flag.RegionFlag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public final class ChestListener extends AbstractRegionListener {

    public ChestListener(@NotNull FuryRaids plugin) {
        super(plugin);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onInteract(@NotNull PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null) return;
        if (!(block.getState() instanceof InventoryHolder)) return;

        RegionData region = regionAt(block.getLocation());
        if (region == null) return;
        Player player = event.getPlayer();
        if (isBypass(player)) return;
        if (isMember(region, player.getUniqueId())) return;
        if (allowedByFlag(region, RegionFlag.ACCESS)) return;
        if (allowedByFlag(region, RegionFlag.CHEST)) return;

        event.setCancelled(true);
        notify(player, "protect-chest");
    }
}
