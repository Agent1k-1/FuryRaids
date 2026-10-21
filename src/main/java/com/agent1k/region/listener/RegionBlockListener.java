package com.agent1k.region.listener;

import com.agent1k.FuryRaids;
import com.agent1k.api.event.RegionCreateEvent;
import com.agent1k.api.event.RegionDeleteEvent;
import com.agent1k.api.flag.RegionFlag;
import com.agent1k.region.data.RegionData;
import com.agent1k.region.data.RegionType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.UUID;
import java.util.IdentityHashMap;
import java.util.Map;

public final class RegionBlockListener extends AbstractRegionListener {
    private final Map<BlockPlaceEvent, RegionData> placements = new IdentityHashMap<>();
    private final Map<BlockBreakEvent, RegionData> removals = new IdentityHashMap<>();

    public RegionBlockListener(@NotNull FuryRaids plugin) {
        super(plugin);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlace(@NotNull BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        Integer typeId = plugin.getItems().readTypeId(item);
        if (typeId == null) return;

        Player player = event.getPlayer();
        RegionType type = plugin.getBlocks().byId(typeId);
        if (type == null) {
            event.setCancelled(true);
            notify(player, "unknown-type");
            return;
        }
        Block block = event.getBlock();
        String world = block.getWorld().getName();
        if (plugin.getBlocks().isBlockedWorld(world)) {
            event.setCancelled(true);
            notify(player, "region-blocked-world");
            return;
        }
        UUID owner = player.getUniqueId();
        int gap = plugin.getConfig().getInt("region.min-distance-between");
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();
        if (plugin.getRegionManager().overlaps(world, x, y, z, type.radius(), gap)) {
            event.setCancelled(true);
            notify(player, "region-overlap");
            return;
        }
        int maximum = plugin.getItems().readMaxDurability(item, type.durability());
        RegionData region = new RegionData(
                UUID.randomUUID().toString(),
                owner,
                world,
                x, y, z,
                type.radius(),
                typeId,
                new HashSet<>(),
                RegionFlag.defaults(), plugin.getItems().readDurability(item, maximum),
                maximum, plugin.getItems().readLastDamage(item)
        );
        if (maximum > 0 && region.getDurability() == 0) {
            event.setCancelled(true);
            notify(player, "core-invalid");
            return;
        }
        RegionCreateEvent createEvent = new RegionCreateEvent(region);
        try {
            if (!plugin.getWorldGuardHook().canCreate(player, region)) {
                event.setCancelled(true);
                notify(player, "region-conflict");
                return;
            }
        } catch (RuntimeException e) {
            event.setCancelled(true);
            notify(player, "error-region-storage");
            plugin.getLogger().severe(e.getMessage());
            return;
        }
        Bukkit.getPluginManager().callEvent(createEvent);
        if (createEvent.isCancelled()) {
            event.setCancelled(true);
            return;
        }
        if (!plugin.getRegionManager().register(region)) {
            event.setCancelled(true);
            notify(player, "error-region-storage");
            return;
        }
        placements.put(event, region);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void finishPlace(BlockPlaceEvent event) {
        RegionData region = placements.remove(event);
        if (region == null) return;
        if (event.isCancelled()) {
            plugin.getRegionManager().unregister(region.getId());
            return;
        }
        plugin.getMessages().send(event.getPlayer(), "region-placed",
                Map.of("radius", String.valueOf(region.getRadius())));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBreak(@NotNull BlockBreakEvent event) {
        Block block = event.getBlock();
        Location loc = block.getLocation();
        RegionData region = regionAt(loc);
        if (region == null) return;
        if (!isCenter(region, block)) return;

        Player player = event.getPlayer();
        if (!region.getOwner().equals(player.getUniqueId()) && !isBypass(player)) {
            event.setCancelled(true);
            notify(player, "protect-break");
            return;
        }
        long seconds = plugin.getCores().remainingCooldown(region);
        if (seconds > 0) {
            event.setCancelled(true);
            plugin.getMessages().send(player, "core-raid-locked", Map.of("seconds", String.valueOf(seconds)));
            return;
        }
        if (!plugin.getRegionManager().unregister(region.getId())) {
            event.setCancelled(true);
            notify(player, "error-region-storage");
            return;
        }
        event.setDropItems(false);
        removals.put(event, region);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void finishBreak(BlockBreakEvent event) {
        RegionData region = removals.remove(event);
        if (region == null) return;
        if (event.isCancelled()) {
            plugin.getRegionManager().register(region);
            return;
        }
        Bukkit.getPluginManager().callEvent(new RegionDeleteEvent(region));
        dropRegionItem(region, event.getBlock());
        notify(event.getPlayer(), "region-broken");
    }

    private void dropRegionItem(@NotNull RegionData region, @NotNull Block block) {
        RegionType type = plugin.getBlocks().byId(region.getTypeId());
        if (type == null) return;
        block.getWorld().dropItemNaturally(
                block.getLocation().add(0.5, 0.5, 0.5),
                plugin.getItems().build(type, region)
        );
    }

    private boolean isCenter(@NotNull RegionData region, @NotNull Block block) {
        return block.getX() == region.getX()
                && block.getY() == region.getY()
                && block.getZ() == region.getZ()
                && block.getWorld().getName().equals(region.getWorld());
    }
}
