package com.agent1k.region.command.sub;

import com.agent1k.FuryRaids;
import com.agent1k.api.flag.RegionFlag;
import com.agent1k.region.command.SubCommand;
import com.agent1k.region.data.RegionData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;

public final class FlagCMD extends SubCommand {

    public FlagCMD(@NotNull FuryRaids plugin) {
        super(plugin);
    }

    @Override
    @NotNull public String getName() { return "flag"; }

    @Override
    @NotNull public String getPermission() { return "eregionplus.flag"; }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player)) return;
        if (args.length < 1) {
            send(sender, "usage-flag");
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
        RegionFlag flag = RegionFlag.from(args[0]);
        if (flag == null) {
            send(sender, "flag-unknown", map("flag", args[0], "available", availableFlags()));
            return;
        }
        boolean current = region.isFlagEnabled(flag);
        boolean next = !current;
        if (!plugin.getAPI().setFlag(region.getId(), flag, next)) {
            send(sender, "error-region-storage");
            return;
        }
        String stateKey = next ? "flag-value-on" : "flag-value-off";
        send(sender, "flag-set", map("flag", flag.getKey(), "value", plugin.getMessages().getText(stateKey)));
    }

    @Override
    @NotNull
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length != 1) return List.of();
        String prefix = args[0].toLowerCase(Locale.ROOT);
        List<String> out = new ArrayList<>();
        for (RegionFlag flag : RegionFlag.values()) {
            if (flag.getKey().startsWith(prefix)) out.add(flag.getKey());
        }
        return out;
    }

    @NotNull
    private String availableFlags() {
        StringJoiner sj = new StringJoiner(", ");
        for (RegionFlag f : RegionFlag.values()) sj.add(f.getKey());
        return sj.toString();
    }
}
