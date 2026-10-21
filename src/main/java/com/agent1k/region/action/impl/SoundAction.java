package com.agent1k.region.action.impl;

import com.agent1k.region.action.Action;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public final class SoundAction implements Action {

    @Override
    public void send(@NotNull CommandSender sender, @NotNull String content) {
        if (!(sender instanceof Player player)) return;
        String[] parts = content.split(",");
        try {
            Sound sound = Sound.valueOf(parts[0].trim().toUpperCase(Locale.ROOT));
            float volume = parts.length > 1 ? Float.parseFloat(parts[1].trim()) : 1.0f;
            float pitch = parts.length > 2 ? Float.parseFloat(parts[2].trim()) : 1.0f;
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException ignored) {}
    }
}
