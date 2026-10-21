package com.agent1k.tnt.action;

import com.agent1k.tnt.action.impl.ActionBarAction;
import com.agent1k.tnt.action.impl.MessageAction;
import com.agent1k.tnt.action.impl.SoundAction;
import com.agent1k.tnt.action.impl.TitleAction;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public enum ActionType {

    MESSAGE("[message]", new MessageAction()),
    ACTIONBAR("[actionbar]", new ActionBarAction()),
    TITLE("[title]", new TitleAction()),
    SOUND("[sound]", new SoundAction());

    private final String prefix;
    private final Action action;

    ActionType(@NotNull String prefix, @NotNull Action action) {
        this.prefix = prefix;
        this.action = action;
    }

    @NotNull
    public String getPrefix() {
        return prefix;
    }

    public static void dispatch(@NotNull CommandSender sender, @NotNull String rawLine) {
        if (rawLine.isEmpty()) {
            sender.sendMessage("");
            return;
        }
        for (ActionType type : values()) {
            if (rawLine.startsWith(type.prefix)) {
                type.action.send(sender, rawLine.substring(type.prefix.length()).stripLeading());
                return;
            }
        }
        MESSAGE.action.send(sender, rawLine);
    }

    public static void dispatchAll(@NotNull CommandSender sender, @NotNull List<String> lines) {
        for (String line : lines) dispatch(sender, line);
    }

    @NotNull
    public static String stripPrefix(@NotNull String rawLine) {
        for (ActionType type : values()) {
            if (rawLine.startsWith(type.prefix)) {
                return rawLine.substring(type.prefix.length()).stripLeading();
            }
        }
        return rawLine;
    }
}
