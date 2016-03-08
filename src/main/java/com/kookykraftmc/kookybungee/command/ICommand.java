package com.kookykraftmc.kookybungee.command;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;

public interface ICommand {

    BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException;

    String getUsage();

    String getIPermission();

    String getName();

    String[] getAliases();

    CommandException invalidUsage();

}
