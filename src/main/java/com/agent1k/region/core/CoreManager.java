package com.agent1k.region.core;

import com.agent1k.FuryRaids;
import com.agent1k.api.event.RegionDeleteEvent;
import com.agent1k.region.data.RegionData;
import com.agent1k.region.listener.MechanicsListener;
import com.agent1k.tnt.TntType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CoreManager implements Listener {
    private final FuryRaids plugin;
    private final Map<String, Integer> pendingDamage = new HashMap<>();

    public CoreManager(FuryRaids plugin) { this.plugin = plugin; }

    public long remainingCooldown(RegionData region) {
        if (pendingDamage.containsKey(region.getId())) return 1;
        long delay = Math.max(0, plugin.getBlocks().raw().getInt("repair.cooldown-after-damage", 60)) * 1000L;
        if (region.getLastDamageTime() <= 0) return 0;
        long remaining = delay - Math.max(0L, System.currentTimeMillis() - region.getLastDamageTime());
        return remaining <= 0 ? 0 : (remaining + 999L) / 1000L;
    }

    private Material repairMaterial() {
        Material material = Material.matchMaterial(plugin.getBlocks().raw().getString("repair.material", "DIAMOND"));
        return material != null && material.isItem() && !material.isAir() ? material : Material.DIAMOND;
    }

    public boolean isRepairClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND) return false;
        ItemStack item = event.getItem();
        Block block = event.getClickedBlock();
        if (item == null || item.getType() != repairMaterial() || block == null) return false;
        RegionData region = plugin.getRegionManager().at(block.getLocation());
        return MechanicsListener.isCenter(region, block);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onRepair(PlayerInteractEvent event) {
        if (!isRepairClick(event)) return;
        event.setCancelled(true);
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        RegionData region = plugin.getRegionManager().at(block.getLocation());
        if (!region.getOwner().equals(player.getUniqueId())) {
            plugin.getMessages().send(player, "region-no-owner");
            return;
        }
        if (!plugin.getWorldGuardHook().canBuild(player, block.getLocation())) {
            plugin.getMessages().send(player, "protect-interact");
            return;
        }
        if (region.getMaxDurability() <= 0 || region.getDurability() >= region.getMaxDurability()) {
            plugin.getMessages().send(player, "core-repair-full");
            return;
        }
        long cooldown = remainingCooldown(region);
        if (cooldown > 0) {
            plugin.getMessages().send(player, "core-repair-cooldown", Map.of("seconds", String.valueOf(cooldown)));
            return;
        }
        ItemStack held = player.getInventory().getItemInMainHand();
        if (held.getType() != repairMaterial() || held.getAmount() < 1) return;
        int previous = region.getDurability();
        int restore = Math.max(1, plugin.getBlocks().raw().getInt("repair.restore", 3));
        int next = (int) Math.min(region.getMaxDurability(), (long) previous + restore);
        region.setDurability(next);
        if (!plugin.getRegionManager().persist(region)) {
            region.setDurability(previous);
            plugin.getMessages().send(player, "error-region-storage");
            return;
        }
        held.setAmount(held.getAmount() - 1);
        player.getInventory().setItemInMainHand(held);
        plugin.getMessages().send(player, "core-repaired", Map.of("amount", String.valueOf(next - previous),
                "durability", String.valueOf(next), "maximum", String.valueOf(region.getMaxDurability())));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onExplosion(EntityExplodeEvent event) {
        if (!(event.getEntity() instanceof TNTPrimed) && !(event.getEntity() instanceof ExplosiveMinecart)) return;
        String id = plugin.getTntItems().readTypeId(event.getEntity());
        TntType type = id == null ? null : plugin.getTntRegistry().byId(id);
        int damage = type == null ? plugin.getTntConfig().vanillaRegionDamage() : type.regionDamage();
        if (damage <= 0 || !plugin.getWorldGuardHook().canExplode(event.getLocation())) return;
        double radius = type == null ? 4.0 : Math.min(4.0, type.radius());
        double distanceSquared = radius * radius;
        World world = event.getLocation().getWorld();
        if (world == null) return;
        List<Hit> hits = new ArrayList<>();
        for (RegionData region : plugin.getRegionManager().all()) {
            if (!region.getWorld().equals(world.getName()) || region.getMaxDurability() <= 0
                    || !world.isChunkLoaded(region.getX() >> 4, region.getZ() >> 4)) continue;
            Location center = new Location(world, region.getX() + 0.5, region.getY() + 0.5, region.getZ() + 0.5);
            if (event.getLocation().distanceSquared(center) > distanceSquared
                    || !plugin.getWorldGuardHook().canExplode(center)) continue;
            Block block = world.getBlockAt(region.getX(), region.getY(), region.getZ());
            if (!block.getType().isAir()) hits.add(new Hit(region, block, block.getType()));
        }
        if (hits.isEmpty()) return;
        for (Hit hit : hits) pendingDamage.merge(hit.region().getId(), 1, Integer::sum);
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                if (event.isCancelled()) return;
                for (Hit hit : hits) {
                    if (plugin.getRegionManager().byId(hit.region().getId()) == hit.region()
                            && hit.block().getWorld().isChunkLoaded(hit.region().getX() >> 4, hit.region().getZ() >> 4)
                            && hit.block().getType() == hit.material()) damage(hit.region(), hit.block(), damage);
                }
            } finally {
                for (Hit hit : hits) pendingDamage.computeIfPresent(hit.region().getId(),
                        (idKey, count) -> count <= 1 ? null : count - 1);
            }
        });
    }

    private void damage(RegionData region, Block block, int amount) {
        int previous = region.getDurability();
        long previousTime = region.getLastDamageTime();
        region.setDurability((int) Math.max(0L, (long) previous - amount));
        region.setLastDamageTime(System.currentTimeMillis());
        if (region.getDurability() == 0) {
            if (!plugin.getRegionManager().unregister(region.getId())) {
                region.setDurability(previous);
                region.setLastDamageTime(previousTime);
                notifyOwner(region, "error-region-storage", Map.of());
                return;
            }
            block.setType(Material.AIR, false);
            Bukkit.getPluginManager().callEvent(new RegionDeleteEvent(region));
            notifyOwner(region, "core-destroyed", Map.of());
        } else if (!plugin.getRegionManager().persist(region)) {
            region.setDurability(previous);
            region.setLastDamageTime(previousTime);
            notifyOwner(region, "error-region-storage", Map.of());
        } else {
            notifyOwner(region, "core-damaged", Map.of("damage", String.valueOf(previous - region.getDurability()),
                    "durability", String.valueOf(region.getDurability()), "maximum", String.valueOf(region.getMaxDurability())));
        }
    }

    private void notifyOwner(RegionData region, String key, Map<String, String> placeholders) {
        Player owner = Bukkit.getPlayer(region.getOwner());
        if (owner != null) plugin.getMessages().send(owner, key, placeholders);
    }

    private record Hit(RegionData region, Block block, Material material) { }
}
