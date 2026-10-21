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

public final class AddCMD extends SubCommand {

    public AddCMD(@NotNull FuryRaids plugin) {
        super(plugin);
    }

    @Override
    @NotNull public String getName() { return "add"; }

    @Override
    @NotNull public String getPermission() { return "eregionplus.add"; }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player)) return;
        if (args.length < 1) {
            send(sender, "usage-add");
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
        if (target.getUniqueId().equals(player.getUniqueId())) {
            send(sender, "self-target");
            return;
        }
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            send(sender, "player-not-found", map("player", args[0]));
            return;
        }
        if (region.isMember(target.getUniqueId())) {
            send(sender, "already-member", map("player", args[0]));
            return;
        }
        if (!plugin.getAPI().addMember(region.getId(), target.getUniqueId())) {
            send(sender, "error-region-storage");
            return;
        }
        send(sender, "player-added", map("player", args[0]));
    }

    @Override
    @NotNull
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length != 1) return List.of();
        String prefix = args[0].toLowerCase(Locale.ROOT);
        List<String> names = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) names.add(p.getName());
        }
        return names;
    }
}
