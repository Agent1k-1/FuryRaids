package com.agent1k.tnt;

import com.agent1k.FuryRaids;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TntBlockTracker {

    private final FuryRaids plugin;

    public TntBlockTracker(@NotNull FuryRaids plugin) {
        this.plugin = plugin;
    }

    public void mark(@NotNull Block block, @NotNull String typeId) {
        container(block).set(keyFor(block), PersistentDataType.STRING, typeId);
    }

    @Nullable
    public String read(@NotNull Block block) {
        return container(block).get(keyFor(block), PersistentDataType.STRING);
    }

    public void clear(@NotNull Block block) {
        container(block).remove(keyFor(block));
    }

    @NotNull
    private PersistentDataContainer container(@NotNull Block block) {
        return block.getChunk().getPersistentDataContainer();
    }

    @NotNull
    private NamespacedKey keyFor(@NotNull Block block) {
        return new NamespacedKey("etnt", "block_" + block.getX() + "_" + block.getY() + "_" + block.getZ());
    }
}
