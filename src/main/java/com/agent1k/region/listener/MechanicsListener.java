package com.agent1k.region.listener;

import com.agent1k.FuryRaids;
import com.agent1k.region.data.RegionData;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;


public final class MechanicsListener implements Listener {
    private final Function<Location, RegionData> lookup;

    public MechanicsListener(FuryRaids plugin) { this(plugin.getRegionManager()::at); }

    MechanicsListener(Function<Location, RegionData> lookup) { this.lookup = lookup; }

    private RegionData regionAt(Location location) { return lookup.apply(location); }

    public static boolean crossesBoundary(RegionData from, RegionData to) {
        return !Objects.equals(from == null ? null : from.getId(), to == null ? null : to.getId());
    }

    public static boolean isCenter(RegionData region, Block block) {
        return region != null && region.getWorld().equals(block.getWorld().getName())
                && region.getX() == block.getX() && region.getY() == block.getY() && region.getZ() == block.getZ();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onExtend(BlockPistonExtendEvent event) {
        if (blockedPiston(event.getBlock(), event.getDirection(), event.getBlocks(), true)) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onRetract(BlockPistonRetractEvent event) {

        BlockFace movement = ((Directional) event.getBlock().getBlockData()).getFacing().getOppositeFace();
        if (blockedPiston(event.getBlock(), movement, event.getBlocks(), false)) event.setCancelled(true);
    }

    private boolean blockedPiston(Block piston, BlockFace movement, List<Block> blocks, boolean extending) {
        RegionData origin = regionAt(piston.getLocation());
        if (extending && crossesBoundary(origin, regionAt(piston.getRelative(movement).getLocation()))) return true;
        for (Block block : blocks) {
            RegionData source = regionAt(block.getLocation());
            Block destination = block.getRelative(movement);
            RegionData target = regionAt(destination.getLocation());

            if (isCenter(source, block) || isCenter(target, destination)) return true;
            if (crossesBoundary(origin, source) || crossesBoundary(source, target)) return true;
        }
        return false;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onTransfer(InventoryMoveItemEvent event) {
        List<Location> sources = locations(event.getSource());
        List<Location> targets = locations(event.getDestination());

        for (Location from : sources) {
            for (Location to : targets) {
                if (crossesBoundary(regionAt(from), regionAt(to))) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    private List<Location> locations(Inventory inventory) {
        List<Location> locations = new ArrayList<>();
        InventoryHolder holder = inventory.getHolder();
        if (holder instanceof DoubleChest chest) {
            addLocation(locations, chest.getLeftSide());
            addLocation(locations, chest.getRightSide());
        } else if (inventory.getLocation() != null) {
            locations.add(inventory.getLocation());
        }
        return locations;
    }

    private void addLocation(List<Location> locations, InventoryHolder holder) {
        if (holder != null && holder.getInventory().getLocation() != null) {
            locations.add(holder.getInventory().getLocation());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onFlow(BlockFromToEvent event) {
        RegionData target = regionAt(event.getToBlock().getLocation());
        if (target != null && crossesBoundary(regionAt(event.getBlock().getLocation()), target)) event.setCancelled(true);
    }
}
