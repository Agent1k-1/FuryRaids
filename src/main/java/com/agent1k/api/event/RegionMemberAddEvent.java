package com.agent1k.api.event;

import com.agent1k.api.model.Region;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class RegionMemberAddEvent extends RegionEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private final UUID member;
    private boolean cancelled;

    public RegionMemberAddEvent(@NotNull Region region, @NotNull UUID member) {
        super(region);
        this.member = member;
    }

    @NotNull
    public UUID getMember() {
        return member;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
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
