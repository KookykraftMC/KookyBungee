package com.kookykraftmc.kookybungee.party;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Party {

    public static TextComponent prefix = new TextComponent("[Party] ");

    static {
        prefix.setColor(ChatColor.RED);
        prefix.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.RED + "Party party party!")));
    }

    private UUID leader;
    private Set<UUID> members = new HashSet<>();
    private Set<UUID> invited = new HashSet<>();

    public Party(ProxiedPlayer leader) {
        this.leader = leader.getUniqueId();
    }

    public UUID getLeader() {
        return leader;
    }

    public Set<UUID> getInvited() {
        return invited;
    }

    public Set<UUID> getMembers() {
        return members;
    }

    public void disband(String reason) {
        TextComponent invitecancelled = new TextComponent("Your invite has been cancelled");
        invitecancelled.setColor(ChatColor.GOLD);
        broadcastToInvitees(new BaseComponent[]{prefix, invitecancelled});
        invited.clear();
        TextComponent disbandmsg = new TextComponent(reason);
        disbandmsg.setColor(ChatColor.GOLD);
        broadcast(new BaseComponent[]{prefix, disbandmsg});
    }

    public void invite(ProxiedPlayer invite, String reason) {
        if (isInvited(invite)) {
            throw new IllegalArgumentException("Already invited");
        }
        if (isMember(invite)) {
            throw new IllegalArgumentException("Already a member");
        }
        invited.add(invite.getUniqueId());
        TextComponent c = new TextComponent(reason);
        c.setColor(ChatColor.GOLD);
        broadcast(new BaseComponent[]{prefix, c});
    }

    public void cancelInvite(ProxiedPlayer invite, String reason) {
        if (!isInvited(invite)) {
            throw new IllegalArgumentException("Not invited");
        }
        if (isMember(invite)) {
            throw new IllegalArgumentException("Already a member");
        }
        TextComponent c = new TextComponent(reason);
        c.setColor(ChatColor.GOLD);
        broadcast(new BaseComponent[]{prefix, c});
    }

    public void addMember(ProxiedPlayer member, String reason) {
        if (!isInvited(member)) {
            throw new IllegalArgumentException("Not invited");
        }
        if (isMember(member)) {
            throw new IllegalArgumentException("Already a member");
        }
        invited.remove(member.getUniqueId());
        members.add(member.getUniqueId());
        TextComponent c = new TextComponent(reason);
        c.setColor(ChatColor.GOLD);
        broadcast(new BaseComponent[]{prefix, c});
    }

    public void removeMember(ProxiedPlayer member, String reason) {
        if (!isMember(member)) {
            throw new IllegalArgumentException("Not a member");
        }
        if (isLeader(member)) {
            throw new IllegalArgumentException("Cannot disband when leader");
        }
        getMembers().remove(member.getUniqueId());
        TextComponent c = new TextComponent(reason);
        c.setColor(ChatColor.GOLD);
        broadcast(new BaseComponent[]{prefix, c});
    }

    public boolean isInvited(ProxiedPlayer player) {
        return getInvited().contains(player.getUniqueId());
    }

    public boolean isMember(ProxiedPlayer player) {
        return getMembers().contains(player.getUniqueId());
    }

    public boolean isLeader(ProxiedPlayer player) {
        return getLeader() == player.getUniqueId();
    }

    public void broadcast(BaseComponent[] baseComponents) {
        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            if (isMember(player)) {
                player.sendMessage(ChatMessageType.CHAT, baseComponents);
            }
        }
    }

    public void broadcastToInvitees(BaseComponent[] baseComponents) {
        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            if (isInvited(player)) {
                player.sendMessage(ChatMessageType.CHAT, baseComponents);
            }
        }
    }

}
