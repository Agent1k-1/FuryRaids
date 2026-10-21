package com.agent1k.tnt.ability.impl;

import com.agent1k.tnt.ability.Ability;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.jetbrains.annotations.NotNull;

public final class BoxDestroyAbility implements Ability {

    private final Material target;

    public BoxDestroyAbility(@NotNull Material target) {
        this.target = target;
    }

    @Override
    public void apply(@NotNull EntityExplodeEvent event, int size) {
        if (size <= 0) return;
        Location center = event.getLocation();
        World world = center.getWorld();
        if (world == null) return;
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        for (int x = -size; x <= size; x++) {
            for (int y = -size; y < size; y++) {
                for (int z = -size; z <= size; z++) {
                    Block block = world.getBlockAt(cx + x, cy + y, cz + z);
                    if (block.getType() == target) {
                        if (!event.blockList().contains(block)) event.blockList().add(block);
                    }
                }
            }
        }
    }
}
