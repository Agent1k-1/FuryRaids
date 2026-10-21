package com.agent1k.tnt.util;

import com.agent1k.FuryRaids;
import com.agent1k.tnt.TntType;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class ItemUtil {

    private final NamespacedKey typeKey;

    public ItemUtil(@NotNull FuryRaids plugin) {
        this.typeKey = new NamespacedKey("etnt", "etnt_type");
    }

    @NotNull
    public NamespacedKey typeKey() {
        return typeKey;
    }

    @NotNull
    public ItemStack build(@NotNull TntType type, int amount) {
        ItemStack item = new ItemStack(type.material(), Math.max(1, Math.min(64, amount)));
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ColorUtil.apply(type.name()));
            List<String> lore = new ArrayList<>(type.lore().size());
            for (String line : type.lore()) lore.add(ColorUtil.apply(line));
            meta.setLore(lore);
            meta.getPersistentDataContainer().set(typeKey, PersistentDataType.STRING, type.id());
            item.setItemMeta(meta);
        }
        return item;
    }

    @Nullable
    public String readTypeId(@NotNull ItemStack item) {
        if (!item.hasItemMeta()) return null;
        return readTypeId(item.getItemMeta());
    }

    @Nullable
    public String readTypeId(@NotNull PersistentDataHolder holder) {
        PersistentDataContainer pdc = holder.getPersistentDataContainer();
        return pdc.get(typeKey, PersistentDataType.STRING);
    }

    public void writeTypeId(@NotNull PersistentDataHolder holder, @NotNull String id) {
        holder.getPersistentDataContainer().set(typeKey, PersistentDataType.STRING, id);
    }
}
