package com.kookykraftmc.kookybungee.command.commands;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.kookykraftmc.api.global.data.InvalidBaseException;
import com.kookykraftmc.api.global.data.PlayerData;
import com.kookykraftmc.api.global.data.RankData;
import com.kookykraftmc.api.global.java.ArgTrimmer;
import com.kookykraftmc.api.global.ranks.Rank;
import com.kookykraftmc.api.global.sql.SQLConnection;
import com.kookykraftmc.api.global.sql.SQLUtil;
import com.kookykraftmc.kookybungee.KookyBungee;
import com.kookykraftmc.kookybungee.command.BaseCommand;
import com.kookykraftmc.kookybungee.command.CommandException;
import com.kookykraftmc.kookybungee.command.ICommand;
import com.kookykraftmc.kookybungee.command.SubCommand;
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
import java.util.*;

public class RankCommand extends BaseCommand {

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

    private static void saveRank(Rank r) {
        KookyBungee.getInstance().updateRank(r);
    }

    public RankCommand() {
        super("rankmanager", "rankmanager.use", new ImmutableSet.Builder<ICommand>().add(new SubCommand("list", "rankmanager.list", "list", "listranks", "groups", "ranks") {
            public BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException {
                ImmutableList.Builder<BaseComponent> componentBuilder = new ImmutableList.Builder<>();
                TextComponent component = new TextComponent("All ranks:");
                component.setColor(ChatColor.GOLD);
                component.setBold(true);
                componentBuilder.add(component);
                for (Rank r : Rank.getRanks()) {
                    TextComponent rankcomponent = new TextComponent("\n - " + r.getName());
                    rankcomponent.setColor(ChatColor.GOLD);
                    rankcomponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(r.getPrefix())));
                    componentBuilder.add(rankcomponent);
                }
                return componentBuilder.build().toArray(new BaseComponent[0]);
            }
        }).add(new SubCommand("setprefix", "rankmanager.prefix", "setprefix <rank> <prefix>", "prefix") {
            public BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException {
                if (args.length < 2) {
                    throw invalidUsage();
                }
                String rankname = args[0];
                Rank r = Rank.getRank(rankname);
                if (r == null) {
                    throw new CommandException("Rank not found", this);
                }
                String prefix = Joiner.on(" ").join(new ArgTrimmer<>(String.class, args).trim(1)).replace("%20", " ");
                r.setPrefix(prefix);
                saveRank(r);
                return TextComponent.fromLegacyText(ChatColor.GOLD + "Successfuly set the prefix of \'" + r.getName() + "\' to \'" + ChatColor.translateAlternateColorCodes('&', prefix) + ChatColor.GOLD + "\'");
            }
        }).add(new SubCommand("setsuffix", "rankmanager.suffix", "setsuffix <rank> <suffix>", "suffix") {
            public BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException {
                if (args.length < 2) {
                    throw invalidUsage();
                }
                String rankname = args[0];
                Rank r = Rank.getRank(rankname);
                if (r == null) {
                    throw new CommandException("Rank not found", this);
                }
                String suffix = Joiner.on(" ").join(new ArgTrimmer<>(String.class, args).trim(1)).replace("%20", " ");
                r.setSuffix(suffix);
                saveRank(r);
                return TextComponent.fromLegacyText(ChatColor.GOLD + "Successfuly set the suffix of \'" + r.getName() + "\' to \'" + ChatColor.translateAlternateColorCodes('&', suffix) + ChatColor.GOLD + "\'");
            }
        }).add(new SubCommand("setinheritance", "rankmanager.inheritance", "setinheritance <rank> <inheritance/none>", "inheritance") {
            public BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException {
                if (args.length < 2) {
                    throw invalidUsage();
                }
                String rankname = args[0];
                Rank r = Rank.getRank(rankname);
                if (r == null) {
                    throw new CommandException("Rank not found", this);
                }
                String toname = args[1];
                Rank toset;
                if (toname.equalsIgnoreCase("none")) {
                    toname = "none";
                    toset = null;
                } else if ((toset = Rank.getRank(toname)) == null) {
                    throw new CommandException("Inheritance rank does not exist", this);
                } else {
                    toname = toset.getName();
                }
                r.setInheritance(toset);
                saveRank(r);
                return TextComponent.fromLegacyText(ChatColor.GOLD + "Successfuly set the inheritance of \'" + r.getName() + "\' to \'" + toname + "\'");
            }
        }).add(new SubCommand("add", "rankmanager.addpermission", "add <rank> <permission>", "addpermission") {
            public BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException {
                if (args.length < 2) {
                    throw invalidUsage();
                }
                String rankname = args[0];
                Rank r = Rank.getRank(rankname);
                if (r == null) {
                    Rank.loadRank(rankname, new HashMap<String, String>());
                    r = Rank.getRank(rankname);
                    if (r == null) {
                        throw new CommandException("Rank not found", this);
                    }
                }
                String permission = args[1].toLowerCase();
                boolean haspermission = true;
                if (permission.startsWith("-")) {
                    haspermission = false;
                    permission = permission.substring(1);
                }
                if (permission.equalsIgnoreCase(RankData.PREFIX) || permission.equalsIgnoreCase(RankData.SUFFIX) || permission.equalsIgnoreCase(RankData.INHERITANCE)) {
                    throw new CommandException("Invalid permission", this);
                }
                r.getData().set(permission, haspermission);
                saveRank(r);
                return TextComponent.fromLegacyText(ChatColor.GOLD + "Successfuly set \'" + permission + " (" + String.valueOf(haspermission) + ") \' on rank \'" + r.getName() + "\'");
            }
        }).add(new SubCommand("remove", "rankmanager.removepermission", "remove <rank> <permission>", "removepermission") {
            public BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException {
                if (args.length < 2) {
                    throw invalidUsage();
                }
                String rankname = args[0];
                Rank r = Rank.getRank(rankname);
                if (r == null) {
                    throw new CommandException("Invalid rank", this);
                }
                String permission = args[1].toLowerCase();
                if (permission.startsWith("-")) {
                    permission = permission.substring(1);
                }
                if (permission.equalsIgnoreCase(RankData.PREFIX) || permission.equalsIgnoreCase(RankData.SUFFIX) || permission.equalsIgnoreCase(RankData.INHERITANCE)) {
                    throw new CommandException("Invalid permission", this);
                }
                try {
                    r.getData().getBoolean(permission);
                } catch (InvalidBaseException e) {
                    throw new CommandException("This rank does not have that permission", this);
                }
                r.getData().getRaw().remove(permission);
                saveRank(r);
                return TextComponent.fromLegacyText(ChatColor.GOLD + "Successfuly removed \'" + permission + "\' on rank \'" + r.getName() + "\'");
            }
        }).add(new SubCommand("info", "rankmanager.info", "info <rank>", "information") {
            public BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException {
                if (args.length == 0) {
                    throw invalidUsage();
                }
                String rankname = args[0];
                Rank r = Rank.getRank(rankname);
                if (r == null) {
                    throw new CommandException("Could not find a rank with this name", this);
                }
                HoverEvent event = new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.RED + "All rank information about " + r.getName()));
                String information = ChatColor.GOLD + "Rank information: " + ChatColor.RED + r.getName();
                information += "\n" + ChatColor.GOLD + "Prefix: \'" + ChatColor.translateAlternateColorCodes('&', r.getPrefix()) + ChatColor.GOLD + "\'";
                information += "\n" + ChatColor.GOLD + "Suffix: \'" + ChatColor.translateAlternateColorCodes('&', r.getSuffix()) + ChatColor.GOLD + "\'";
                information += "\n" + ChatColor.GOLD + "Inheritance: " + (r.getInheritance() != null ? r.getInheritance().getName() : "None");
                information += "\n" + ChatColor.GOLD + "Permissions: ";
                for (String key : r.getData().getRaw().keySet()) {
                    if (key.equalsIgnoreCase(RankData.PREFIX) || key.equalsIgnoreCase(RankData.SUFFIX) || key.equalsIgnoreCase(RankData.INHERITANCE)) {
                        continue;
                    }
                    boolean value;
                    try {
                        value = r.getData().getBoolean(key);
                    } catch (InvalidBaseException e) {
                        continue;
                    }
                    information += "\n" + ChatColor.GOLD + " - " + key + " (" + String.valueOf(value) + ")";
                }
                BaseComponent[] baseComponents = TextComponent.fromLegacyText(information);
                for (BaseComponent c : baseComponents) {
                    c.setHoverEvent(event);
                }
                return baseComponents;
            }
        }).add(new WhoisCommand()).add(new SubCommand("setrank", "rankmanager.setrank", "setrank <player> <rank>", "setgroup") {
            public BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException {
                if (args.length < 2) {
                    throw invalidUsage();
                }
                String playername = args[0];
                ProxiedKookyPlayer online = ProxiedKookyPlayer.getObject(playername);
                boolean forcesave = false;
                if (online == null) {
                    forcesave = true;
                    UUID u = getUUID(playername);
                    if (u == null) {
                        throw new CommandException("Player not found", this);
                    }
                    try {
                        online = new ProxiedKookyPlayer(u, KookyBungee.getInstance().loadData(u));
                    } catch (Exception e) {
                        throw new CommandException("Player not found", this);
                    }
                }
                String rankname = args[1];
                Rank r = Rank.getRank(rankname);
                if (r == null) {
                    throw new CommandException("Rank not found", this);
                }
                online.setRank(r);
                if (forcesave) {
                    online.save();
                }
                return TextComponent.fromLegacyText(ChatColor.GOLD + "Set the rank of \'" + online.getNickName() + "\' to \'" + r.getName() + "\'");
            }
        }).add(new SubCommand("addrank", "rankmanager.addsubrank", "addrank <player> <rank>", "addgroup") {
            public BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException {
                if (args.length < 2) {
                    throw invalidUsage();
                }
                String playername = args[0];
                ProxiedKookyPlayer online = ProxiedKookyPlayer.getObject(playername);
                boolean forcesave = false;
                if (online == null) {
                    forcesave = true;
                    UUID u = getUUID(playername);
                    if (u == null) {
                        throw new CommandException("Player not found", this);
                    }
                    try {
                        online = new ProxiedKookyPlayer(u, KookyBungee.getInstance().loadData(u));
                    } catch (Exception e) {
                        throw new CommandException("Player not found", this);
                    }
                }
                String rankname = args[1];
                Rank r = Rank.getRank(rankname);
                if (r == null) {
                    throw new CommandException("Rank not found", this);
                }
                List<Rank> rankList = new ArrayList<>();
                rankList.addAll(Arrays.asList(online.getSubRanks()));
                if (rankList.contains(r)) {
                    throw new CommandException("This player already has that subrank", this);
                }
                rankList.add(r);
                online.setSubRanks(rankList);
                if (forcesave) {
                    online.save();
                }
                return TextComponent.fromLegacyText(ChatColor.GOLD + "Added the subrank of \'" + online.getNickName() + "\' to \'" + r.getName() + "\'");
            }
        }).add(new SubCommand("removerank", "rankmanager.removesubrank", "removerank <player> <rank>", "removegroup") {
            public BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException {
                if (args.length < 2) {
                    throw invalidUsage();
                }
                String playername = args[0];
                ProxiedKookyPlayer online = ProxiedKookyPlayer.getObject(playername);
                boolean forcesave = false;
                if (online == null) {
                    forcesave = true;
                    UUID u = getUUID(playername);
                    if (u == null) {
                        throw new CommandException("Player not found", this);
                    }
                    try {
                        online = new ProxiedKookyPlayer(u, KookyBungee.getInstance().loadData(u));
                    } catch (Exception e) {
                        throw new CommandException("Player not found", this);
                    }
                }
                String rankname = args[1];
                Rank r = Rank.getRank(rankname);
                if (r == null) {
                    throw new CommandException("Rank not found", this);
                }
                List<Rank> rankList = new ArrayList<>();
                rankList.addAll(Arrays.asList(online.getSubRanks()));
                if (!rankList.contains(r)) {
                    throw new CommandException("This player does not have that subrank", this);
                }
                rankList.remove(r);
                online.setSubRanks(rankList);
                if (forcesave) {
                    online.save();
                }
                return TextComponent.fromLegacyText(ChatColor.GOLD + "Removed the subrank of \'" + online.getNickName() + "\' from \'" + r.getName() + "\'");
            }
        }).add(new SubCommand("deleterank", "rankmanager.deleterank", "deleterank <rank>", "deletegroup") {
            public BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException {
                if (args.length == 0) {
                    throw invalidUsage();
                }
                String rankname = args[0];
                Rank r = Rank.getRank(rankname);
                if (r == null) {
                    throw new CommandException("Invalid rank", this);
                }
                //Incase its the only rank
                if (r.isDefault() || Rank.getDefault() == r) {
                    throw new CommandException("You may not delete the default rank", this);
                }
                Rank.getRanks().remove(r);
                Rank fakerank = new Rank(r.getName(), new RankData(null));
                saveRank(fakerank);
                r.getData().getRaw().clear();
                try {
                    r.getData().save("ranks", "rank", r.getName());
                } catch (Exception e) {
                    throw new CommandException("Failed to save the deleted rank in SQL", this);
                }
                return TextComponent.fromLegacyText(ChatColor.GOLD + "Successfully deleted the rank \'" + r.getName() + "\'");
            }
        }).add(new SubCommand("createrank", "rankmanager.createrank", "createrank <rank>", "creategroup", "create") {
            public BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException {
                if (args.length == 0) {
                    throw invalidUsage();
                }
                String rankname = args[0];
                if (Rank.getRank(rankname) != null) {
                    throw new CommandException("This rank has already been created", this);
                }
                Map<String, String> map = new HashMap<>();
                map.put("default", String.valueOf(false));
                Rank.loadRank(rankname, map);
                return TextComponent.fromLegacyText(ChatColor.GOLD + "Successfully created a rank with the name \'" + rankname + "\'");
            }
        }).add(new SubCommand("setdefault", "rankmanager.setdefault", "setdefault <rank>") {
            public BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException {
                if (args.length == 0) {
                    throw invalidUsage();
                }
                String rankname = args[0];
                Rank r = Rank.getRank(rankname);
                if (r == null) {
                    throw new CommandException("Rank not found", this);
                }
                if (r.isDefault()) {
                    throw new CommandException("This rank is already default", this);
                }
                for (Rank otherrank : Rank.getRanks()) {
                    otherrank.getData().set("default", false);
                }
                r.getData().set("default", true);
                return TextComponent.fromLegacyText(ChatColor.GOLD + "Successfully changed the default rank to \'" + r.getName() + "\'");
            }
        }).build(), "groupmanager", "pex", "permissionsex", "ranks", "rmanager", "rank", "ranksmanager");
    }

}
