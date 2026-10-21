package com.agent1k.tnt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

public final class TntRegistry {

    private final Map<String, TntType> types = new LinkedHashMap<>();

    public void replaceAll(@NotNull Collection<TntType> loaded) {
        types.clear();
        for (TntType type : loaded) types.put(type.id().toLowerCase(Locale.ROOT), type);
    }

    @Nullable
    public TntType byId(@NotNull String id) {
        return types.get(id.toLowerCase(Locale.ROOT));
    }

    @NotNull
    public Collection<TntType> all() {
        return Collections.unmodifiableCollection(types.values());
    }

    @NotNull
    public Set<String> ids() {
        return Collections.unmodifiableSet(types.keySet());
    }

    @NotNull
    public String joinedIds() {
        StringJoiner sj = new StringJoiner(", ");
        for (String id : types.keySet()) sj.add(id);
        return sj.toString();
    }
}
