package com.agent1k.region.action.impl;

import com.agent1k.region.action.Action;
import com.agent1k.region.util.ColorUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class TitleAction implements Action {

    private static final int FADE_IN = 10;
    private static final int STAY = 40;
    private static final int FADE_OUT = 10;

    @Override
    public void send(@NotNull CommandSender sender, @NotNull String content) {
        if (!(sender instanceof Player player)) return;
        String[] parts = content.split("\\|", 2);
        String title = ColorUtil.apply(parts[0]);
        String subtitle = parts.length > 1 ? ColorUtil.apply(parts[1]) : "";
        player.sendTitle(title, subtitle, FADE_IN, STAY, FADE_OUT);
    }
}
