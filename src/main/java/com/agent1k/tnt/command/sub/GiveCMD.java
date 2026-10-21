package com.agent1k.tnt.command.sub;

import com.agent1k.FuryRaids;
import com.agent1k.tnt.command.SubCommand;
import com.agent1k.tnt.TntType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class GiveCMD extends SubCommand {

    public GiveCMD(@NotNull FuryRaids plugin) {
        super(plugin);
    }

    @Override
    @NotNull public String getName() { return "give"; }

    @Override
    @NotNull public String getPermission() { return "etnt.give"; }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 3) {
            send(sender, "usage-give");
            return;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            send(sender, "player-offline", map("player", args[0]));
            return;
        }
        TntType type = plugin.getTntRegistry().byId(args[1]);
        if (type == null) {
            send(sender, "unknown-type", map("type", args[1], "available", plugin.getTntRegistry().joinedIds()));
            return;
        }
        Integer amount = parseInt(args[2]);
        if (amount == null || amount < 1 || amount > 64) {
            send(sender, "invalid-amount");
            return;
        }
        ItemStack item = plugin.getTntItems().build(type, amount);
        target.getInventory().addItem(item).values().forEach(leftover ->
                target.getWorld().dropItemNaturally(target.getLocation(), leftover));
        send(sender, "given", map("player", target.getName(), "type", type.name(), "amount", String.valueOf(amount)));
        plugin.getTntMessages().send(target, "received", map("type", type.name(), "amount", String.valueOf(amount)));
    }

    @Override
    @NotNull
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        List<String> out = new ArrayList<>();
        String prefix = args[args.length - 1].toLowerCase(Locale.ROOT);
        switch (args.length) {
            case 1 -> {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) out.add(p.getName());
                }
            }
            case 2 -> {
                for (String id : plugin.getTntRegistry().ids()) {
                    if (id.startsWith(prefix)) out.add(id);
                }
            }
            case 3 -> {
                for (String v : new String[]{"1", "8", "16", "32", "64"}) {
                    if (v.startsWith(prefix)) out.add(v);
                }
            }
        }
        return out;
    }

    private Integer parseInt(@NotNull String raw) {
        try { return Integer.parseInt(raw); } catch (NumberFormatException e) { return null; }
    }
}
