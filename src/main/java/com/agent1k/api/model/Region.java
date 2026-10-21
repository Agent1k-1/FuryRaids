package com.agent1k.api.model;

import com.agent1k.api.flag.RegionFlag;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

public interface Region {

    @NotNull String getId();

    @NotNull UUID getOwner();

    @NotNull String getWorld();

    int getX();

    int getY();

    int getZ();

    int getRadius();

    int getTypeId();

    int getDurability();

    int getMaxDurability();

    long getLastDamageTime();

    @NotNull Set<UUID> getMembers();

    boolean isMember(@NotNull UUID uuid);

    boolean isOwnerOrMember(@NotNull UUID uuid);

    boolean isFlagEnabled(@NotNull RegionFlag flag);

    boolean contains(@NotNull Location location);
}
