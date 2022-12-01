package com.github.neapovil.fishingdrops;

import org.bukkit.plugin.java.JavaPlugin;

import com.electronwill.nightconfig.core.file.FileConfig;
import com.github.neapovil.fishingdrops.command.AddCommand;
import com.github.neapovil.fishingdrops.command.RemoveCommand;
import com.github.neapovil.fishingdrops.command.ModifyCommand;
import com.github.neapovil.fishingdrops.listener.Listener;
import com.github.neapovil.fishingdrops.manager.DropsManager;

public class FishingDrops extends JavaPlugin
{
    private static FishingDrops instance;
    private DropsManager dropsManager;

    @Override
    public void onEnable()
    {
        instance = this;

        this.getServer().getPluginManager().registerEvents(new Listener(), this);

        this.saveResource("drops.json", false);
        final FileConfig fileconfig = FileConfig.builder(this.getDataFolder().toPath().resolve("drops.json"))
                .autoreload()
                .autosave()
                .build();
        fileconfig.load();

        this.dropsManager = new DropsManager(fileconfig);

        new AddCommand().register();
        new ModifyCommand().register();
        new RemoveCommand().register();
    }

    @Override
    public void onDisable()
    {
    }

    public static FishingDrops getInstance()
    {
        return instance;
    }

    public DropsManager getDropsManager()
    {
        return this.dropsManager;
    }
}
