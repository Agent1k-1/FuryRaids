package com.agent1k.region.util;

import com.agent1k.FuryRaids;
import com.agent1k.region.data.RegionType;
import com.agent1k.region.data.RegionData;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class ItemUtil {

    private final NamespacedKey typeKey;
    private final NamespacedKey durabilityKey;
    private final NamespacedKey maximumKey;
    private final NamespacedKey damageTimeKey;

    public ItemUtil(@NotNull FuryRaids plugin) {
        this.typeKey = new NamespacedKey("eregionplus", "region_type_id");
        this.durabilityKey = new NamespacedKey(plugin, "core_durability");
        this.maximumKey = new NamespacedKey(plugin, "core_max_durability");
        this.damageTimeKey = new NamespacedKey(plugin, "core_last_damage");
    }

    @NotNull
    public ItemStack build(@NotNull RegionType type, int amount) {
        return build(type, amount, type.durability(), type.durability(), 0L);
    }

    public ItemStack build(RegionType type, RegionData region) {
        return build(type, 1, region.getDurability(), region.getMaxDurability(), region.getLastDamageTime());
    }

    private ItemStack build(RegionType type, int amount, int durability, int maximum, long lastDamage) {
        ItemStack item = new ItemStack(type.material(), Math.max(1, Math.min(64, amount)));
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ColorUtil.apply(type.displayName()));
            List<String> lore = new ArrayList<>(type.lore().size());
            for (String line : type.lore()) lore.add(ColorUtil.apply(line));
            meta.setLore(lore);
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(typeKey, PersistentDataType.INTEGER, type.id());
            pdc.set(durabilityKey, PersistentDataType.INTEGER, durability);
            pdc.set(maximumKey, PersistentDataType.INTEGER, maximum);
            pdc.set(damageTimeKey, PersistentDataType.LONG, lastDamage);
            item.setItemMeta(meta);
        }
        return item;
    }

    @Nullable
    public Integer readTypeId(@NotNull ItemStack item) {
        if (!item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.get(typeKey, PersistentDataType.INTEGER);
    }

    @NotNull
    public NamespacedKey typeKey() {
        return typeKey;
    }

    public int readMaxDurability(ItemStack item, int fallback) {
        if (!item.hasItemMeta()) return fallback;
        return Math.max(0, item.getItemMeta().getPersistentDataContainer()
                .getOrDefault(maximumKey, PersistentDataType.INTEGER, fallback));
    }

    public int readDurability(ItemStack item, int maximum) {
        if (!item.hasItemMeta()) return maximum;
        return Math.max(0, Math.min(maximum, item.getItemMeta().getPersistentDataContainer()
                .getOrDefault(durabilityKey, PersistentDataType.INTEGER, maximum)));
    }

    public long readLastDamage(ItemStack item) {
        return item.hasItemMeta() ? Math.max(0L, item.getItemMeta().getPersistentDataContainer()
                .getOrDefault(damageTimeKey, PersistentDataType.LONG, 0L)) : 0L;
    }
}
