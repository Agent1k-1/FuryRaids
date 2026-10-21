package com.agent1k.tnt.action.impl;

import com.agent1k.tnt.action.Action;
import com.agent1k.tnt.util.ColorUtil;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class MessageAction implements Action {

    @Override
    public void send(@NotNull CommandSender sender, @NotNull String content) {
        sender.sendMessage(ColorUtil.apply(content));
    }
}
