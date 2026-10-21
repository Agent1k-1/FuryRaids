package com.agent1k.region.manager;

import com.agent1k.FuryRaids;
import com.agent1k.region.data.RegionData;
import com.agent1k.region.data.ViewSettings;
import com.agent1k.region.view.RegionOutline;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public final class RegionViewManager implements Listener {
    private final Function<String, RegionData> regions;
    private final Function<UUID, Player> players;
    private final Scheduler scheduler;
    private final Consumer<String> warning;
    private final Consumer<Player> stopped;
    private final Map<UUID, Session> sessions = new HashMap<>();

    public RegionViewManager(FuryRaids plugin) {
        this(plugin.getRegionManager()::byId, Bukkit::getPlayer,
                (task, period) -> Bukkit.getScheduler().runTaskTimer(plugin, task, 0L, period),
                plugin.getLogger()::warning, player -> plugin.getMessages().send(player, "view-off"));
    }

    RegionViewManager(Function<String, RegionData> regions, Function<UUID, Player> players,
                      Scheduler scheduler, Consumer<String> warning, Consumer<Player> stopped) {
        this.regions = regions;
        this.players = players;
        this.scheduler = scheduler;
        this.warning = warning;
        this.stopped = stopped;
    }

    @FunctionalInterface
    interface Scheduler { BukkitTask repeat(Runnable task, long period); }

    public boolean stop(UUID player) {
        return stop(player, true);
    }

    private boolean stop(UUID playerId, boolean notify) {
        Session session = sessions.remove(playerId);
        if (session == null) return false;
        session.cancel();
        Player player = players.apply(playerId);
        if (notify && player != null && player.isOnline()) stopped.accept(player);
        return true;
    }

    public boolean isShowing(UUID playerId) {
        return sessions.containsKey(playerId);
    }

    public void show(Player player, RegionData region, ViewSettings settings) {
        stop(player.getUniqueId(), false);
        World world = player.getWorld();
        List<RegionOutline.Point> points = RegionOutline.points(region, world.getMinHeight(), world.getMaxHeight(),
                settings.spacing(), settings.maxPoints());
        Session session = new Session(player.getUniqueId(), region.getId(), settings, points);
        sessions.put(player.getUniqueId(), session);
        session.task = scheduler.repeat(session, settings.intervalTicks());
    }

    public void shutdown() {
        for (UUID playerId : List.copyOf(sessions.keySet())) stop(playerId);
    }

    @EventHandler public void onQuit(PlayerQuitEvent event) { stop(event.getPlayer().getUniqueId(), false); }
    @EventHandler public void onWorldChange(PlayerChangedWorldEvent event) { stop(event.getPlayer().getUniqueId()); }

    private final class Session implements Runnable {
        private final UUID playerId;
        private final String regionId;
        private final ViewSettings settings;
        private final List<RegionOutline.Point> points;
        private int elapsed;
        private BukkitTask task;

        private Session(UUID playerId, String regionId, ViewSettings settings, List<RegionOutline.Point> points) {
            this.playerId = playerId;
            this.regionId = regionId;
            this.settings = settings;
            this.points = points;
        }

        private void cancel() { if (task != null) task.cancel(); }

        @Override public void run() {
            Player player = players.apply(playerId);
            RegionData region = regions.apply(regionId);
            if (player == null || !player.isOnline() || region == null || elapsed >= settings.durationTicks()
                    || !player.getWorld().getName().equals(region.getWorld())
                    || !player.hasPermission("eregionplus.view")
                    || (!region.isOwnerOrMember(playerId) && !player.hasPermission("eregionplus.admin"))) {
                stop(playerId);
                return;
            }
            Location eye = player.getEyeLocation();
            double maxDistance = settings.range() * settings.range();
            try {
                for (RegionOutline.Point point : points) {
                    double dx = eye.getX() - point.x(), dy = eye.getY() - point.y(), dz = eye.getZ() - point.z();
                    if (dx * dx + dy * dy + dz * dz > maxDistance) continue;
                    player.spawnParticle(settings.particle(), point.x(), point.y(), point.z(), settings.count(),
                            settings.offsetX(), settings.offsetY(), settings.offsetZ(), settings.speed(), settings.data(), true);
                }
                elapsed += settings.intervalTicks();
            } catch (IllegalArgumentException e) {
                warning.accept("Показ границ остановлен: " + e.getMessage());
                stop(playerId);
            }
        }
    }
}
