package com.github.neapovil.fishingdrops.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import dev.jorel.commandapi.arguments.ItemStackArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import dev.jorel.commandapi.arguments.SafeSuggestions;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import net.kyori.adventure.text.Component;

public class ModifyCommand implements ICommand
{
    @Override
    public void register()
    {
        final List<Argument<?>> arguments = new ArrayList<>();

        arguments.add(new LiteralArgument("modify"));
        arguments.add(new BiomeArgument("biome").replaceSafeSuggestions(SafeSuggestions.suggest(info -> {
            final FishingDrops plugin = FishingDrops.getInstance();

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

        new CommandAPICommand(this.commandName())
                .withPermission(this.permission())
                .withArguments(arguments)
                .withArguments(new MultiLiteralArgument("removeDrop"))
                .withArguments(new ItemStackArgument("itemstack").replaceSafeSuggestions(SafeSuggestions.suggest(info -> {
                    final Biome biome = (Biome) info.previousArgs()[0];

                    final FishingDrops plugin = FishingDrops.getInstance();

                    return plugin.getDropsManager().getDropsByBiome(biome)
                            .stream()
                            .map(i -> i.getItemStack()) // fix this
                            .toArray(ItemStack[]::new);
                })))
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
