package com.agent1k.region.command.sub;

import com.agent1k.FuryRaids;
import com.agent1k.region.command.SubCommand;
import com.agent1k.region.data.RegionData;
import com.agent1k.region.data.ViewSettings;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class ViewCMD extends SubCommand {
    public ViewCMD(FuryRaids plugin) { super(plugin); }

    @Override public String getName() { return "view"; }
    @Override public String getPermission() { return "eregionplus.view"; }

    @Override public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) return;
        if (plugin.getRegionViews().isShowing(player.getUniqueId())) {
            send(sender, "error-view-on");
            return;
        }
        RegionData region = plugin.getRegionManager().at(player.getLocation());
        if (region == null) {
            send(sender, "region-no-territoria");
            return;
        }
        if (!region.isOwnerOrMember(player.getUniqueId()) && !player.hasPermission("eregionplus.admin")) {
            send(sender, "view-not-member");
            return;
        }
        ViewSettings settings = plugin.getBlocks().viewById(region.getTypeId());
        if (!settings.enabled()) {
            send(sender, "view-off");
            return;
        }
        plugin.getRegionViews().show(player, region, settings);
        send(sender, "view-on", map("seconds", String.valueOf(settings.durationTicks() / 20)));
    }
}
