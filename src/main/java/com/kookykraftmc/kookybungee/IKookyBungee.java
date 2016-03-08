package com.kookykraftmc.kookybungee;

import com.kookykraftmc.api.global.data.PlayerData;
import com.kookykraftmc.api.global.player.KookyPlayer;
import com.kookykraftmc.api.global.plugin.KookyHub;
import com.kookykraftmc.api.global.ranks.Rank;
import com.kookykraftmc.kookybungee.servermanager.ServerManager;
import net.md_5.bungee.api.plugin.Plugin;

import java.sql.SQLException;
import java.util.UUID;

public interface IKookyBungee extends KookyHub<Plugin> {

    KookyPlugin getPlugin();

    PlayerData loadData(UUID load) throws SQLException, ClassNotFoundException;

    void updateRank(Rank r);

    void updatePlayer(KookyPlayer p);

    KookyListener getListener();

    ServerManager getManager();

    KookyPlugman getPlugman();

}
