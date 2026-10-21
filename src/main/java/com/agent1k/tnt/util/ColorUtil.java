package com.agent1k.tnt.util;

import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ColorUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    private ColorUtil() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    public static String apply(@NotNull String input) {
        Matcher matcher = HEX_PATTERN.matcher(input);
        StringBuilder buffer = new StringBuilder(input.length() + 32);
        while (matcher.find()) {
            matcher.appendReplacement(buffer, ChatColor.of("#" + matcher.group(1)).toString());
        }
        matcher.appendTail(buffer);
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }
}
