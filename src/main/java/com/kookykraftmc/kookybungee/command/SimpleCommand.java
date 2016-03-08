package com.kookykraftmc.kookybungee.command;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

import java.util.logging.Level;

public abstract class SimpleCommand extends Command implements ICommand {

    public static SimpleCommand asMirror(final ICommand command) {
        return new SimpleCommand(command.getName(), command.getIPermission(), command.getUsage(), command.getAliases()) {
            public BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException {
                return command.Iexecute(sender, args);
            }
        };
    }

    private String permissionstring;
    private String usage;
    private CommandException invalidusage;

    public SimpleCommand(String name, String permission, String usage, String... aliases) {
        super(name, null, aliases);
        this.permissionstring = permission;
        this.usage = usage;
        invalidusage = new CommandException("Invalid usage: " + getUsage(), this);
    }

    public void execute(CommandSender commandSender, String[] strings) {
        try {
            if (getIPermission() != null && !commandSender.hasPermission(getIPermission())) {
                throw new CommandException("You do not have permission for this command", this);
            }
            BaseComponent b[] = Iexecute(commandSender, strings);
            if (b != null) {
                commandSender.sendMessage(b);
            }
        } catch (CommandException e) {
            commandSender.sendMessage(e.getResponse());
        } catch (IllegalArgumentException e) {
            commandSender.sendMessage(new CommandException(e.getMessage(), this).getResponse());
        } catch (Throwable ex) {
            commandSender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "An internal " + ex.getClass().getSimpleName() + " has occurred\n" + ChatColor.RED + ex.getMessage()));
            ProxyServer.getInstance().getLogger().log(Level.WARNING, "An error occurred whilst executing " + getClass().getName(), ex);
        }
    }

    public abstract BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException;

    public String getUsage() {
        return usage;
    }

    public String getIPermission() {
        return permissionstring;
    }

    public CommandException invalidUsage() {
        return invalidusage;
    }

}
