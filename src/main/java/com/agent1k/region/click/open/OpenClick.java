package com.agent1k.region.click.open;

import org.bukkit.event.block.Action;

import java.util.Locale;

public enum OpenClick {
    RIGHT;

    public boolean matches(Action action) { return action == Action.RIGHT_CLICK_BLOCK; }

    public static OpenClick from(String name) {
        return valueOf(name.trim().toUpperCase(Locale.ROOT));
    }
}
