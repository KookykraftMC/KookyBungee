package com.kookykraftmc.kookybungee.servermanager;

import com.kookykraftmc.api.global.kookypackets.PacketInfo;
import com.kookykraftmc.api.global.type.ServerType;
import com.kookykraftmc.kookybungee.KookyBungee;
import de.mickare.xserver.net.XServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.logging.Level;

public class ServerManager {

    private KookyBungee bungee;

    private Set<KookyServer> servers = new HashSet<>();
    private Set<PacketInfo> unassigned = new HashSet<>();

    public ServerManager(KookyBungee bungee) {
        this.bungee = bungee;
    }

    protected KookyBungee getBungee() {
        return bungee;
    }

    public ServerType getType(ServerInfo info) {
        for (ServerType wrapper : ServerType.getTypes()) {
            if (info.getName().startsWith(wrapper.getPrefix())) {
                return wrapper;
            }
        }
        throw new IllegalArgumentException("No servertype found for " + info.getName());
    }

    public int getID(ServerInfo info, ServerType wrapper) {
        try {
            return Integer.parseInt(info.getName().replace(wrapper.getPrefix(), ""));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Could not format information", ex);
        }
    }

    public KookyServer getServer(String name) {
        for (KookyServer server : servers) {
            if (server.getName().equalsIgnoreCase(name)) {
                return server;
            }
        }
        return null;
    }

    public Set<PacketInfo> getUnassigned() {
        return unassigned;
    }

    public Set<KookyServer> getServers() {
        return servers;
    }

    public KookyServer load(XServer xserver, ServerInfo info) {
        removeUnassigned(xserver);
        KookyServer server = new KookyServer(info, xserver);
        servers.add(server);
        return server;
    }

    public void removeUnassigned(XServer server) {
        Iterator<PacketInfo> infoIterator = getUnassigned().iterator();
        while (infoIterator.hasNext()) {
            PacketInfo packetInfo = infoIterator.next();
            if (packetInfo.getServer().getName().equals(server.getName())) {
                infoIterator.remove();
            }
        }
    }

    public KookyServer create(XServer server, ServerType wrapper, int id) {
        removeUnassigned(server);
        InetSocketAddress address = new InetSocketAddress(server.getHost(), Integer.parseInt(server.getName()) + 10000);
        return KookyServer.create(server, address, wrapper, id);
    }

    protected void register(KookyServer server) {
        if (!servers.contains(server)) {
            servers.add(server);
        }
    }

    protected void remove(KookyServer server) {
        servers.remove(server);
    }

    public KookyServer getAvailble(ServerType type, boolean joinable, boolean playercount) {
        if (type == null) {
            throw new IllegalArgumentException("Type cannot be null");
        }
        for (KookyServer server : getServers()) {
            if (server.getType() != null && type.getName().equals(server.getType().getName()) && (!joinable || server.isJoinable()) && (!playercount || server.getPlayercount() < server.getMaxplayercount())) {
                return server;
            }
        }
        return null;
    }


    public int getNewID(ServerType type) {
        int i = 0;
        KookyServer server;
        do {
            i++;
            server = getServer(type, i);
        } while (server != null);
        return i;
    }

    public ServerType getNeeded() throws UneededException {
        Map<String, Integer> map = new HashMap<>();
        for (ServerType type : ServerType.getTypes()) {
            map.put(type.getName(), 0);
        }
        for (KookyServer server : getServers()) {
            if (server.getType() != null) {
                map.put(server.getType().getName(), map.get(server.getType().getName()) + 1);
            } else {
                getBungee().getPlugin().getLogger().log(Level.WARNING, "{0} doesn't have a server type!", new Object[]{server.getServer().getName()});
            }
        }
        List<ServerType> needed = new ArrayList<>();
        List<ServerType> softneeded = new ArrayList<>();
        for (ServerType type : ServerType.getTypes()) {
            int current = map.get(type.getName());
            if (type.getLowlimit() > current) {
                needed.add(type);
            }
            if (type.getHighlimit() > current) {
                softneeded.add(type);
            }
        }
        if (!needed.isEmpty()) {
            Collections.shuffle(needed);
            return needed.get(0);
        }
        if (!softneeded.isEmpty()) {
            Collections.shuffle(softneeded);
            return softneeded.get(0);
        }
        throw new UneededException();
    }

    public KookyServer getServer(XServer xserver) {
        for (KookyServer server : servers) {
            if (server.getServer().getName().equals(xserver.getName())) {
                return server;
            }
        }
        return null;
    }

    public KookyServer getServer(ServerType type, int id) {
        for (KookyServer server : servers) {
            if (server.getType() == type && server.getId() == id) {
                return server;
            }
        }
        return null;
    }

    public KookyServer getServer(ServerInfo info) {
        for (KookyServer server : servers) {
            if (server.getInfo().getAddress() == info.getAddress()) {
                return server;
            }
        }
        return null;
    }

    class UneededException extends Exception {
        public UneededException() {

        }
    }

}
