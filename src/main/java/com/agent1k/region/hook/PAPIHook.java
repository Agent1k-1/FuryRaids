package com.agent1k.region.hook;

import com.agent1k.FuryRaids;
import com.agent1k.region.data.RegionData;
import com.agent1k.region.data.RegionType;
import com.agent1k.api.flag.RegionFlag;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Locale;
import java.util.List;

public final class PAPIHook extends PlaceholderExpansion {

    private final FuryRaids plugin;
    private final ThreadLocal<RegionData> regionContext = new ThreadLocal<>();

    public PAPIHook(@NotNull FuryRaids plugin) {
        this.plugin = plugin;
    }

    @Override
    @NotNull public String getIdentifier() { return "region"; }

    @Override
    @NotNull public String getAuthor() { return "agent1k"; }

    @Override
    @NotNull public String getVersion() { return plugin.getDescription().getVersion(); }

    @Override
    public boolean persist() { return true; }

    @Override
    public boolean canRegister() { return true; }

    public List<String> apply(@NotNull RegionData region, @NotNull List<String> lines) {
        RegionData previous = regionContext.get();
        regionContext.set(region);
        try {
            return PlaceholderAPI.setPlaceholders(Bukkit.getOfflinePlayer(region.getOwner()), lines);
        } finally {
            if (previous == null) regionContext.remove();
            else regionContext.set(previous);
        }
    }

    @Override
    @Nullable
    public String onRequest(@Nullable OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";
        String key = params.toLowerCase(Locale.ROOT);
        if (key.startsWith("flag_")) {
            RegionFlag flag = RegionFlag.from(key.substring(5).replace('_', '-'));
            return flag == null ? null : currentFlag(player, flag);
        }
        return switch (key) {
            case "id" -> currentId(player);
            case "owner" -> currentOwner(player);
            case "type" -> currentTypeName(player);
            case "radius" -> currentRadius(player);
            case "x_y_z" -> currentCoordinates(player);
            case "members" -> String.valueOf(currentMembers(player));
            case "durability" -> durability(player, false);
            case "max_durability" -> durability(player, true);
            case "durability_percent" -> durabilityPercent(player);
            case "owned_count" -> String.valueOf(plugin.getRegionManager().byOwner(player.getUniqueId()).size());
            default -> null;
        };
    }

    @NotNull
    private String currentId(@NotNull OfflinePlayer player) {
        RegionData r = regionAt(player);
        return r != null ? r.getId() : "";
    }

    @NotNull
    private String currentOwner(@NotNull OfflinePlayer player) {
        RegionData r = regionAt(player);
        if (r == null) return "";
        OfflinePlayer op = Bukkit.getOfflinePlayer(r.getOwner());
        return op.getName() != null ? op.getName() : r.getOwner().toString();
    }

    @NotNull
    private String currentTypeName(@NotNull OfflinePlayer player) {
        RegionData r = regionAt(player);
        if (r == null) return "";
        RegionType t = plugin.getBlocks().byId(r.getTypeId());
        return t != null ? t.displayName() : String.valueOf(r.getTypeId());
    }

    @NotNull
    private String currentRadius(@NotNull OfflinePlayer player) {
        RegionData r = regionAt(player);
        return r != null ? String.valueOf(r.getRadius()) : "";
    }

    @NotNull
    private String currentCoordinates(@NotNull OfflinePlayer player) {
        RegionData r = regionAt(player);
        return r != null ? r.getX() + ", " + r.getY() + ", " + r.getZ() : "";
    }

    private int currentMembers(@NotNull OfflinePlayer player) {
        RegionData r = regionAt(player);
        return r != null ? r.getMembers().size() : 0;
    }

    @NotNull
    private String currentFlag(@NotNull OfflinePlayer player, @NotNull RegionFlag flag) {
        RegionData r = regionAt(player);
        if (r == null) return "";
        String key = "flag-value-" + (r.isFlagEnabled(flag) ? "on" : "off");
        return plugin.getMessages().getText(key);
    }

    @Nullable
    private RegionData regionAt(@NotNull OfflinePlayer player) {
        RegionData context = regionContext.get();
        if (context != null) return context;
        if (!(player instanceof Player online)) return null;
        return plugin.getRegionManager().at(online.getLocation());
    }

    private String durability(OfflinePlayer player, boolean maximum) {
        RegionData region = regionAt(player);
        return region == null ? "" : String.valueOf(maximum ? region.getMaxDurability() : region.getDurability());
    }

    private String durabilityPercent(OfflinePlayer player) {
        RegionData region = regionAt(player);
        if (region == null) return "";
        return String.valueOf(region.getMaxDurability() == 0 ? 100
                : region.getDurability() * 100L / region.getMaxDurability());
    }
}
