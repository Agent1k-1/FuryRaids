package com.agent1k.tnt.hook;

import com.agent1k.FuryRaids;
import com.agent1k.tnt.TntType;
import com.agent1k.tnt.util.ColorUtil;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public final class PAPIHook extends PlaceholderExpansion {

    private final FuryRaids plugin;

    public PAPIHook(@NotNull FuryRaids plugin) {
        this.plugin = plugin;
    }

    @Override
    @NotNull public String getIdentifier() { return "tnt"; }

    @Override
    @NotNull public String getAuthor() { return "agent1k"; }

    @Override
    @NotNull public String getVersion() { return plugin.getDescription().getVersion(); }

    @Override
    public boolean persist() { return true; }

    @Override
    @Nullable
    public String onRequest(@Nullable OfflinePlayer player, @NotNull String params) {
        String lower = params.toLowerCase(Locale.ROOT);
        if (lower.equals("types_count")) return String.valueOf(plugin.getTntRegistry().all().size());
        if (lower.startsWith("type_name_")) {
            TntType type = plugin.getTntRegistry().byId(lower.substring("type_name_".length()));
            return type != null ? ColorUtil.apply(type.name()) : "";
        }
        if (lower.startsWith("type_radius_")) {
            TntType type = plugin.getTntRegistry().byId(lower.substring("type_radius_".length()));
            return type != null ? String.valueOf(type.radius()) : "";
        }
        if (lower.startsWith("type_damage_")) {
            TntType type = plugin.getTntRegistry().byId(lower.substring("type_damage_".length()));
            return type == null ? "" : String.valueOf(type.regionDamage());
        }
        return null;
    }
}
