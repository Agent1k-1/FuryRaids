package com.agent1k.tnt.listener;

import com.agent1k.FuryRaids;
import com.agent1k.tnt.ability.AbilityEntry;
import com.agent1k.tnt.TntType;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ExplosionListener implements Listener {

    private final FuryRaids plugin;

    public ExplosionListener(@NotNull FuryRaids plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPrime(@NotNull ExplosionPrimeEvent event) {
        if (!(event.getEntity() instanceof TNTPrimed tnt)) return;
        TntType type = resolveType(tnt);
        if (type == null) return;
        event.setRadius(type.radius());
        event.setFire(plugin.getTntConfig().incendiary());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onExplode(@NotNull EntityExplodeEvent event) {
        if (!(event.getEntity() instanceof TNTPrimed tnt)) return;
        TntType type = resolveType(tnt);
        if (type == null) return;
        for (AbilityEntry ability : type.abilities()) {
            ability.apply(event);
        }
    }

    @Nullable
    private TntType resolveType(@NotNull TNTPrimed tnt) {
        String typeId = plugin.getTntItems().readTypeId(tnt);
        if (typeId == null) return null;
        return plugin.getTntRegistry().byId(typeId);
    }
}
