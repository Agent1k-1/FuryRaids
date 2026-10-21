package com.agent1k.region.action.impl;

import com.agent1k.region.action.Action;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public final class ConsoleAction implements Action {
    private final Supplier<Server> servers;

    public ConsoleAction() {
        this(Bukkit::getServer);
    }

    ConsoleAction(Supplier<Server> servers) {
        this.servers = servers;
    }

    @Override
    public void send(@NotNull CommandSender sender, @NotNull String content) {
        String command = CommandAction.normalize(content);
        if (command.isEmpty()) return;
        Server server = servers.get();
        server.dispatchCommand(server.getConsoleSender(), command);
    }
}
