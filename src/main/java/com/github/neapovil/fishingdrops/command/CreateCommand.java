package com.github.neapovil.fishingdrops.command;

import java.util.Arrays;

import org.apache.commons.lang3.EnumUtils;
import org.bukkit.block.Biome;

import com.github.neapovil.fishingdrops.FishingDrops;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;

public class CreateCommand implements ICommand
{
    public void register()
    {
        new CommandAPICommand("fishingdrops")
                .withPermission("fishingdrops.command.admin")
                .withArguments(new LiteralArgument("create"))
                .withArguments(new StringArgument("biome").replaceSuggestions(ArgumentSuggestions.strings(info -> {
                    final FishingDrops plugin = FishingDrops.getInstance();

                    return Arrays.asList(Biome.values())
                            .stream()
                            .filter(i -> !i.equals(Biome.CUSTOM))
                            .filter(i -> !plugin.getDropsManager().hasBiome(i))
                            .map(i -> i.toString())
                            .toArray(String[]::new);
                })))
                .executes((sender, args) -> {
                    final String biomestring = ((String) args[0]).toUpperCase();

                    if (!EnumUtils.isValidEnum(Biome.class, biomestring))
                    {
                        throw CommandAPI.fail("This biome doesn't exist!");
                    }

                    final Biome biome = Biome.valueOf(biomestring);

                    if (biome.equals(Biome.CUSTOM))
                    {
                        throw CommandAPI.fail("Custom biomes are not supported yet!");
                    }

                    final FishingDrops plugin = FishingDrops.getInstance();

                    if (plugin.getDropsManager().hasBiome(biome))
                    {
                        throw CommandAPI.fail("This biome was already added!");
                    }

                    plugin.getDropsManager().create(biome);

                    sender.sendMessage("Biome drops created.");
                })
                .register();
    }
}
