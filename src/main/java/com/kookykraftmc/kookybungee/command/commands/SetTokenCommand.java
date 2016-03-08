package com.kookykraftmc.kookybungee.command.commands;

import com.kookykraftmc.kookybungee.KookyBungee;
import com.kookykraftmc.kookybungee.command.CommandException;
import com.kookykraftmc.kookybungee.command.SimpleCommand;
import com.kookykraftmc.kookybungee.player.ProxiedKookyPlayer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.UUID;

public class SetTokenCommand extends SimpleCommand {

    public SetTokenCommand() {
        super("settokens", "token.settokens", "/settokens <player> <tokens>", "settoken");
    }

    public BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException {
        if (args.length < 2) {
            throw invalidUsage();
        }
        String name = args[0];
        ProxiedKookyPlayer online = ProxiedKookyPlayer.getObject(name);
        boolean forcesave = false;
        if (online == null) {
            forcesave = true;
            UUID u = TokenCommand.getUUID(name);
            if (u == null) {
                throw new CommandException("Player not found", this);
            }
            try {
                online = new ProxiedKookyPlayer(u, KookyBungee.getInstance().loadData(u));
            } catch (Exception e) {
                throw new CommandException("Player not found", this);
            }
        }
        String number = args[1];
        int i;
        try {
            i = Integer.parseInt(number);
        } catch (Exception ex) {
            throw new CommandException("Invalid number", this);
        }
        online.setTokens(i);
        if (forcesave) {
            online.save();
        }
        BaseComponent[] components = TextComponent.fromLegacyText(ChatColor.GOLD + "Successfully set the tokens of \'" + online.getNickName() + "\' to \'" + String.valueOf(i) + "\'");
        HoverEvent event = new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.RED + "This was " + (!forcesave ? "not" : "") + " force saved"));
        for (BaseComponent c : components) {
            c.setHoverEvent(event);
        }
        return components;
    }

}
