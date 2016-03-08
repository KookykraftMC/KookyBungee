package com.kookykraftmc.kookybungee.command.commands;

import com.kookykraftmc.api.global.ranks.Rank;
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

public class WhoisCommand extends SimpleCommand {

    public WhoisCommand() {
        super("whois", "rankmanager.whois", "/whois <player>", "show", "who");
    }


    public BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            throw invalidUsage();
        }
        String playername = args[0];
        ProxiedKookyPlayer online = ProxiedKookyPlayer.getObject(playername);
        if (online == null) {
            UUID u = RankCommand.getUUID(playername);
            if (u == null) {
                throw new CommandException("Player not found", this);
            }
            try {
                online = new ProxiedKookyPlayer(u, KookyBungee.getInstance().loadData(u));
            } catch (Exception e) {
                throw new CommandException("Player not found", this);
            }
        }
        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.RED + "All player information about " + online.getName()));
        String info = ChatColor.GOLD + "Whois: " + ChatColor.RED + online.getName();
        info += "\n" + ChatColor.GOLD + "Nickname: " + online.getNickName();
        info += "\n" + ChatColor.GOLD + "Tokens: " + String.valueOf(online.getTokens());
        info += "\n" + ChatColor.GOLD + "Rank: " + online.getRank().getName();
        info += "\n" + ChatColor.GOLD + "Subranks: ";
        for (Rank r : online.getSubRanks()) {
            info += r.getName();
        }
        BaseComponent[] baseComponents = TextComponent.fromLegacyText(info);
        for (BaseComponent b : baseComponents) {
            b.setHoverEvent(hoverEvent);
        }
        return baseComponents;
    }

}
