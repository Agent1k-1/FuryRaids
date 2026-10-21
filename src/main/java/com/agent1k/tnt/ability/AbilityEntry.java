package com.agent1k.tnt.ability;

import org.bukkit.event.entity.EntityExplodeEvent;
import org.jetbrains.annotations.NotNull;

public record AbilityEntry(@NotNull AbilityKind kind, int size) {

    public void apply(@NotNull EntityExplodeEvent event) {
        kind.getHandler().apply(event, size);
    }
}
