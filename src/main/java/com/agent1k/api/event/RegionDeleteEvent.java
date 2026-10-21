package com.agent1k.api.event;

import com.agent1k.api.model.Region;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class RegionDeleteEvent extends RegionEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    public RegionDeleteEvent(@NotNull Region region) {
        super(region);
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
