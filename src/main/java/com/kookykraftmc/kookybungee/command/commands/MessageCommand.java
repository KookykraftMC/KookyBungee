package com.kookykraftmc.kookybungee.command.commands;

import com.google.common.base.Joiner;
import com.kookykraftmc.api.global.java.ArgTrimmer;
import com.kookykraftmc.api.global.ranks.Rank;
import com.kookykraftmc.kookybungee.KookyBungee;
import com.kookykraftmc.kookybungee.command.CommandException;
import com.kookykraftmc.kookybungee.command.SimpleCommand;
import com.kookykraftmc.kookybungee.player.ProxiedKookyPlayer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Date;

public class MessageCommand extends SimpleCommand {

    public MessageCommand() {
        super("msg", null, "/msg <player> <message>", "message", "mail", "tell", "whisper", "t", "m");
    }

    public BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException {
        if (args.length < 2) {
            throw invalidUsage();
        }
        String target = args[0];
        ProxiedKookyPlayer player = ProxiedKookyPlayer.getObject(target);
        if (player == null) {
            throw new CommandException("Player not found", this);
        }
        String message = Joiner.on(" ").join(new ArgTrimmer<>(String.class, args).trim(1));
        TextComponent space = new TextComponent(" ");
        TextComponent senderprefix = new TextComponent("[You -> " + player.getNickName() + "]");
        String name = sender.getName();
        if (sender instanceof ProxiedPlayer) {
            ProxiedKookyPlayer senderplayer = ProxiedKookyPlayer.getObject(((ProxiedPlayer) sender).getUniqueId());
            name = senderplayer.getNickName();
        }
        TextComponent playerprefix = new TextComponent("[" + name + " -> You]");
        playerprefix.setColor(ChatColor.GOLD);
        senderprefix.setColor(ChatColor.GOLD);
        HoverEvent messageprefixhover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.GOLD + "Send a message with " + getUsage()));
        playerprefix.setHoverEvent(messageprefixhover);
        senderprefix.setHoverEvent(messageprefixhover);
        playerprefix.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + name + " "));
        senderprefix.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + player.getNickName() + " "));


        String ranks = player.getRank().getName();
        for (Rank r : player.getSubRanks()) {
            ranks += ", " + r.getName();
        }

        HoverEvent sendermessageinfo = new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Name: " + ChatColor.GRAY + player.getName() +
                "\nSent at " + ChatColor.GRAY + KookyBungee.getInstance().getListener().format.format(new Date()) +
                "\nRank: " + ChatColor.GRAY + ranks));
        TextComponent messagetext = new TextComponent(message);
        messagetext.setHoverEvent(sendermessageinfo);

        if (player.getPlayer() != null) {
            player.getPlayer().sendMessage(playerprefix, space, messagetext);
        }
        return new BaseComponent[]{senderprefix, space, messagetext};
    }

}
