package com.agent1k.region.command.sub;

import com.agent1k.FuryRaids;
import com.agent1k.region.command.SubCommand;
import com.agent1k.region.command.sub.admin.AdminHelpCMD;
import com.agent1k.region.command.sub.admin.GiveCMD;
import com.agent1k.region.command.sub.admin.ReloadCMD;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class AdminCMD extends SubCommand {

    private final Map<String, SubCommand> registry = new LinkedHashMap<>();
    private final AdminHelpCMD helpCommand;

    public AdminCMD(@NotNull FuryRaids plugin) {
        super(plugin);
        this.helpCommand = new AdminHelpCMD(plugin);
        register(helpCommand);
        register(new GiveCMD(plugin));
        register(new ReloadCMD(plugin));
    }

    private void register(@NotNull SubCommand sub) {
        registry.put(sub.getName().toLowerCase(Locale.ROOT), sub);
        for (String a : sub.getAliases()) registry.put(a.toLowerCase(Locale.ROOT), sub);
    }

    @Override
    @NotNull public String getName() { return "admin"; }

    @Override
    @NotNull public String getPermission() { return "eregionplus.admin"; }

    @Override
    @NotNull public String[] getAliases() { return new String[]{"a"}; }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 0) {
            helpCommand.execute(sender, args);
            return;
        }
        SubCommand sub = registry.get(args[0].toLowerCase(Locale.ROOT));
        if (sub == null) {
            helpCommand.execute(sender, args);
            return;
        }
        if (!hasAccess(sender, sub)) {
            send(sender, "no-permission");
            return;
        }
        sub.execute(sender, Arrays.copyOfRange(args, 1, args.length));
    }

    @Override
    @NotNull
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            List<String> out = new ArrayList<>();
            for (SubCommand s : distinctSubs()) {
                if (!hasAccess(sender, s)) continue;
                if (s.getName().startsWith(prefix)) out.add(s.getName());
            }
            return out;
        }
        SubCommand sub = registry.get(args[0].toLowerCase(Locale.ROOT));
        if (sub == null || !hasAccess(sender, sub)) return List.of();
        return sub.tabComplete(sender, Arrays.copyOfRange(args, 1, args.length));
    }

    @NotNull
    private List<SubCommand> distinctSubs() {
        List<SubCommand> out = new ArrayList<>();
        for (SubCommand s : registry.values()) if (!out.contains(s)) out.add(s);
        return out;
    }

    private boolean hasAccess(@NotNull CommandSender sender, @NotNull SubCommand sub) {
        return sub.getPermission().isEmpty() || sender.hasPermission(sub.getPermission());
    }
}
