package com.agent1k.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FuryRaidsProvider {

    private static FuryRaidsAPI instance;

    private FuryRaidsProvider() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    public static FuryRaidsAPI get() {
        if (instance == null) {
            throw new IllegalStateException("FuryRaids API ещё не инициализирован. Убедитесь, что плагин загружен и зависимость указана в plugin.yml.");
        }
        return instance;
    }

    @Nullable
    public static FuryRaidsAPI getOrNull() {
        return instance;
    }

    public static boolean isReady() {
        return instance != null;
    }

    public static void register(@NotNull FuryRaidsAPI api) {
        instance = api;
    }

    public static void unregister() {
        instance = null;
    }
}
