package com.agent1k.region.action;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public interface Action {

    void send(@NotNull CommandSender sender, @NotNull String content);
}
