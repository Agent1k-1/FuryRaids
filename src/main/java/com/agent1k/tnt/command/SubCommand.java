package com.agent1k.tnt.command;

import com.agent1k.FuryRaids;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class SubCommand {

    protected final FuryRaids plugin;

    protected SubCommand(@NotNull FuryRaids plugin) {
        this.plugin = plugin;
    }

    @NotNull public abstract String getName();
    @NotNull public abstract String getPermission();
    @NotNull public String[] getAliases() { return new String[0]; }

    public abstract void execute(@NotNull CommandSender sender, @NotNull String[] args);

    @NotNull
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        return Collections.emptyList();
    }

    protected final void send(@NotNull CommandSender sender, @NotNull String key) {
        plugin.getTntMessages().send(sender, key);
    }

    protected final void send(@NotNull CommandSender sender, @NotNull String key, @NotNull Map<String, String> ph) {
        plugin.getTntMessages().send(sender, key, ph);
    }

    @NotNull
    protected static Map<String, String> map(@NotNull String... kv) {
        Map<String, String> m = new HashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) m.put(kv[i], kv[i + 1]);
        return m;
    }
}
