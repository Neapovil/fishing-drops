package com.github.neapovil.fishingdrops.command;

import java.util.Arrays;

import org.bukkit.block.Biome;

import com.github.neapovil.fishingdrops.FishingDrops;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.BiomeArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.SafeSuggestions;

public class RemoveCommand implements ICommand
{
    @Override
    public void register()
    {
        new CommandAPICommand(this.commandName())
                .withPermission(this.permission())
                .withArguments(new LiteralArgument("remove"))
                .withArguments(new BiomeArgument("biome").replaceSafeSuggestions(SafeSuggestions.suggest(info -> {
                    final FishingDrops plugin = FishingDrops.getInstance();

                    return Arrays.asList(Biome.values())
                            .stream()
                            .filter(i -> plugin.getDropsManager().hasBiome(i))
                            .toArray(Biome[]::new);
                })))
                .executes((sender, args) -> {
                    final Biome biome = (Biome) args[0];

                    final FishingDrops plugin = FishingDrops.getInstance();

                    if (!plugin.getDropsManager().hasBiome(biome))
                    {
                        throw CommandAPI.fail("Add the biome first before trying to edit its loot table!");
                    }

                    plugin.getDropsManager().removeBiome(biome);

                    sender.sendMessage("Biome deleted: " + biome.toString());
                })
                .register();
    }
}
