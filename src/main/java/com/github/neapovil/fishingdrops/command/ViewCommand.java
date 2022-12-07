package com.github.neapovil.fishingdrops.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.block.Biome;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.neapovil.fishingdrops.FishingDrops;
import com.github.neapovil.fishingdrops.Keys;
import com.github.neapovil.fishingdrops.object.WeightedItem;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.BiomeArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.SafeSuggestions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;

public class ViewCommand implements ICommand
{
    @Override
    public void register()
    {
        new CommandAPICommand(this.commandName())
                .withPermission(this.permission())
                .withArguments(new LiteralArgument("view"))
                .withArguments(new BiomeArgument("biome").replaceSafeSuggestions(SafeSuggestions.suggest(info -> {
                    final FishingDrops plugin = FishingDrops.getInstance();

                    return Arrays.asList(Biome.values())
                            .stream()
                            .filter(i -> plugin.getDropsManager().hasBiome(i))
                            .toArray(Biome[]::new);
                })))
                .executesPlayer((player, args) -> {
                    final Biome biome = (Biome) args[0];

                    final FishingDrops plugin = FishingDrops.getInstance();

                    if (!plugin.getDropsManager().hasBiome(biome))
                    {
                        throw CommandAPI.fail("This biome doesn't have a custom drop table!");
                    }

                    final Component title = Component.text(biome.toString() + "'s Loot Table");
                    final Inventory inventory = plugin.getServer().createInventory(null, 54, title);

                    final List<WeightedItem> items = plugin.getDropsManager().getDropsByBiome(biome)
                            .stream()
                            .toList();

                    for (int i = 0; i < items.size(); i++)
                    {
                        final WeightedItem weighteditem = items.get(i);
                        final ItemStack itemstack = weighteditem.itemStack();

                        final int index = i;
                        itemstack.editMeta(i1 -> {
                            final List<Component> lore = new ArrayList<>();

                            final Style style = Style.empty().color(NamedTextColor.GOLD);

                            lore.add(Component.text("----"));
                            lore.add(Component.text("(Remove index: " + index + ")", style));
                            lore.add(Component.text("Weight: " + weighteditem.weight(), style));
                            lore.add(Component.text("Min Count: " + weighteditem.minCount(), style));
                            lore.add(Component.text("Max Count: " + weighteditem.maxCount(), style));
                            lore.add(Component.text("----"));

                            if (i1.hasLore())
                            {
                                lore.addAll(i1.lore());
                            }

                            i1.lore(lore);
                        });

                        inventory.addItem(itemstack);
                    }

                    final Keys<Integer, Integer> key = Keys.VIEW_COMMAND;

                    player.getPersistentDataContainer().set(key.namespacedKey(), key.type(), 1);

                    player.openInventory(inventory);
                })
                .register();
    }
}
