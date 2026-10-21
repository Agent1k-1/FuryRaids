package com.agent1k.api.event;

import com.agent1k.api.flag.RegionFlag;
import com.agent1k.api.model.Region;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class RegionFlagChangeEvent extends RegionEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private final RegionFlag flag;
    private final boolean oldValue;
    private final boolean newValue;
    private boolean cancelled;

    public RegionFlagChangeEvent(@NotNull Region region, @NotNull RegionFlag flag, boolean oldValue, boolean newValue) {
        super(region);
        this.flag = flag;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    @NotNull
    public RegionFlag getFlag() {
        return flag;
    }

    public boolean getOldValue() {
        return oldValue;
    }

    public boolean getNewValue() {
        return newValue;
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
