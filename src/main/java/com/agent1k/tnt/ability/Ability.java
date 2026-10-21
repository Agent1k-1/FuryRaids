package com.agent1k.tnt.ability;

import org.bukkit.event.entity.EntityExplodeEvent;
import org.jetbrains.annotations.NotNull;

public interface Ability {

    void apply(@NotNull EntityExplodeEvent event, int size);
}
