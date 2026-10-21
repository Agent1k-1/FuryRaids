package com.agent1k.tnt.command;

import com.agent1k.FuryRaids;
import com.agent1k.tnt.command.sub.GiveCMD;
import com.agent1k.tnt.command.sub.ReloadCMD;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ETNTCommand implements CommandExecutor, TabCompleter {

    private final FuryRaids plugin;
    private final Map<String, SubCommand> registry = new LinkedHashMap<>();

    public ETNTCommand(@NotNull FuryRaids plugin) {
        this.plugin = plugin;
        register(new GiveCMD(plugin));
        register(new ReloadCMD(plugin));

    }

    private void register(@NotNull SubCommand sub) {
        registry.put(sub.getName().toLowerCase(Locale.ROOT), sub);
        for (String alias : sub.getAliases()) {
            registry.put(alias.toLowerCase(Locale.ROOT), sub);
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            plugin.getTntMessages().send(sender, "help");
            return true;
        }
        SubCommand sub = registry.get(args[0].toLowerCase(Locale.ROOT));
        if (sub == null) {
            plugin.getTntMessages().send(sender, "help");
            return true;
        }
        if (!hasAccess(sender, sub)) {
            plugin.getTntMessages().send(sender, "no-permission");
            return true;
        }
        sub.execute(sender, Arrays.copyOfRange(args, 1, args.length));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            List<String> out = new ArrayList<>();
            for (SubCommand s : distinct()) {
                if (!hasAccess(sender, s)) continue;
                if (s.getName().startsWith(prefix)) out.add(s.getName());
            }
            return out;
        }
        SubCommand sub = registry.get(args[0].toLowerCase(Locale.ROOT));
        if (sub == null || !hasAccess(sender, sub)) return List.of();
        return sub.tabComplete(sender, Arrays.copyOfRange(args, 1, args.length));
    }

    private boolean hasAccess(@NotNull CommandSender sender, @NotNull SubCommand sub) {
        return sub.getPermission().isEmpty() || sender.hasPermission(sub.getPermission());
    }

    @NotNull
    private Collection<SubCommand> distinct() {
        return new LinkedHashSet<>(registry.values());
    }
}
