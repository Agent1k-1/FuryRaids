package com.agent1k.tnt.listener;

import com.agent1k.FuryRaids;
import com.agent1k.tnt.TntType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public final class InteractListener implements Listener {

    private final FuryRaids plugin;

    public InteractListener(@NotNull FuryRaids plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(@NotNull PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        EquipmentSlot hand = event.getHand();
        if (hand != EquipmentSlot.HAND && hand != EquipmentSlot.OFF_HAND) return;
        ItemStack item = event.getItem();
        if (item == null || item.getType() == Material.AIR) return;

        String typeId = plugin.getTntItems().readTypeId(item);
        if (typeId == null) return;

        TntType type = plugin.getTntRegistry().byId(typeId);
        if (type == null) return;
        if (!type.autofire()) return;

        Block clicked = event.getClickedBlock();
        if (clicked == null) return;

        event.setCancelled(true);

        Player player = event.getPlayer();
        Location spawn = clicked.getRelative(event.getBlockFace()).getLocation().add(0.5, 0, 0.5);
        TNTPrimed tnt = (TNTPrimed) player.getWorld().spawnEntity(spawn, EntityType.TNT);
        tnt.setFuseTicks(plugin.getTntConfig().defaultFuseTicks());
        tnt.setSource(player);
        plugin.getTntItems().writeTypeId(tnt, typeId);

        item.setAmount(item.getAmount() - 1);
        plugin.getTntMessages().send(player, "armed", Map.of("radius", String.valueOf(type.radius())));
    }
}
