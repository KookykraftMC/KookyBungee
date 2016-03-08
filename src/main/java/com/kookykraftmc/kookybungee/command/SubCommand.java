package com.kookykraftmc.kookybungee.command;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;

public abstract class SubCommand implements ICommand {

    public static SubCommand asMirror(final ICommand command) {
        return new SubCommand(command.getName(), command.getIPermission(), command instanceof SubCommand ? command.getUsage() : command.getUsage().substring(1), command.getAliases()) {
            public BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException {
                return command.Iexecute(sender, args);
            }
        };
    }

    private String name, usage, permission;
    private String[] aliases;
    private CommandException usagemessage;

    public SubCommand(String name, String permission, String usage, String... aliases) {
        this.name = name;
        this.aliases = aliases;
        this.usage = usage;
        this.permission = permission;
        usagemessage = new CommandException("Invalid usage: " + getUsage(), this);
    }

    public abstract BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException;

    public String getUsage() {
        return usage;
    }

    public String getIPermission() {
        return permission;
    }

    public String getName() {
        return name;
    }

    public String[] getAliases() {
        return aliases;
    }

    public CommandException invalidUsage() {
        return usagemessage;
    }

}
