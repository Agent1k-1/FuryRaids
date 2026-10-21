package com.agent1k.region.listener;

import com.agent1k.FuryRaids;
import com.agent1k.region.data.RegionData;
import com.agent1k.api.flag.RegionFlag;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public abstract class AbstractRegionListener implements Listener {

    protected final FuryRaids plugin;

    protected AbstractRegionListener(@NotNull FuryRaids plugin) {
        this.plugin = plugin;
    }

    @Nullable
    protected final RegionData regionAt(@NotNull Location loc) {
        return plugin.getRegionManager().at(loc);
    }

    protected final boolean isBypass(@NotNull Player player) {
        return player.hasPermission("eregionplus.admin");
    }

    protected final boolean isMember(@NotNull RegionData region, @Nullable UUID uuid) {
        return uuid != null && region.isOwnerOrMember(uuid);
    }

    protected final boolean allowedByFlag(@NotNull RegionData region, @NotNull RegionFlag flag) {
        return region.isFlagEnabled(flag);
    }

    protected final void notify(@NotNull Player player, @NotNull String key) {
        plugin.getMessages().send(player, key);
    }
}
