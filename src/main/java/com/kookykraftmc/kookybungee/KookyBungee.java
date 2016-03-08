package com.kookykraftmc.kookybungee;

import com.kookykraftmc.api.global.kookypackets.messaging.messages.handshake.RankDataUpdate;
import com.kookykraftmc.api.global.kookypackets.messaging.messages.response.PlayerDataResponse;
import com.kookykraftmc.api.global.data.DataObject;
import com.kookykraftmc.api.global.data.PlayerData;
import com.kookykraftmc.api.global.data.RankData;
import com.kookykraftmc.api.global.player.KookyPlayer;
import com.kookykraftmc.api.global.plugin.KookyHubObject;
import com.kookykraftmc.api.global.ranks.Rank;
import com.kookykraftmc.api.global.sql.SQLUtil;
import com.kookykraftmc.kookybungee.command.ICommand;
import com.kookykraftmc.kookybungee.command.commands.*;
import com.kookykraftmc.kookybungee.servermanager.KookyServer;
import com.kookykraftmc.kookybungee.servermanager.ServerManager;
import de.mickare.xserver.XServerPlugin;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KookyBungee extends KookyHubObject<Plugin> implements IKookyBungee  {

    private static final int VERSION = 12;

    public static IKookyBungee getInstance() {
        return instance;
    }

    public static void setInstance(IKookyBungee instance) {
        KookyBungee.instance = instance;
    }

    private static IKookyBungee instance;
    private ServerManager manager;
    private KookyListener listener;
    private KookyPlugin plugin;
    private KookyPlugman pluginManager;
    private File file;

    public KookyBungee(KookyPlugin plugin) {
        super();
        this.plugin = plugin;
    }

    public void onKookyEnable() {
        setInstance(this);

        file = getPlugin().getFile();

        logInfo("Loading ranks...");

        try {
            loadRanks();
        } catch (Exception e) {
            logSevere(e.getMessage());
            endSetup("Failed to load ranks");
        }

        logInfo("Loaded ranks");

        logInfo("Setting up components");

        manager = new ServerManager(this);
        listener = new KookyListener(this);
        getPacketHub().registerListener(listener);
        getPlugin().getProxy().getPluginManager().registerListener(getPlugin(), listener);

        logInfo("Components are set up");

        logInfo("Creating commands");

        registerCommand(new PlugmanCommand(getPlugman()));
        registerCommand(new ReloadCommand("b", getPlugman()));
        registerCommand(new FriendCommand());
        registerCommand(new RankCommand());
        registerCommand(new WhoisCommand());
        registerCommand(new TokenCommand());
        registerCommand(new MessageCommand());
        registerCommand(new SetTokenCommand());
        registerCommand(new PartyCommand());
        registerCommand(new HelpCommand());

        logInfo("Commands have been created");

        logInfo("Finished setup");
    }

    public void registerCommand(ICommand command) {
        if (command instanceof Command) {
            getPlugin().getProxy().getPluginManager().registerCommand(getPlugin(), (Command) command);
        } else {
            throw new IllegalArgumentException(command.getClass().getName() + " is not a bungeecord command!");
        }
    }

    public void onKookyDisable() {
        for (Rank r : Rank.getRanks()) {
            try {
                r.getData().save("ranks", "rank", r.getName());
            } catch (SQLException | ClassNotFoundException e) {
                logSevere(e.getMessage());
                logSevere("Error saving rank " + r.getName());
            }
        }
        setInstance(null);
        manager = null;
    }


    public PlayerData loadData(UUID load) throws SQLException, ClassNotFoundException {
        return new PlayerData(DataObject.loadData(SQLUtil.query(getConnection(), PlayerData.table, "*", new SQLUtil.WhereVar("uuid", load))));
    }

    public ServerManager getManager() {
        return manager;
    }

    public KookyPlugin getPlugin() {
        return plugin;
    }

    public void saveXServerDefaults() {

        logInfo("Finding XServer folders...");

        File xserverfoler = new File("plugins" + File.separator + "XServerProxy");
        if (!xserverfoler.exists()) {
            logInfo("Creating XServer folder");
            xserverfoler.mkdir();
        }

        logInfo("Finding XServer configuration...");

        File xserverconfig = new File(xserverfoler + File.separator + "config.yml");
        if (!xserverconfig.exists()) {
            try {
                xserverconfig.createNewFile();
            } catch (IOException ex) {
                logSevere(ex.getMessage());
                endSetup("Could not create XServer configuration");
            }
        }

        logInfo("Loading XServer configuration");

        Configuration c;
        try {
            c = YamlConfiguration.getProvider(YamlConfiguration.class).load(xserverconfig);
        } catch (IOException e) {
            logSevere(e.getMessage());
            endSetup("Could not load XServer config");
            return;
        }
        c.set("servername", "proxy");
        c.set("mysql.User", getConnection().getUser());
        c.set("mysql.Pass", getConnection().getPassword());
        c.set("mysql.Data", getConnection().getDatabase());
        c.set("mysql.Host", getConnection().getHostname());
        c.set("mysql.Port", getConnection().getPort());
        c.set("mysql.TableXServers", "xserver_servers");
        c.set("mysql.TableXGroups", "xserver_groups");
        c.set("mysql.TableXServersGroups", "xserver_servergroups");

        logInfo("Loaded XServer configuration");

        logInfo("Saving XServer configuration");

        try {
            YamlConfiguration.getProvider(YamlConfiguration.class).save(c, xserverconfig);
        } catch (IOException e) {
            logSevere(e.getMessage());
            endSetup("Could not save XServer config");
        }

        logInfo("Saved XServer configuration");
    }

    public void onKookyLoad() {
        pluginManager = new KookyPlugman(getPlugin().getProxy());
    }

    public ProxiedPlayer getPlayer(UUID uuid) {
        return getPlugin().getProxy().getPlayer(uuid);
    }

    public void updateRank(Rank r) {
        RankDataUpdate update = new RankDataUpdate(r.getName(), r.getData().getRaw());
        for (KookyServer server : getManager().getServers()) {
            try {
                getPacketHub().sendMessage(server.getServer(), update);
            } catch (IOException e) {
                logSevere(e.getMessage());
                logSevere("Error sending rank update message to " + server);
            }
        }
    }

    public void updatePlayer(KookyPlayer player) {
        PlayerDataResponse response = new PlayerDataResponse(player.getName(), player.getData().getRaw());
        ProxiedPlayer proxiedPlayer = getPlugin().getProxy().getPlayer(player.getUUID());
        if (proxiedPlayer != null) {
            ServerInfo info = proxiedPlayer.getServer().getInfo();
            KookyServer server = getManager().getServer(info);
            if (server != null) {
                try {
                    getPacketHub().sendMessage(server.getServer(), response);
                } catch (IOException e) {
                    logSevere("Error sending packet to update: " + e.getMessage());
                }
            }
        }
    }

    public void endSetup(String s) throws RuntimeException {
        getPlugin().getProxy().stop(s);
        throw new IllegalArgumentException(s);
    }

    public Logger getLogger() {
        if (getPlugin() != null) {
            return getPlugin().getLogger();
        }
        return null;
    }

    public void logInfo(String s) {
        Logger l = getLogger();
        if (l != null) {
            l.info(s);
        } else {
            System.out.println("[BubbleBungee] " + s);
        }
    }

    public void logSevere(String s) {
        Logger l = getLogger();
        if (l != null) {
            l.severe(s);
        } else {
            System.err.println("[BubbleBungee] " + s);
        }
    }

    public void runTaskLater(Runnable runnable, long l, TimeUnit unit) {
        getPlugin().getProxy().getScheduler().schedule(getPlugin(), runnable, l, unit);
    }

    public void loadRanks() throws SQLException, ClassNotFoundException {
        Rank.getRanks().clear();
        if(!SQLUtil.tableExists(getConnection(),"ranks")){
            getLogger().log(Level.INFO,"Rank table does not exist, creating...");
            getConnection().executeSQL("" +
                    "CREATE TABLE `ranks` (" +
                    "`rank` VARCHAR(32) NOT NULL DEFAULT 'default'," +
                    "`value` TEXT NOT NULL," +
                    "`key` TEXT NOT NULL," +
                    "INDEX `rank` (`rank`)" +
                    ");");
            RankData defaultrank = new RankData(new HashMap<String,String>());
            defaultrank.set("default",true);
            Rank.loadRank("default",defaultrank.getRaw());
            getLogger().log(Level.INFO,"Created ranks table");
        }
        ResultSet set = SQLUtil.query(getConnection(), "ranks", "*", new SQLUtil.Where("1"));
        Map<String, Map<String, String>> map = new HashMap<>();
        while (set.next()) {
            String rankname = set.getString("rank");
            Map<String, String> currentmap = map.containsKey(rankname) ? map.get(rankname) : new HashMap<String, String>();
            currentmap.put(set.getString("key"), set.getString("value"));
            map.put(rankname, currentmap);
        }
        set.close();
        for (Map.Entry<String, Map<String, String>> entry : map.entrySet()) {
            Rank.loadRank(entry.getKey(), entry.getValue());
            logInfo("Loaded rank: " + entry.getKey());
        }
    }

    public XServerPlugin getXPlugin() {
        Plugin p = getPlugin().getProxy().getPluginManager().getPlugin("XServerProxy");
        if (p == null) {
            endSetup("Could not find XServerProxy");
        }
        return (XServerPlugin) p;
    }

    public KookyListener getListener() {
        return listener;
    }

    public File getReplace() {
        return file;
    }

    public String getArtifact() {
        return getPlugin().getDescription().getName();
    }

    public int getVersion() {
        return VERSION;
    }

    public boolean bungee() {
        return true;
    }

    public KookyPlugman getPlugman() {
        return pluginManager;
    }

    public void update(Runnable r) {
        runTaskLater(r, 1L, TimeUnit.SECONDS);
    }

    public void updateTaskBefore() {
        getPlugman().unload(getPlugin());
    }

    public void updateTaskAfter() {
        getPlugman().load(file);
    }

}
