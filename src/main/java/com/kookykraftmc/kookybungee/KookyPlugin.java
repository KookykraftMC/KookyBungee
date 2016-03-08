package com.kookykraftmc.kookybungee;

import net.md_5.bungee.api.plugin.Plugin;

public class KookyPlugin extends Plugin {

    private IKookyBungee kookyBungee;

    public KookyPlugin() {
        super();
        kookyBungee = new KookyBungee(this);
    }

    public void onLoad() {
        kookyBungee.onLoad();
    }

    public void onEnable() {
        kookyBungee.onEnable();
    }

    public void onDisable() {
        kookyBungee.onDisable();
    }

}
