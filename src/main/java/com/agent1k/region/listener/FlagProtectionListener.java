package com.agent1k.region.listener;

import com.agent1k.FuryRaids;
import com.agent1k.api.flag.RegionFlag;
import com.agent1k.region.data.RegionData;
import com.sk89q.worldguard.bukkit.event.block.UseBlockEvent;
import com.sk89q.worldguard.bukkit.event.entity.DamageEntityEvent;
import com.sk89q.worldguard.bukkit.util.Entities;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class FlagProtectionListener implements Listener {
    private final Function<Location, RegionData> lookup;
    private final BiConsumer<Player, String> notify;

    public FlagProtectionListener(FuryRaids plugin) {
        this(plugin.getRegionManager()::at, (player, key) -> plugin.getMessages().send(player, key));
    }

    FlagProtectionListener(Function<Location, RegionData> lookup, BiConsumer<Player, String> notify) {
        this.lookup = lookup;
        this.notify = notify;
    }

    public static RegionFlag controlFlag(Material material) {
        String name = material.name();
        if (name.endsWith("_DOOR") || name.endsWith("_TRAPDOOR") || name.endsWith("_FENCE_GATE")) return RegionFlag.DOORS;
        if (name.endsWith("_BUTTON")) return RegionFlag.BUTTONS;
        if (material == Material.LEVER) return RegionFlag.LEVERS;
        return null;
    }

    private static boolean isUseBlock(Material material) {
        if (controlFlag(material) != null || material.name().endsWith("_PRESSURE_PLATE")) return true;
        return switch (material) {
            case LECTERN, ENCHANTING_TABLE, BELL, LOOM, CARTOGRAPHY_TABLE, STONECUTTER, GRINDSTONE, VAULT -> true;
            default -> false;
        };
    }

    public static boolean allowed(RegionData region, UUID actor, boolean bypass, RegionFlag flag) {
        return region == null || bypass || (actor != null && region.isOwnerOrMember(actor))
                || region.isFlagEnabled(flag);
    }

    public static boolean pvpAllowed(RegionData attacker, RegionData victim) {
        return (attacker == null || attacker.isFlagEnabled(RegionFlag.PVP))
                && (victim == null || victim.isFlagEnabled(RegionFlag.PVP));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onUse(UseBlockEvent event) {
        Player player = event.getCause().getFirstPlayer();
        UUID actor = player == null ? null : player.getUniqueId();
        boolean bypass = player != null && player.hasPermission("eregionplus.admin");
        for (Block block : event.getBlocks()) {
            Material material = block.getType();
            if (!isUseBlock(material)) continue;
            RegionFlag flag = controlFlag(material);
            if (flag == null) flag = RegionFlag.ACCESS;
            RegionData target = lookup.apply(block.getLocation());
            boolean localAutomation = false;
            if (player == null && target != null) {
                Block sourceBlock = event.getCause().getFirstBlock();
                var sourceEntity = event.getCause().getFirstEntity();
                Location source = sourceBlock != null ? sourceBlock.getLocation()
                        : sourceEntity != null ? sourceEntity.getLocation() : null;
                RegionData sourceRegion = source == null ? null : lookup.apply(source);
                localAutomation = sourceRegion != null && sourceRegion.getId().equals(target.getId());
            }
            if (allowed(target, actor, bypass || localAutomation, flag)) continue;
            event.setCancelled(true);
            if (player != null) notify.accept(player, "protect-interact");
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(DamageEntityEvent event) {
        Player attacker = event.getCause().getFirstPlayer();
        if (attacker == null) return;
        if (event.getEntity() instanceof Player victim) {
            if (attacker.getUniqueId().equals(victim.getUniqueId())) return;
            if (pvpAllowed(lookup.apply(attacker.getLocation()), lookup.apply(victim.getLocation()))) return;
            event.setCancelled(true);
            notify.accept(attacker, "protect-pvp");
        } else if (Entities.isNonHostile(event.getEntity())) {
            if (allowed(lookup.apply(event.getEntity().getLocation()), attacker.getUniqueId(),
                    attacker.hasPermission("eregionplus.admin"), RegionFlag.DAMAGE_ANIMALS)) return;
            event.setCancelled(true);
            notify.accept(attacker, "protect-animals");
        }
    }
}
