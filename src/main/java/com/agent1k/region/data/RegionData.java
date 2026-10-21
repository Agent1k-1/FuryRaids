package com.agent1k.region.data;

import com.agent1k.api.flag.RegionFlag;
import com.agent1k.api.model.Region;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RegionData implements Region {

    private final String id;
    private final UUID owner;
    private final String world;
    private final int x;
    private final int y;
    private final int z;
    private final int radius;
    private final int typeId;
    private final Set<UUID> members;
    private final EnumMap<RegionFlag, Boolean> flags;
    private int durability;
    private int maxDurability;
    private long lastDamageTime;

    public RegionData(@NotNull String id,
                      @NotNull UUID owner,
                      @NotNull String world,
                      int x, int y, int z,
                      int radius,
                      int typeId,
                      @NotNull Set<UUID> members,
                      @NotNull EnumMap<RegionFlag, Boolean> flags) {
        this(id, owner, world, x, y, z, radius, typeId, members, flags, -1, -1, 0L);
    }

    public RegionData(String id, UUID owner, String world, int x, int y, int z, int radius,
                      int typeId, Set<UUID> members, EnumMap<RegionFlag, Boolean> flags,
                      int durability, int maxDurability, long lastDamageTime) {
        this.id = id;
        this.owner = owner;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
        this.typeId = typeId;
        this.members = Collections.newSetFromMap(new ConcurrentHashMap<>());
        this.members.addAll(members);
        this.flags = new EnumMap<>(RegionFlag.class);
        this.flags.putAll(flags);
        this.maxDurability = maxDurability;
        this.durability = maxDurability < 0 ? -1 : Math.max(0, Math.min(durability, maxDurability));
        this.lastDamageTime = Math.max(0L, lastDamageTime);
    }

    @Override @NotNull public String getId() { return id; }
    @Override @NotNull public UUID getOwner() { return owner; }
    @Override @NotNull public String getWorld() { return world; }
    @Override public int getX() { return x; }
    @Override public int getY() { return y; }
    @Override public int getZ() { return z; }
    @Override public int getRadius() { return radius; }
    @Override public int getTypeId() { return typeId; }
    @Override public int getDurability() { return durability; }
    @Override public int getMaxDurability() { return maxDurability; }
    @Override public long getLastDamageTime() { return lastDamageTime; }

    public void initializeDurability(int maximum) {
        if (maxDurability >= 0) return;
        maxDurability = Math.max(0, maximum);
        durability = maxDurability;
    }

    public void setDurability(int value) { durability = Math.max(0, Math.min(value, maxDurability)); }
    public void setLastDamageTime(long value) { lastDamageTime = Math.max(0L, value); }

    @Override
    @NotNull
    public Set<UUID> getMembers() {
        return Collections.unmodifiableSet(members);
    }

    public boolean addMember(@NotNull UUID uuid) {
        if (uuid.equals(owner)) return false;
        return members.add(uuid);
    }

    public boolean removeMember(@NotNull UUID uuid) {
        return members.remove(uuid);
    }

    @Override
    public boolean isMember(@NotNull UUID uuid) {
        return members.contains(uuid);
    }

    @Override
    public boolean isOwnerOrMember(@NotNull UUID uuid) {
        return owner.equals(uuid) || members.contains(uuid);
    }

    @Override
    public boolean isFlagEnabled(@NotNull RegionFlag flag) {
        return flags.getOrDefault(flag, flag.getDefaultValue());
    }

    public void setFlag(@NotNull RegionFlag flag, boolean value) {
        flags.put(flag, value);
    }

    @NotNull
    public EnumMap<RegionFlag, Boolean> getFlags() {
        return new EnumMap<>(flags);
    }

    @Override
    public boolean contains(@NotNull Location loc) {
        World w = loc.getWorld();
        if (w == null || !w.getName().equals(world)) return false;
        long dx = Math.abs((long) loc.getBlockX() - x);
        long dy = Math.abs((long) loc.getBlockY() - y);
        long dz = Math.abs((long) loc.getBlockZ() - z);
        return dx <= radius && dy <= radius && dz <= radius;
    }

    public boolean overlaps(@NotNull String otherWorld, int ox, int oz, int otherRadius, int gap) {
        if (!world.equals(otherWorld)) return false;
        int dx = Math.abs(x - ox);
        int dz = Math.abs(z - oz);
        int sum = radius + otherRadius + gap;
        return dx <= sum && dz <= sum;
    }

    public boolean overlaps(@NotNull String otherWorld, int ox, int oy, int oz, int otherRadius, int gap) {
        if (!world.equals(otherWorld)) return false;
        long sum = (long) radius + otherRadius + gap;
        return Math.abs((long) x - ox) <= sum
                && Math.abs((long) y - oy) <= sum
                && Math.abs((long) z - oz) <= sum;
    }

    @NotNull
    public Set<UUID> membersSnapshot() {
        return new HashSet<>(members);
    }
}
