package com.agent1k.region.manager;

import com.agent1k.FuryRaids;
import com.agent1k.api.event.RegionDeleteEvent;
import com.agent1k.region.data.RegionData;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

public final class RegionMemberInputManager implements Listener {
    private static final int TIMEOUT_SECONDS = 60;
    private static final Pattern PLAYER_NAME = Pattern.compile("[A-Za-z0-9_]{3,16}");
    private final FuryRaids plugin;
    private final Map<UUID, InputSession> inputs = new ConcurrentHashMap<>();

    public RegionMemberInputManager(FuryRaids plugin) {
        this.plugin = plugin;
    }

    public void start(Player player) {
        RegionData region = plugin.getRegionGUI().getOpenRegion(player);
        if (region == null) {
            send(player, "region-no-territoria");
            return;
        }
        if (!canAdd(player, region)) return;
        cancel(player.getUniqueId());
        player.closeInventory();
        InputSession session = new InputSession(region.getId());
        inputs.put(player.getUniqueId(), session);
        session.timeout = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!inputs.remove(player.getUniqueId(), session)) return;
            if (!player.isOnline()) return;
            send(player, "timeout-add-member");
            reopen(player, session.regionId);
        }, TIMEOUT_SECONDS * 20L);
        plugin.getMessages().send(player, "new-add-member", Map.of("seconds", String.valueOf(TIMEOUT_SECONDS)));
    }

    private boolean canAdd(Player player, RegionData region) {
        if (!player.hasPermission("eregionplus.add")) {
            send(player, "no-permission");
            return false;
        }
        if (!region.getOwner().equals(player.getUniqueId()) && !player.hasPermission("eregionplus.admin")) {
            send(player, "region-no-owner");
            return false;
        }
        if (!plugin.getRegionGUI().canView(player, region)) {
            send(player, "protect-interact");
            return false;
        }
        return true;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncChatEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        InputSession session = inputs.get(playerId);
        if (session == null) return;
        event.setCancelled(true);
        event.viewers().clear();
        if (!session.processing.compareAndSet(false, true)) return;
        String nickname = PlainTextComponentSerializer.plainText().serialize(event.originalMessage()).strip();
        try {
            Bukkit.getScheduler().runTask(plugin, () -> submit(playerId, session, nickname));
        } catch (IllegalPluginAccessException ignored) {
            inputs.remove(playerId, session);
        }
    }

    private void submit(UUID playerId, InputSession session, String nickname) {
        if (!inputs.remove(playerId, session)) return;
        session.timeout.cancel();
        Player player = Bukkit.getPlayer(playerId);
        if (player == null || !player.isOnline()) return;
        try {
            if (nickname.equalsIgnoreCase("отмена") || nickname.equalsIgnoreCase("cancel")) {
                send(player, "new-add-memder-cancel");
                return;
            }
            RegionData region = plugin.getRegionManager().byId(session.regionId);
            if (region == null) {
                send(player, "region-no-territoria");
                return;
            }
            if (!canAdd(player, region)) return;
            if (!PLAYER_NAME.matcher(nickname).matches()) {
                plugin.getMessages().send(player, "player-not-found", Map.of("player", nickname));
                return;
            }
            OfflinePlayer target = Bukkit.getPlayerExact(nickname);
            if (target == null) target = Bukkit.getOfflinePlayerIfCached(nickname);
            if (target == null || (!target.isOnline() && !target.hasPlayedBefore())) {
                plugin.getMessages().send(player, "player-not-found", Map.of("player", nickname));
                return;
            }
            String targetName = target.getName() == null ? nickname : target.getName();
            if (target.getUniqueId().equals(playerId)) {
                send(player, "self-target");
                return;
            }
            if (region.isOwnerOrMember(target.getUniqueId())) {
                plugin.getMessages().send(player, "already-member", Map.of("player", targetName));
                return;
            }
            if (!plugin.getAPI().addMember(region.getId(), target.getUniqueId())) {
                send(player, "error-region-storage");
                return;
            }
            plugin.getMessages().send(player, "new-add-member-player", Map.of("player", targetName));
        } finally {
            reopen(player, session.regionId);
        }
    }

    private void reopen(Player player, String regionId) {
        if (player.isOnline() && !inputs.containsKey(player.getUniqueId())) plugin.getRegionGUI().open(player, regionId);
    }

    private void send(Player player, String key) {
        plugin.getMessages().send(player, key);
    }

    private void cancel(UUID playerId) {
        InputSession session = inputs.remove(playerId);
        if (session != null && session.timeout != null) session.timeout.cancel();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cancel(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onDelete(RegionDeleteEvent event) {
        for (var entry : inputs.entrySet()) {
            if (!entry.getValue().regionId.equals(event.getRegion().getId())) continue;
            cancel(entry.getKey());
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player != null) send(player, "region-no-territoria");
        }
    }

    public void shutdown(boolean notify) {
        for (UUID playerId : inputs.keySet()) {
            cancel(playerId);
            Player player = Bukkit.getPlayer(playerId);
            if (notify && player != null) send(player, "new-add-memder-cancel");
        }
    }

    private static final class InputSession {
        private final String regionId;
        private final AtomicBoolean processing = new AtomicBoolean();
        private BukkitTask timeout;

        private InputSession(String regionId) { this.regionId = regionId; }
    }
}
