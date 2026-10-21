package com.agent1k.tnt;

import com.agent1k.tnt.ability.AbilityEntry;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record TntType(
        @NotNull String id,
        @NotNull String name,
        @NotNull List<String> lore,
        @NotNull Material material,
        int radius,
        boolean autofire,
        @NotNull List<AbilityEntry> abilities,
        int regionDamage
) {}
