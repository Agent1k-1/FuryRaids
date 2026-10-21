package com.agent1k.region.click;

public enum ClickType {
    LEFT_CLICK_ACTION("left_click_action"),
    RIGHT_CLICK_ACTION("right_click_action");

    private final String key;

    ClickType(String key) { this.key = key; }

    public String getKey() { return key; }

    public static ClickType from(org.bukkit.event.inventory.ClickType click) {
        return switch (click) {
            case LEFT -> LEFT_CLICK_ACTION;
            case RIGHT -> RIGHT_CLICK_ACTION;
            default -> null;
        };
    }
}
