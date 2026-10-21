package com.agent1k.region.listener;

import com.agent1k.FuryRaids;
import com.agent1k.region.data.RegionData;
import com.agent1k.api.flag.RegionFlag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

public final class AccessListener extends AbstractRegionListener {

    public AccessListener(@NotNull FuryRaids plugin) {
        super(plugin);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onBreak(@NotNull BlockBreakEvent event) {
        guard(event, event.getPlayer(), event.getBlock(), "protect-break");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onPlace(@NotNull BlockPlaceEvent event) {
        guard(event, event.getPlayer(), event.getBlock(), "protect-place");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onBucketEmpty(@NotNull PlayerBucketEmptyEvent event) {
        guard(event, event.getPlayer(), event.getBlock(), "protect-place");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onBucketFill(@NotNull PlayerBucketFillEvent event) {
        guard(event, event.getPlayer(), event.getBlock(), "protect-break");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onPhysicalInteract(@NotNull PlayerInteractEvent event) {
        if (event.getAction() != org.bukkit.event.block.Action.PHYSICAL) return;
        Block block = event.getClickedBlock();
        if (block == null) return;
        guard(event, event.getPlayer(), block, "protect-interact");
    }

    private void guard(@NotNull Cancellable event,
                       @NotNull Player player,
                       @NotNull Block block,
                       @NotNull String messageKey) {
        RegionData region = regionAt(block.getLocation());
        if (region == null) return;
        if (isBypass(player)) return;
        if (isMember(region, player.getUniqueId())) return;
        if (allowedByFlag(region, RegionFlag.ACCESS)) return;
        event.setCancelled(true);
        notify(player, messageKey);
    }
}
