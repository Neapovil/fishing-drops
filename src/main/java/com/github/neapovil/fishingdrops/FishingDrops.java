package com.github.neapovil.fishingdrops;

import org.bukkit.plugin.java.JavaPlugin;

public class FishingDrops extends JavaPlugin
{
    private static FishingDrops instance;

    @Override
    public void onEnable()
    {
        instance = this;
    }

    @Override
    public void onDisable()
    {
    }

    public static FishingDrops getInstance()
    {
        return instance;
    }
}
