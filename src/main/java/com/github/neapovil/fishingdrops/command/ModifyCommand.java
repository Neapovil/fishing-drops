package com.github.neapovil.fishingdrops.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.EnumUtils;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.github.neapovil.fishingdrops.FishingDrops;
import com.github.neapovil.fishingdrops.object.WeightedItem;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.ItemStackArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import dev.jorel.commandapi.arguments.SafeSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import net.kyori.adventure.text.Component;

public class ModifyCommand implements ICommand
{
    @Override
    public void register()
    {
        final List<Argument<?>> arguments = new ArrayList<>();

        arguments.add(new LiteralArgument("modify"));
        arguments.add(new StringArgument("biome").replaceSuggestions(ArgumentSuggestions.strings(info -> {
            final FishingDrops plugin = FishingDrops.getInstance();

            return Arrays.asList(Biome.values())
                    .stream()
                    .filter(i -> !i.equals(Biome.CUSTOM))
                    .filter(i -> plugin.getDropsManager().hasBiome(i))
                    .map(i -> i.toString())
                    .toArray(String[]::new);
        })));

        new CommandAPICommand("fishingdrops")
                .withPermission("fishingdrops.command.admin")
                .withArguments(arguments)
                .withArguments(new MultiLiteralArgument("addDropFromHand"))
                .executes(this::run)
                .register();

        new CommandAPICommand("fishingdrops")
                .withPermission("fishingdrops.command.admin")
                .withArguments(arguments)
                .withArguments(new MultiLiteralArgument("addDrop"))
                .withArguments(new ItemStackArgument("itemstack"))
                .executes(this::run)
                .register();

        new CommandAPICommand("fishingdrops")
                .withPermission("fishingdrops.command.admin")
                .withArguments(arguments)
                .withArguments(new MultiLiteralArgument("removeDrop"))
                .withArguments(new ItemStackArgument("itemstack").replaceSafeSuggestions(SafeSuggestions.suggest(info -> {
                    final FishingDrops plugin = FishingDrops.getInstance();

                    final String biomestring = (String) info.previousArgs()[0];

                    if (!EnumUtils.isValidEnum(Biome.class, biomestring.toUpperCase()))
                    {
                        return new ItemStack[] {};
                    }

                    return plugin.getDropsManager().getDropsByBiome(Biome.valueOf(biomestring))
                            .stream()
                            .map(i -> i.getItemStack()) // fix this
                            .toArray(ItemStack[]::new);
                })))
                .executes(this::run)
                .register();
    }

    private void run(CommandSender sender, Object[] args) throws WrapperCommandSyntaxException
    {
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

        if (!plugin.getDropsManager().hasBiome(biome))
        {
            throw CommandAPI.fail("This biome is not available for customization!");
        }

        final String operation = (String) args[1];

        ItemStack itemstack;

        if (args.length == 3)
        {
            itemstack = (ItemStack) args[2];
        }
        else
        {
            if (!(sender instanceof Player))
            {
                throw CommandAPI.fail("Only players can use this command!");
            }

            itemstack = ((Player) sender).getInventory().getItemInMainHand();
        }

        final List<WeightedItem> drops = plugin.getDropsManager().getDropsByBiome(biome);

        if (operation.equalsIgnoreCase("addDrop") || operation.equalsIgnoreCase("addDropFromHand"))
        {
            if (drops.stream().anyMatch(i -> i.getItemStack().equals(itemstack)))
            {
                throw CommandAPI.fail("This itemstack is already in the drop table!");
            }

            if (itemstack.getType().isAir())
            {
                throw CommandAPI.fail("You can't add AIR into the loot table!");
            }

            plugin.getDropsManager().addDrop(biome, itemstack, operation.equalsIgnoreCase("addDropFromHand") ? true : false);

            sender.sendMessage(Component.text("New item added to the drop table: ").append(itemstack.displayName()));
        }

        if (operation.equalsIgnoreCase("removeDrop"))
        {
            if (drops.stream().noneMatch(i -> i.getItemStack().equals(itemstack)))
            {
                throw CommandAPI.fail("This ItemStack is not in the drop table!");
            }

            plugin.getDropsManager().removeDrop(biome, itemstack);

            sender.sendMessage(Component.text("Item removed from the drop table: ").append(itemstack.displayName()));
        }
    }
}
