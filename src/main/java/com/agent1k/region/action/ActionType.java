package com.agent1k.region.action;

import com.agent1k.region.action.impl.ActionBarAction;
import com.agent1k.region.action.impl.MessageAction;
import com.agent1k.region.action.impl.SoundAction;
import com.agent1k.region.action.impl.TitleAction;
import com.agent1k.region.action.impl.CloseAction;
import com.agent1k.region.action.impl.CommandAction;
import com.agent1k.region.action.impl.ConsoleAction;
import com.agent1k.region.action.impl.AddMemberAction;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public enum ActionType {

    MESSAGE("[message]", new MessageAction()),
    ACTIONBAR("[actionbar]", new ActionBarAction()),
    TITLE("[title]", new TitleAction()),
    SOUND("[sound]", new SoundAction()),
    CLOSE("[close]", new CloseAction()),
    COMMAND("[command]", new CommandAction()),
    CONSOLE("[console]", new ConsoleAction()),
    ADD_MEMBER("[add-member]", new AddMemberAction());

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

    @NotNull
    public Action getAction() {
        return action;
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
