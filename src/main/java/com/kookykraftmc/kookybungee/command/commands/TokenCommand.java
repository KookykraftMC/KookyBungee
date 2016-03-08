package com.kookykraftmc.kookybungee.command.commands;

import com.kookykraftmc.api.global.data.PlayerData;
import com.kookykraftmc.api.global.sql.SQLConnection;
import com.kookykraftmc.api.global.sql.SQLUtil;
import com.kookykraftmc.kookybungee.KookyBungee;
import com.kookykraftmc.kookybungee.command.CommandException;
import com.kookykraftmc.kookybungee.command.SimpleCommand;
import com.kookykraftmc.kookybungee.player.ProxiedKookyPlayer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class TokenCommand extends SimpleCommand {

    protected static UUID getUUID(String name) {
        ProxiedPlayer player;
        if ((player = ProxyServer.getInstance().getPlayer(name)) != null) {
            return player.getUniqueId();
        }
        SQLConnection connection = KookyBungee.getInstance().getConnection();
        final String s = "`key`=\"" + PlayerData.NAME + "\" AND `value`=\"" + name + "\"";
        final String s2 = "`key`=\"" + PlayerData.NICKNAME + "\" AND `value`=\"" + name + "\"";
        ResultSet set = null;
        try {
            set = SQLUtil.query(connection, PlayerData.table, "uuid", new SQLUtil.Where(null) {
                @Override
                public String getWhere() {
                    return s;
                }
            });
            if (set.next()) {
                return UUID.fromString(set.getString("uuid"));
            }
            set.close();
            set = SQLUtil.query(connection, PlayerData.table, "uuid", new SQLUtil.Where(null) {
                @Override
                public String getWhere() {
                    return s2;
                }
            });
            if (set.next()) {
                return UUID.fromString(set.getString("uuid"));
            }
        } catch (SQLException | ClassNotFoundException ex) {
            return null;
        } finally {
            if (set != null) {
                try {
                    set.close();
                } catch (Exception ex) {

                }
            }
        }
        return null;
    }


    public TokenCommand() {
        super("tokens", null, "/tokens [other]", "token", "gettokens");
    }

    public BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException {
        TextComponent c;
        TextComponent amt;
        if (args.length == 0 && sender instanceof ProxiedPlayer) {
            ProxiedPlayer proxiedPlayer = (ProxiedPlayer) sender;
            ProxiedKookyPlayer kookyPlayer = ProxiedKookyPlayer.getObject(proxiedPlayer.getUniqueId());
            if (kookyPlayer == null) {
                throw new CommandException("Your not online!", this);
            }
            c = new TextComponent("Your tokens: ");
            c.setColor(ChatColor.GOLD);
            c.setUnderlined(true);
            c.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.GOLD + "The current amount of tokens you have")));
            amt = new TextComponent(String.valueOf(kookyPlayer.getTokens()));
            amt.setColor(ChatColor.RED);
        } else {
            if (args.length == 0) {
                throw invalidUsage();
            }
            String other = args[0];
            ProxiedKookyPlayer target = ProxiedKookyPlayer.getObject(other);
            if (target == null) {
                UUID u = getUUID(other);
                if (u == null) {
                    throw new CommandException("Player not found", this);
                }
                try {
                    target = new ProxiedKookyPlayer(u, KookyBungee.getInstance().loadData(u));
                } catch (Exception e) {
                    throw new CommandException("Player not found", this);
                }
            }
            c = new TextComponent(target.getNickName() + "\'s tokens: ");
            c.setColor(ChatColor.GOLD);
            c.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.GOLD + "The current amount of tokens " + target.getNickName() + " has")));
            amt = new TextComponent(String.valueOf(target.getTokens()));
            amt.setColor(ChatColor.RED);
        }
        return new BaseComponent[]{c, amt};
    }

}
