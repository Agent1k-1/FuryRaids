package com.agent1k.region.action.impl;

import com.agent1k.region.action.Action;
import com.agent1k.region.util.ColorUtil;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class ActionBarAction implements Action {

    @Override
    public void send(@NotNull CommandSender sender, @NotNull String content) {
        if (!(sender instanceof Player player)) return;
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ColorUtil.apply(content)));
    }
}
