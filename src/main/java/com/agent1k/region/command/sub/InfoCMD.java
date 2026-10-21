package com.agent1k.region.command.sub;

import com.agent1k.FuryRaids;
import com.agent1k.region.command.SubCommand;
import com.agent1k.region.data.RegionData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;

public final class InfoCMD extends SubCommand {

    public InfoCMD(@NotNull FuryRaids plugin) {
        super(plugin);
    }

    @Override
    @NotNull public String getName() { return "info"; }

    @Override
    @NotNull public String getPermission() { return "eregionplus.info"; }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player)) return;
        Player player = (Player) sender;
        RegionData region = plugin.getRegionManager().at(player.getLocation());
        if (region == null) {
            send(sender, "region-no-territoria");
            return;
        }
        Map<String, String> ph = new HashMap<>();
        ph.put("world", region.getWorld());
        ph.put("members", formatMembers(region));
        send(sender, "info", ph);
    }

    @NotNull
    private String formatMembers(@NotNull RegionData region) {
        if (region.getMembers().isEmpty()) return plugin.getMessages().getText("no-members");
        StringJoiner sj = new StringJoiner(", ");
        for (UUID u : region.getMembers()) {
            OfflinePlayer op = Bukkit.getOfflinePlayer(u);
            sj.add(op.getName() != null ? op.getName() : u.toString());
        }
        return sj.toString();
    }
}
