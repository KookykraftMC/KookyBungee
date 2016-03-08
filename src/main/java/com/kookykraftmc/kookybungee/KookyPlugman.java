package com.kookykraftmc.kookybungee;

import com.kookykraftmc.api.global.plugin.Plugman;
import com.google.common.collect.Multimap;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.*;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

public class KookyPlugman implements Plugman<Plugin> {

    private PluginManager pluginManager;
    private ProxyServer server;

    public KookyPlugman(ProxyServer server) {
        this.server = server;
        pluginManager = server.getPluginManager();
    }

    public void disable(Plugin plugin) {
        try {
            plugin.onDisable();
        } catch (Throwable throwable) {
            server.getLogger().log(Level.WARNING, "Error whilst disabling plugin ", throwable);
        }
        pluginManager.unregisterCommands(plugin);
        pluginManager.unregisterListeners(plugin);
        server.getScheduler().cancel(plugin);
        Map<String, Plugin> plugins = plugins();
        while (plugins.containsValue(plugin)) {
            plugins.values().remove(plugin);
        }
        setPluginManagerField("plugins", plugins);
        server.getLogger().log(Level.INFO, "Disabled {0}", new Object[]{plugin.getDescription().getName()});
    }

    public void enable(Plugin plugin) {
        try {
            plugin.onEnable();
        } catch (Throwable throwable) {
            server.getLogger().log(Level.WARNING, "Error whilst enabling plugin ", throwable);
        }
        server.getLogger().log(Level.INFO, "Enabled {0} v{1}", new Object[]{plugin.getDescription().getName(), plugin.getDescription().getVersion()});
    }

    public boolean isEnabled(Plugin plugin) {
        return plugins().containsValue(plugin);
    }

    public void unload(Plugin plugin) {
        String name = plugin.getDescription().getName();
        if (isEnabled(plugin)) {
            disable(plugin);
        }
        ClassLoader cl = plugin.getClass().getClassLoader();
        if (cl instanceof URLClassLoader) {
            try {
                ((URLClassLoader) cl).close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.gc();
        server.getLogger().log(Level.INFO, "Successfully unloaded {0}", new Object[]{name});
    }

    public Collection<Command> getCommands(Plugin p) {
        return commandsByPlugin().get(p);
    }

    public Plugin load(File file) {
        if (file.isFile() && file.getName().endsWith(".jar")) {
            PluginDescription description;
            try {
                JarFile ex = new JarFile(file);
                Throwable var7 = null;

                try {
                    JarEntry x2 = ex.getJarEntry("bungee.yml");
                    if (x2 == null) {
                        x2 = ex.getJarEntry("plugin.yml");
                    }

                    if (x2 == null) {
                        throw new IllegalArgumentException("Doesn't contain plugin.yml or bungee.yml!");
                    }
                    InputStream in = ex.getInputStream(x2);
                    Throwable var10 = null;

                    try {
                        description = yaml().loadAs(in, PluginDescription.class);
                        description.setFile(file);
                    } catch (Throwable var35) {
                        var10 = var35;
                        throw var35;
                    } finally {
                        if (in != null) {
                            if (var10 != null) {
                                try {
                                    in.close();
                                } catch (Throwable var34) {
                                    var10.addSuppressed(var34);
                                }
                            } else {
                                in.close();
                            }
                        }

                    }
                } catch (Throwable var37) {
                    var7 = var37;
                    throw var37;
                } finally {
                    if (ex != null) {
                        if (var7 != null) {
                            try {
                                ex.close();
                            } catch (Throwable var33) {
                                var7.addSuppressed(var33);
                            }
                        } else {
                            ex.close();
                        }
                    }

                }
            } catch (Exception var39) {
                server.getLogger().log(Level.WARNING, "Could not load plugin from file " + file, var39);
                throw new IllegalArgumentException(var39);
            }
            if (enablePlugin(pluginStatuses(), new Stack<PluginDescription>(), description)) {
                server.getLogger().log(Level.ALL, "Loaded plugin {0} v{1}", new Object[]{description.getName(), description.getVersion()});
                Plugin p = plugins().get(description.getName());
                p.onLoad();
                return p;
            } else {
                server.getLogger().log(Level.WARNING, "Could not load plugin {0} v{1}", new Object[]{description.getName(), description.getVersion()});
                throw new IllegalArgumentException("Error");
            }
        } else {
            throw new IllegalArgumentException("Must be a jar file");
        }
    }

    public Plugin get(String s) {
        return pluginManager.getPlugin(s);
    }

    private Object getPluginManagerField(String s) {
        try {
            Field f = PluginManager.class.getDeclaredField(s);
            f.setAccessible(true);
            return f.get(pluginManager);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private void setPluginManagerField(String s, Object o) {
        try {
            Field f = PluginManager.class.getDeclaredField(s);
            f.setAccessible(true);
            f.set(pluginManager, o);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private Method getPluginManagerMethod(String s, Class... classes) {
        try {
            Method m = PluginManager.class.getDeclaredMethod(s, classes);
            m.setAccessible(true);
            return m;
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private Map<PluginDescription, Boolean> pluginStatuses() {
        Map<PluginDescription, Boolean> pluginStatuses = new HashMap<>();
        for (Plugin p : plugins().values()) {
            pluginStatuses.put(p.getDescription(), true);
        }
        return pluginStatuses;
    }

    private Object doPluginManagerMethod(String s, Class[] classes, Object[] args) {
        Method m = getPluginManagerMethod(s, classes);
        try {
            return m.invoke(pluginManager, args);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private Yaml yaml() {
        return (Yaml) getPluginManagerField("yaml");
    }

    private Map<String, Command> commandMap() {
        return (Map<String, Command>) getPluginManagerField("commandMap");
    }

    private Map<String, Plugin> plugins() {
        return (Map<String, Plugin>) getPluginManagerField("plugins");
    }

    private Map<String, PluginDescription> toLoad() {
        return (Map<String, PluginDescription>) getPluginManagerField("toLoad");
    }

    private Multimap<Plugin, Command> commandsByPlugin() {
        return (Multimap<Plugin, Command>) getPluginManagerField("commandsByPlugin");
    }

    private Multimap<Plugin, Listener> listenersByPlugin() {
        return (Multimap<Plugin, Listener>) getPluginManagerField("listenersByPlugin");
    }

    private boolean enablePlugin(Map<PluginDescription, Boolean> pluginStatuses, Stack<PluginDescription> dependStack, PluginDescription plugin) {
        return (Boolean) doPluginManagerMethod("enablePlugin", new Class[]{Map.class, Stack.class, PluginDescription.class}, new Object[]{pluginStatuses, dependStack, plugin});
    }

    public Collection<Plugin> getPlugins() {
        return plugins().values();
    }

    public PluginManager getPluginManager() {
        return pluginManager;
    }

    public ProxyServer getServer() {
        return server;
    }

    public void reset_toLoad() {
        setPluginManagerField("toLoad", new HashMap<String, PluginDescription>());
    }

}
