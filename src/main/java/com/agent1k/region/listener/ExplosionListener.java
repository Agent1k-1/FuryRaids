package com.agent1k.region.listener;

import com.agent1k.FuryRaids;
import com.agent1k.api.flag.RegionFlag;
import com.agent1k.region.data.RegionData;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

public final class ExplosionListener extends AbstractRegionListener {

    public ExplosionListener(@NotNull FuryRaids plugin) {
        super(plugin);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityExplode(@NotNull EntityExplodeEvent event) {
        RegionFlag flag = isTnt(event.getEntity()) ? RegionFlag.TNT : RegionFlag.EXPLOSION;
        filter(event.blockList(), flag);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockExplode(@NotNull BlockExplodeEvent event) {
        filter(event.blockList(), RegionFlag.EXPLOSION);
    }

    private void filter(@NotNull List<Block> blocks, @NotNull RegionFlag flag) {
        Iterator<Block> it = blocks.iterator();
        while (it.hasNext()) {
            Block block = it.next();
            RegionData region = regionAt(block.getLocation());
            if (region == null) continue;
            if (isCenter(region, block) || !allowedByFlag(region, flag)) {
                it.remove();
                continue;
            }
        }
    }

    private boolean isTnt(@NotNull Entity entity) {
        return entity instanceof TNTPrimed || entity instanceof ExplosiveMinecart;
    }

    private boolean isCenter(@NotNull RegionData region, @NotNull Block block) {
        return block.getX() == region.getX()
                && block.getY() == region.getY()
                && block.getZ() == region.getZ()
                && block.getWorld().getName().equals(region.getWorld());
    }
}
