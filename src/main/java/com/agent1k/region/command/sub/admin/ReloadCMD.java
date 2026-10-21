package com.agent1k.region.command.sub.admin;

import com.agent1k.FuryRaids;
import com.agent1k.region.command.SubCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class ReloadCMD extends SubCommand {

    public ReloadCMD(@NotNull FuryRaids plugin) {
        super(plugin);
    }

    @Override
    @NotNull public String getName() { return "reload"; }

    @Override
    @NotNull public String getPermission() { return "eregionplus.admin.reload"; }

    @Override
    @NotNull public String[] getAliases() { return new String[]{"rl"}; }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        plugin.reloadAll();
        send(sender, "reloaded");
    }
}
