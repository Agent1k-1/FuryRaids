package com.agent1k.api.flag;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;

public enum RegionFlag {

    EXPLOSION("explosion", false),
    TNT("tnt", false),
    ACCESS("access", false),
    CHEST("chest", false),
    PVP("pvp", false),
    DAMAGE_ANIMALS("damage-animals", false),
    DOORS("doors", false),
    BUTTONS("buttons", false),
    LEVERS("levers", false);

    private static final Map<String, RegionFlag> INDEX;

    static {
        INDEX = new HashMap<>();
        for (RegionFlag f : values()) INDEX.put(f.key, f);
    }

    private final String key;
    private final boolean defaultValue;

    RegionFlag(@NotNull String key, boolean defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    @NotNull
    public String getKey() {
        return key;
    }

    public boolean getDefaultValue() {
        return defaultValue;
    }

    @Nullable
    public static RegionFlag from(@NotNull String raw) {
        return INDEX.get(raw.toLowerCase(Locale.ROOT));
    }

    @NotNull
    public static EnumMap<RegionFlag, Boolean> defaults() {
        EnumMap<RegionFlag, Boolean> map = new EnumMap<>(RegionFlag.class);
        for (RegionFlag f : values()) map.put(f, f.defaultValue);
        return map;
    }
}
