package com.agent1k.api.event;

import com.agent1k.api.model.Region;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public abstract class RegionEvent extends Event {

    private final Region region;

    protected RegionEvent(@NotNull Region region) {
        this.region = region;
    }

    @NotNull
    public Region getRegion() {
        return region;
    }
}
