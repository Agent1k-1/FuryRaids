package com.agent1k.tnt.ability;

import com.agent1k.tnt.ability.impl.BoxDestroyAbility;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public enum AbilityKind {

    OBSIDIAN("obsidian", new BoxDestroyAbility(Material.OBSIDIAN)),
    BEDROCK("bedrock", new BoxDestroyAbility(Material.BEDROCK));

    private static final Map<String, AbilityKind> INDEX;

    static {
        INDEX = new HashMap<>();
        for (AbilityKind k : values()) INDEX.put(k.key, k);
    }

    private final String key;
    private final Ability handler;

    AbilityKind(@NotNull String key, @NotNull Ability handler) {
        this.key = key;
        this.handler = handler;
    }

    @NotNull
    public String getKey() {
        return key;
    }

    @NotNull
    public Ability getHandler() {
        return handler;
    }

    @Nullable
    public static AbilityKind from(@NotNull String raw) {
        return INDEX.get(raw.toLowerCase(Locale.ROOT));
    }
}
