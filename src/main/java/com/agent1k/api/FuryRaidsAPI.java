package com.agent1k.api;

import com.agent1k.api.flag.RegionFlag;
import com.agent1k.api.model.Region;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface FuryRaidsAPI {

    @NotNull Optional<Region> getRegion(@NotNull String id);

    @NotNull Optional<Region> getRegionAt(@NotNull Location location);

    @NotNull Collection<Region> getRegionsByOwner(@NotNull UUID owner);

    @NotNull Collection<Region> getAllRegions();

    boolean addMember(@NotNull String regionId, @NotNull UUID member);

    boolean removeMember(@NotNull String regionId, @NotNull UUID member);

    boolean setFlag(@NotNull String regionId, @NotNull RegionFlag flag, boolean value);

    boolean deleteRegion(@NotNull String regionId);

    boolean canAccess(@NotNull Player player, @NotNull Location location);

    boolean canOpenChest(@NotNull Player player, @NotNull Location location);
}
