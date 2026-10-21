package com.agent1k.api.internal;

import com.agent1k.FuryRaids;
import com.agent1k.api.FuryRaidsAPI;
import com.agent1k.api.event.RegionDeleteEvent;
import com.agent1k.api.event.RegionFlagChangeEvent;
import com.agent1k.api.event.RegionMemberAddEvent;
import com.agent1k.api.event.RegionMemberRemoveEvent;
import com.agent1k.api.flag.RegionFlag;
import com.agent1k.api.model.Region;
import com.agent1k.region.data.RegionData;
import com.sk89q.worldguard.protection.flags.Flags;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

public final class APIImpl implements FuryRaidsAPI {

    private final FuryRaids plugin;

    public APIImpl(@NotNull FuryRaids plugin) {
        this.plugin = plugin;
    }

    @Override
    @NotNull
    public Optional<Region> getRegion(@NotNull String id) {
        return Optional.ofNullable(plugin.getRegionManager().byId(id));
    }

    @Override
    @NotNull
    public Optional<Region> getRegionAt(@NotNull Location location) {
        return Optional.ofNullable(plugin.getRegionManager().at(location));
    }

    @Override
    @NotNull
    public Collection<Region> getRegionsByOwner(@NotNull UUID owner) {
        return new ArrayList<>(plugin.getRegionManager().byOwner(owner));
    }

    @Override
    @NotNull
    public Collection<Region> getAllRegions() {
        return Collections.unmodifiableCollection(new ArrayList<>(plugin.getRegionManager().all()));
    }

    @Override
    public boolean addMember(@NotNull String regionId, @NotNull UUID member) {
        RegionData region = plugin.getRegionManager().byId(regionId);
        if (region == null) return false;
        RegionMemberAddEvent event = new RegionMemberAddEvent(region, member);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;
        if (!region.addMember(member)) return false;
        if (!plugin.getRegionManager().persist(region)) {
            region.removeMember(member);
            plugin.getRegionManager().persist(region);
            return false;
        }
        return true;
    }

    @Override
    public boolean removeMember(@NotNull String regionId, @NotNull UUID member) {
        RegionData region = plugin.getRegionManager().byId(regionId);
        if (region == null) return false;
        RegionMemberRemoveEvent event = new RegionMemberRemoveEvent(region, member);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;
        if (!region.removeMember(member)) return false;
        if (!plugin.getRegionManager().persist(region)) {
            region.addMember(member);
            plugin.getRegionManager().persist(region);
            return false;
        }
        return true;
    }

    @Override
    public boolean setFlag(@NotNull String regionId, @NotNull RegionFlag flag, boolean value) {
        RegionData region = plugin.getRegionManager().byId(regionId);
        if (region == null) return false;
        boolean current = region.isFlagEnabled(flag);
        if (current == value) return true;
        RegionFlagChangeEvent event = new RegionFlagChangeEvent(region, flag, current, value);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;
        region.setFlag(flag, value);
        if (!plugin.getRegionManager().persist(region)) {
            region.setFlag(flag, current);
            plugin.getRegionManager().persist(region);
            return false;
        }
        return true;
    }

    @Override
    public boolean deleteRegion(@NotNull String regionId) {
        RegionData region = plugin.getRegionManager().byId(regionId);
        if (region == null) return false;
        if (!plugin.getRegionManager().unregister(regionId)) return false;
        Bukkit.getPluginManager().callEvent(new RegionDeleteEvent(region));
        return true;
    }

    @Override
    public boolean canAccess(@NotNull Player player, @NotNull Location location) {
        if (!plugin.getWorldGuardHook().canBuild(player, location)) return false;
        RegionData region = plugin.getRegionManager().at(location);
        if (region == null) return true;
        if (player.hasPermission("eregionplus.admin")) return true;
        if (region.isOwnerOrMember(player.getUniqueId())) return true;
        return region.isFlagEnabled(RegionFlag.ACCESS);
    }

    @Override
    public boolean canOpenChest(@NotNull Player player, @NotNull Location location) {
        if (!plugin.getWorldGuardHook().canBuild(player, location, Flags.CHEST_ACCESS)) return false;
        RegionData region = plugin.getRegionManager().at(location);
        if (region == null) return true;
        if (player.hasPermission("eregionplus.admin")) return true;
        if (region.isOwnerOrMember(player.getUniqueId())) return true;
        return region.isFlagEnabled(RegionFlag.ACCESS) || region.isFlagEnabled(RegionFlag.CHEST);
    }
}
