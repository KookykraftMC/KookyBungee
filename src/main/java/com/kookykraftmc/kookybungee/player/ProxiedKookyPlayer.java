package com.kookykraftmc.kookybungee.player;

import com.kookykraftmc.api.global.data.InvalidBaseException;
import com.kookykraftmc.api.global.data.PlayerData;
import com.kookykraftmc.api.global.player.KookyPlayer;
import com.kookykraftmc.kookybungee.KookyBungee;
import com.kookykraftmc.kookybungee.party.Party;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.SQLException;
import java.util.UUID;

public class ProxiedKookyPlayer extends KookyPlayer<ProxiedPlayer> {

    public static ProxiedKookyPlayer getObject(UUID u) {
        return (ProxiedKookyPlayer) getPlayerObjectMap().get(u);
    }

    public static ProxiedKookyPlayer getObject(String name) {
        for (KookyPlayer player : getPlayerObjectMap().values()) {
            if (player instanceof ProxiedKookyPlayer) {
                ProxiedKookyPlayer proxiedBubblePlayer = (ProxiedKookyPlayer) player;
                try {
                    if (proxiedBubblePlayer.getName().equalsIgnoreCase(name)) {
                        return proxiedBubblePlayer;
                    }
                } catch (UnsupportedOperationException ex) {
                }
            }
        }
        for (KookyPlayer player : getPlayerObjectMap().values()) {
            if (player instanceof ProxiedKookyPlayer) {
                ProxiedKookyPlayer proxiedBubblePlayer = (ProxiedKookyPlayer) player;
                try {
                    if (proxiedBubblePlayer.getNickName() != null && proxiedBubblePlayer.getNickName().equalsIgnoreCase(name)) {
                        return proxiedBubblePlayer;
                    }
                } catch (UnsupportedOperationException ex) {
                }
            }
        }
        return null;
    }

    private String name;
    private Party party = null;

    public ProxiedKookyPlayer(UUID u, PlayerData data) {
        super(u, data);
    }

    public String getName() {
        if (name == null) {
            if (getPlayer() != null) {
                setName(getPlayer().getName());
            } else {
                try {
                    setName(getData().getString(PlayerData.NAME));
                } catch (InvalidBaseException e) {
                    throw new UnsupportedOperationException(e);
                }
            }
        }
        return name;
    }

    public void setName(String name) {
        this.name = name;
        getData().set(PlayerData.NAME, name);
    }

    @Override
    public void update() {
        KookyBungee.getInstance().updatePlayer(this);
    }

    public void save() {
        try {
            getData().save(PlayerData.table, "uuid", getUUID());
        } catch (SQLException | ClassNotFoundException e) {
            KookyBungee.getInstance().logSevere(e.getMessage());
            KookyBungee.getInstance().logSevere("Could not save data of " + getName());
        }
    }

    public Party getParty() {
        return party;
    }

    public void setParty(Party party) {
        if (this.party != null) {
            if (this.party.isLeader(getPlayer())) {
                this.party.disband(getNickName() + " disbanded the party");
            } else if (this.party.isMember(getPlayer())) {
                this.party.removeMember(getPlayer(), getNickName() + " left the party");
            } else if (this.party.isInvited(getPlayer())) {
                this.party.cancelInvite(getPlayer(), getNickName() + " denied the invite");
            }
        }
        this.party = party;
    }

}
