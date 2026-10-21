package com.agent1k.tnt.command.sub;

import com.agent1k.FuryRaids;
import com.agent1k.tnt.command.SubCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class ReloadCMD extends SubCommand {

    public ReloadCMD(@NotNull FuryRaids plugin) {
        super(plugin);
    }

    @Override
    @NotNull public String getName() { return "reload"; }

    @Override
    @NotNull public String getPermission() { return "etnt.reload"; }

    @Override
    @NotNull public String[] getAliases() { return new String[]{"rl"}; }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        plugin.reloadAll();
        send(sender, "reloaded");
    }
}
