package com.agent1k.region.command.sub.admin;

import com.agent1k.FuryRaids;
import com.agent1k.region.command.SubCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class AdminHelpCMD extends SubCommand {

    public AdminHelpCMD(@NotNull FuryRaids plugin) {
        super(plugin);
    }

    @Override
    @NotNull public String getName() { return "help"; }

    @Override
    @NotNull public String getPermission() { return "eregionplus.admin"; }

    @Override
    @NotNull public String[] getAliases() { return new String[]{"?", "хелп"}; }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        send(sender, "ps-admin-help");
    }
}
