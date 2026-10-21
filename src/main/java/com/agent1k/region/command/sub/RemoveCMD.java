package com.agent1k.region.command.sub;

import com.agent1k.FuryRaids;
import com.agent1k.region.command.SubCommand;
import com.agent1k.region.data.RegionData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public final class RemoveCMD extends SubCommand {

    public RemoveCMD(@NotNull FuryRaids plugin) {
        super(plugin);
    }

    @Override
    @NotNull public String getName() { return "remove"; }

    @Override
    @NotNull public String getPermission() { return "eregionplus.remove"; }

    @Override
    @NotNull public String[] getAliases() { return new String[]{"del", "delete", "kick"}; }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player)) return;
        if (args.length < 1) {
            send(sender, "usage-remove");
            return;
        }
        Player player = (Player) sender;
        RegionData region = plugin.getRegionManager().at(player.getLocation());
        if (region == null) {
            send(sender, "region-no-territoria");
            return;
        }
        if (!region.getOwner().equals(player.getUniqueId())) {
            send(sender, "region-no-owner");
            return;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayerIfCached(args[0]);
        if (target == null) target = Bukkit.getOfflinePlayer(args[0]);
        UUID uuid = target.getUniqueId();
        if (!region.isMember(uuid)) {
            send(sender, "not-member", map("player", args[0]));
            return;
        }
        if (!plugin.getAPI().removeMember(region.getId(), uuid)) {
            send(sender, "error-region-storage");
            return;
        }
        send(sender, "player-removed", map("player", args[0]));
    }

    @Override
    @NotNull
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length != 1 || !(sender instanceof Player player)) return List.of();
        RegionData region = plugin.getRegionManager().at(player.getLocation());
        if (region == null || !region.getOwner().equals(player.getUniqueId())) return List.of();
        String prefix = args[0].toLowerCase(Locale.ROOT);
        List<String> names = new ArrayList<>();
        for (UUID u : region.getMembers()) {
            OfflinePlayer op = Bukkit.getOfflinePlayer(u);
            String name = op.getName();
            if (name == null) continue;
            if (name.toLowerCase(Locale.ROOT).startsWith(prefix)) names.add(name);
        }
        return names;
    }
}
