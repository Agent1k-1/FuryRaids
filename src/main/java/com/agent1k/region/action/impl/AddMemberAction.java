package com.agent1k.region.action.impl;

import com.agent1k.FuryRaids;
import com.agent1k.region.action.Action;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class AddMemberAction implements Action {
    @Override
    public void send(@NotNull CommandSender sender, @NotNull String content) {
        if (sender instanceof Player player) FuryRaids.instance().getMemberInputs().start(player);
    }
}
