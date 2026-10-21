package com.agent1k.region.data;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record RegionType(
        int id,
        @NotNull Material material,
        @NotNull String displayName,
        @NotNull List<String> lore,
        int radius,
        int durability
) {}
