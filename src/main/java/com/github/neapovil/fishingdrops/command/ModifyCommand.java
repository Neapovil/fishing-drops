package com.github.neapovil.fishingdrops.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.github.neapovil.fishingdrops.FishingDrops;
import com.github.neapovil.fishingdrops.object.WeightedItem;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.BiomeArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.ItemStackArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import dev.jorel.commandapi.arguments.SafeSuggestions;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import net.kyori.adventure.text.Component;

public class ModifyCommand implements ICommand
{
    private final FishingDrops plugin = FishingDrops.getInstance();

    @Override
    public void register()
    {
        final List<Argument<?>> arguments = new ArrayList<>();

        arguments.add(new LiteralArgument("modify"));
        arguments.add(new BiomeArgument("biome").replaceSafeSuggestions(SafeSuggestions.suggest(info -> {
            return Arrays.asList(Biome.values())
                    .stream()
                    .filter(i -> plugin.getDropsManager().hasBiome(i))
                    .toArray(Biome[]::new);
        })));

        new CommandAPICommand(this.commandName())
                .withPermission(this.permission())
                .withArguments(arguments)
                .withArguments(new MultiLiteralArgument("addDropFromHand"))
                .executes(this::run)
                .register();

        new CommandAPICommand(this.commandName())
                .withPermission(this.permission())
                .withArguments(arguments)
                .withArguments(new MultiLiteralArgument("addDrop"))
                .withArguments(new ItemStackArgument("itemstack"))
                .executes(this::run)
                .register();

        final Argument<Integer> indexargument = new IntegerArgument("index", 0).replaceSafeSuggestions(SafeSuggestions.suggest(info -> {
            final Biome biome = (Biome) info.previousArgs()[0];
            final int dropsize = plugin.getDropsManager().getDropsByBiome(biome).size();

            return IntStream.range(0, dropsize).boxed().toArray(Integer[]::new);
        }));

        new CommandAPICommand(this.commandName())
                .withPermission(this.permission())
                .withArguments(arguments)
                .withArguments(new MultiLiteralArgument("removeDrop"))
                .withArguments(indexargument)
                .executes(this::run)
                .register();

        new CommandAPICommand(this.commandName())
                .withPermission(this.permission())
                .withArguments(arguments)
                .withArguments(new MultiLiteralArgument("edit"))
                .withArguments(indexargument)
                .withArguments(new MultiLiteralArgument("weight"))
                .withArguments(new IntegerArgument("newWeight", 1))
                .executes(this::run)
                .register();

        new CommandAPICommand(this.commandName())
                .withPermission(this.permission())
                .withArguments(arguments)
                .withArguments(new MultiLiteralArgument("edit"))
                .withArguments(indexargument)
                .withArguments(new MultiLiteralArgument("count"))
                .withArguments(new IntegerArgument("min", 1))
                .withArguments(new IntegerArgument("max", 1))
                .executes(this::run)
                .register();
    }

    private void run(CommandSender sender, Object[] args) throws WrapperCommandSyntaxException
    {
        final Biome biome = (Biome) args[0];

        if (biome.equals(Biome.CUSTOM))
        {
            throw CommandAPI.fail("Custom biomes are not supported yet!");
        }

        if (!plugin.getDropsManager().hasBiome(biome))
        {
            throw CommandAPI.fail("This biome is not available for customization!");
        }

        final String operation = (String) args[1];

        if (operation.equals("addDropFromHand"))
        {
            if (!(sender instanceof Player))
            {
                throw CommandAPI.fail("Only players can run this command!");
            }

            final ItemStack itemstack = ((Player) sender).getInventory().getItemInMainHand();

            final boolean status = plugin.getDropsManager().addDrop(biome, itemstack, true);

            if (status)
            {
                sender.sendMessage(Component.text("Custom item added: ").append(itemstack.displayName()));
            }
            else
            {
                throw CommandAPI.fail("Item can't be added. Be sure that its not a duplicate or air.");
            }
        }

        if (operation.equals("addDrop"))
        {
            final ItemStack itemstack = (ItemStack) args[2];

            final boolean status = plugin.getDropsManager().addDrop(biome, itemstack, false);

            if (status)
            {
                sender.sendMessage(Component.text("Generic item added: ").append(itemstack.displayName()));
            }
            else
            {
                throw CommandAPI.fail("Item can't be added. Be sure that its not a duplicate or air.");
            }
        }

        if (operation.equals("removeDrop"))
        {
            final int index = (int) args[2];

            final List<WeightedItem> drops = plugin.getDropsManager().getDropsByBiome(biome);

            final boolean removed = plugin.getDropsManager().removeDropByIndex(biome, index);

            if (!removed)
            {
                throw CommandAPI.fail("Item can't be removed. Be sure it exist.");
            }

            sender.sendMessage(Component.text("Item removed: ").append(drops.get(index).itemStack().displayName()));
        }

        if (operation.equals("edit"))
        {
            final int index = (int) args[2];
            final String editoperation = (String) args[3];

            if (editoperation.equals("weight"))
            {
                final int weight = (int) args[4];

                final boolean edited = plugin.getDropsManager().editWeightByIndex(biome, index, weight);

                if (!edited)
                {
                    throw CommandAPI.fail("Unable to edit item. Please check the parameters.");
                }

                sender.sendMessage("New item weight: " + weight);
            }

            if (editoperation.equals("count"))
            {
                final int mincount = (int) args[4];
                final int maxcount = (int) args[5];

                final boolean edited = plugin.getDropsManager().editCountByIndex(biome, index, mincount, maxcount);

                if (!edited)
                {
                    throw CommandAPI.fail("Unable to edit item. Please check the parameters.");
                }

                sender.sendMessage("New item counts: " + mincount + " min" + maxcount + " max");
            }
        }
    }
}
