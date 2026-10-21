package com.agent1k.region.hook;

import com.agent1k.FuryRaids;
import com.agent1k.api.flag.RegionFlag;
import com.agent1k.region.data.RegionData;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.RemovalStrategy;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public final class WorldGuardHook {

    public static final String PREFIX = "eregionplus_";
    private static final Pattern MANAGED_ID = Pattern.compile(
            PREFIX + "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");

    private final FuryRaids plugin;
    private final boolean present;

    public WorldGuardHook(@NotNull FuryRaids plugin) {
        this.plugin = plugin;
        Plugin wg = Bukkit.getPluginManager().getPlugin("WorldGuard");
        this.present = wg != null && wg.isEnabled();
    }

    public boolean isPresent() {
        return present;
    }

    public void verify() {
        if (!present) {
            plugin.getLogger().severe("WorldGuard не найден. Плагин будет отключён.");
            Bukkit.getPluginManager().disablePlugin(plugin);
        } else {
            plugin.getLogger().info("WorldGuard обнаружен: синхронизация регионов активна.");
        }
    }

    public static String regionId(RegionData region) { return PREFIX + region.getId(); }

    public static boolean isManagedId(String id) { return MANAGED_ID.matcher(id).matches(); }

    public static ProtectedCuboidRegion buildRegion(RegionData data, World world) {
        return new ProtectedCuboidRegion(regionId(data),
                BlockVector3.at(data.getX() - data.getRadius(), Math.max(world.getMinHeight(), data.getY() - data.getRadius()), data.getZ() - data.getRadius()),
                BlockVector3.at(data.getX() + data.getRadius(), Math.min(world.getMaxHeight() - 1, data.getY() + data.getRadius()), data.getZ() + data.getRadius()));
    }

    public static void applyData(ProtectedRegion target, RegionData data) {
        DefaultDomain owners = new DefaultDomain();
        owners.addPlayer(data.getOwner());
        target.setOwners(owners);
        DefaultDomain members = new DefaultDomain();
        data.getMembers().forEach(members::addPlayer);
        target.setMembers(members);
        target.setFlag(Flags.PASSTHROUGH, data.isFlagEnabled(RegionFlag.ACCESS) ? StateFlag.State.ALLOW : null);
        target.setFlag(Flags.CHEST_ACCESS, data.isFlagEnabled(RegionFlag.CHEST) ? StateFlag.State.ALLOW : null);
        target.setFlag(Flags.TNT, state(data.getMaxDurability() > 0 || data.isFlagEnabled(RegionFlag.TNT)));
        target.setFlag(Flags.PVP, state(data.isFlagEnabled(RegionFlag.PVP)));
        target.setFlag(Flags.PVP.getRegionGroupFlag(), RegionGroup.ALL);
        target.setFlag(Flags.DAMAGE_ANIMALS, state(data.isFlagEnabled(RegionFlag.DAMAGE_ANIMALS)));
        target.setFlag(Flags.DAMAGE_ANIMALS.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);
        boolean publicControls = data.isFlagEnabled(RegionFlag.DOORS)
                || data.isFlagEnabled(RegionFlag.BUTTONS) || data.isFlagEnabled(RegionFlag.LEVERS);
        target.setFlag(Flags.USE, publicControls ? StateFlag.State.ALLOW : null);
        target.setFlag(Flags.USE.getRegionGroupFlag(), RegionGroup.ALL);
        StateFlag.State explosion = state(data.isFlagEnabled(RegionFlag.EXPLOSION));
        target.setFlag(Flags.CREEPER_EXPLOSION, explosion);
        target.setFlag(Flags.OTHER_EXPLOSION, explosion);
        target.setFlag(Flags.GHAST_FIREBALL, explosion);
        target.setFlag(Flags.WITHER_DAMAGE, explosion);
        target.setFlag(Flags.ENDERDRAGON_BLOCK_DAMAGE, explosion);
        target.setFlag(Flags.ENDER_BUILD, StateFlag.State.DENY);
        target.setFlag(Flags.RAVAGER_RAVAGE, StateFlag.State.DENY);
        target.setFlag(Flags.FIRE_SPREAD, StateFlag.State.DENY);
        target.setFlag(Flags.LAVA_FIRE, StateFlag.State.DENY);
    }

    private static StateFlag.State state(boolean enabled) {
        return enabled ? StateFlag.State.ALLOW : StateFlag.State.DENY;
    }

    private RegionManager manager(World world) {
        RegionManager manager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
        if (manager == null) throw new IllegalStateException("WorldGuard недоступен в мире " + world.getName());
        return manager;
    }

    public boolean canCreate(Player player, RegionData data) {
        World world = player.getWorld();
        RegionManager manager = manager(world);
        ProtectedRegion candidate = buildRegion(data, world);
        for (ProtectedRegion other : manager.getApplicableRegions(candidate)) {
            if (!other.getId().equals("__global__")) return false;
        }
        var localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        return WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(localPlayer, BukkitAdapter.adapt(world))
                || manager.getApplicableRegions(BlockVector3.at(data.getX(), data.getY(), data.getZ()))
                .testState(localPlayer, Flags.BUILD);
    }

    public boolean canBuild(Player player, Location location, StateFlag... flags) {
        World world = location.getWorld();
        if (world == null) return false;
        try {
            manager(world);
            var localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
            if (WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(localPlayer, BukkitAdapter.adapt(world))) return true;
            return WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery()
                    .testBuild(BukkitAdapter.adapt(location), localPlayer, flags);
        } catch (RuntimeException e) {
            return false;
        }
    }

    public void upsert(RegionData data) {
        World world = Bukkit.getWorld(data.getWorld());
        if (world == null) return;
        RegionManager manager = manager(world);
        ProtectedRegion target = manager.getRegion(regionId(data));
        ProtectedRegion shape = buildRegion(data, world);
        if (target == null || !target.getMinimumPoint().equals(shape.getMinimumPoint())
                || !target.getMaximumPoint().equals(shape.getMaximumPoint())
                || !(target instanceof ProtectedCuboidRegion)) {
            if (target != null) shape.copyFrom(target);
            target = shape;
            manager.addRegion(target);
        }
        applyData(target, data);
    }

    public boolean canExplode(Location location) {
        World world = location.getWorld();
        if (world == null) return false;
        try {
            return manager(world).getApplicableRegions(BlockVector3.at(location.getBlockX(),
                    location.getBlockY(), location.getBlockZ())).testState(null, Flags.TNT);
        } catch (RuntimeException e) {
            return false;
        }
    }

    public void remove(RegionData data) {
        World world = Bukkit.getWorld(data.getWorld());
        if (world != null) manager(world).removeRegion(regionId(data), RemovalStrategy.UNSET_PARENT_IN_CHILDREN);
    }

    public void reconcile(World world, Collection<RegionData> regions) {
        RegionManager manager = manager(world);
        Set<String> expected = new HashSet<>();
        for (RegionData region : regions) {
            if (!region.getWorld().equals(world.getName())) continue;
            expected.add(regionId(region));
            upsert(region);
        }
        for (String id : new ArrayList<>(manager.getRegions().keySet())) {
            if (isManagedId(id) && !expected.contains(id)) {
                manager.removeRegion(id, RemovalStrategy.UNSET_PARENT_IN_CHILDREN);
            }
        }
    }

    public void flush() {
        for (World world : Bukkit.getWorlds()) {
            try {
                manager(world).saveChanges();
            } catch (Exception e) {
                plugin.getLogger().severe("Ошибка сохранения WorldGuard: " + e.getMessage());
            }
        }
    }
}
