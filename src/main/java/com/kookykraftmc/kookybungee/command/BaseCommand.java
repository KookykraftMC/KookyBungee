package com.kookykraftmc.kookybungee.command;

import com.kookykraftmc.api.global.java.ArgTrimmer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

import java.util.Set;
import java.util.logging.Level;

public class BaseCommand extends Command implements ICommand {

    private Set<ICommand> subcommands;
    private String permissionstring;
    private String usage;
    private CommandException invalidusage;

    public BaseCommand(String name, String permission, Set<ICommand> subcommands, String... aliases) {
        super(name, null, aliases);
        this.permissionstring = permission;
        this.usage = "/" + name + " <arg>";
        this.subcommands = subcommands;
        String s = "";
        for (ICommand command : subcommands) {
            String usage = command.getUsage();
            while (usage.startsWith("/")) {
                usage = usage.substring(1);
            }
            s += "\n" + getUsage().replace("<arg>", usage);
        }
        invalidusage = new CommandException("Invalid usage: " + getUsage() + s, this);
    }

    public void execute(CommandSender commandSender, String[] strings) {
        try {
            BaseComponent c[] = Iexecute(commandSender, strings);
            if (c != null) {
                commandSender.sendMessage(c);
            }
        } catch (CommandException e) {
            commandSender.sendMessage(e.getResponse());
        } catch (IllegalArgumentException e) {
            commandSender.sendMessage(new CommandException(e.getMessage(), this).getResponse());
            ProxyServer.getInstance().getLogger().log(Level.INFO, "An validation error occurred whilst executing", e);
        } catch (Throwable ex) {
            commandSender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "An internal " + ex.getClass().getSimpleName() + " has occurred\n" + ChatColor.RED + ex.getMessage()));
            ProxyServer.getInstance().getLogger().log(Level.WARNING, "An error occurred whilst executing " + getClass().getName(), ex);
        }
    }

    public BaseComponent[] Iexecute(CommandSender commandSender, String[] strings) throws CommandException {
        if (getIPermission() != null && !commandSender.hasPermission(getIPermission())) {
            throw new CommandException("You do not have permission for this command", this);
        }
        if (strings.length == 0) {
            throw invalidUsage();
        }
        String firstarg = strings[0];
        ICommand command = getCommand(firstarg);
        if (command != null) {
            if (command.getIPermission() != null && !commandSender.hasPermission(command.getIPermission())) {
                throw new CommandException("You do not have permission for this command", command);
            }
            return command.Iexecute(commandSender, new ArgTrimmer<>(String.class, strings).trim(1));

        }
        throw invalidUsage();
    }

    protected ICommand getCommand(String firstarg) {
        for (ICommand command : subcommands) {
            if (command.getName().equalsIgnoreCase(firstarg)) {
                return command;
            }
        }
        for (ICommand command : subcommands) {
            for (String alias : command.getAliases()) {
                if (alias.equalsIgnoreCase(firstarg)) {
                    return command;
                }
            }
        }
        return null;
    }

    public CommandException invalidUsage() {
        return invalidusage;
    }

    public String getUsage() {
        return usage;
    }

    public String getIPermission() {
        return permissionstring;
    }

}
