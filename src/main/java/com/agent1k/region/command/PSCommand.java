package com.agent1k.region.command;

import com.agent1k.FuryRaids;
import com.agent1k.region.command.sub.AddCMD;
import com.agent1k.region.command.sub.AdminCMD;
import com.agent1k.region.command.sub.FlagCMD;
import com.agent1k.region.command.sub.HelpCMD;
import com.agent1k.region.command.sub.InfoCMD;
import com.agent1k.region.command.sub.RemoveCMD;
import com.agent1k.region.command.sub.ViewCMD;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
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

public final class PSCommand implements CommandExecutor, TabCompleter {

    private final FuryRaids plugin;
    private final Map<String, SubCommand> registry = new LinkedHashMap<>();
    private final HelpCMD helpCommand;

    public PSCommand(@NotNull FuryRaids plugin) {
        this.plugin = plugin;
        this.helpCommand = new HelpCMD(plugin);
        register(helpCommand);
        register(new AddCMD(plugin));
        register(new RemoveCMD(plugin));
        register(new FlagCMD(plugin));
        register(new InfoCMD(plugin));
        register(new ViewCMD(plugin));
        register(new AdminCMD(plugin));
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
            helpCommand.execute(sender, args);
            return true;
        }
        SubCommand sub = registry.get(args[0].toLowerCase(Locale.ROOT));
        if (sub == null) {
            helpCommand.execute(sender, args);
            return true;
        }
        if (!hasAccess(sender, sub)) {
            plugin.getMessages().send(sender, "no-permission");
            return true;
        }
        sub.execute(sender, tail(args));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            List<String> out = new ArrayList<>();
            Collection<SubCommand> unique = distinct();
            for (SubCommand s : unique) {
                if (!hasAccess(sender, s)) continue;
                if (s.getName().startsWith(prefix)) out.add(s.getName());
            }
            return out;
        }
        SubCommand sub = registry.get(args[0].toLowerCase(Locale.ROOT));
        if (sub == null || !hasAccess(sender, sub)) return List.of();
        return sub.tabComplete(sender, tail(args));
    }

    private boolean hasAccess(@NotNull CommandSender sender, @NotNull SubCommand sub) {
        return sub.getPermission().isEmpty() || sender.hasPermission(sub.getPermission());
    }

    @NotNull
    private Collection<SubCommand> distinct() {
        return new LinkedHashSet<>(registry.values());
    }

    @NotNull
    private String[] tail(@NotNull String[] args) {
        return Arrays.copyOfRange(args, 1, args.length);
    }
}
