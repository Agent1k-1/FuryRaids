package com.agent1k.region.action.impl;

import com.agent1k.region.action.Action;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class CommandAction implements Action {
    @Override
    public void send(@NotNull CommandSender sender, @NotNull String content) {
        if (!(sender instanceof Player player)) return;
        String command = normalize(content);
        if (!command.isEmpty()) player.performCommand(command);
    }

    static String normalize(String content) {
        String command = content.strip();
        return command.startsWith("/") ? command.substring(1).stripLeading() : command;
    }
}
