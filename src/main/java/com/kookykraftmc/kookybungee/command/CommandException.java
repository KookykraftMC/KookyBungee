package com.kookykraftmc.kookybungee.command;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class CommandException extends Exception {

    private static TextComponent error = new TextComponent("Error: ");

    static {
        error.setColor(ChatColor.RED);
        error.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.RED + "An error occurred whilst executing this command")));
    }

    private TextComponent errormessage;
    private ICommand command;

    public CommandException(String error, ICommand command) {
        super(error);
        this.command = command;
        errormessage = new TextComponent(error);
        errormessage.setColor(ChatColor.GOLD);
        errormessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.GRAY + command.getUsage())));
        errormessage.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command.getUsage()));
    }

    public ICommand getCommand() {
        return command;
    }

    public BaseComponent[] getResponse() {
        return new BaseComponent[]{error, errormessage};
    }

}
