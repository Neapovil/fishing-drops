package com.github.neapovil.fishingdrops.listener;

import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

import com.github.neapovil.fishingdrops.FishingDrops;
import com.github.neapovil.fishingdrops.Keys;
import com.github.neapovil.fishingdrops.object.WeightedItem;

public class Listener implements org.bukkit.event.Listener
{
    private final FishingDrops plugin = FishingDrops.getInstance();

    @EventHandler
    private void playerFishing(PlayerFishEvent event)
    {
        if (!event.getState().equals(PlayerFishEvent.State.CAUGHT_FISH))
        {
            return;
        }

        final Location location = event.getHook().getLocation();
        final Biome biome = event.getHook().getWorld().getComputedBiome(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        final List<WeightedItem> items = plugin.getDropsManager().getDropsByBiome(biome);

        if (items.isEmpty())
        {
            return;
        }

        items.sort(Comparator.comparingInt(WeightedItem::getWeight));

        final int weight = items.stream().map(i -> i.getWeight()).reduce(1, Integer::sum);
        final Random random = new Random();
        final int rolledweight = random.nextInt(1, weight);

        final WeightedItem weighteditem = items.stream()
                .filter(i -> rolledweight <= i.getWeight())
                .findAny()
                .orElse(items.get(items.size() - 1));

        final ItemStack itemstack = weighteditem.getItemStack();

        itemstack.setAmount(random.nextInt(weighteditem.getMinCount(), weighteditem.getMaxCount() + 1));

        final Item item = (Item) event.getCaught();

        item.setItemStack(itemstack);
    }

    @EventHandler
    private void viewCommandInventoryClick(InventoryClickEvent event)
    {
        if (event.getWhoClicked().getPersistentDataContainer().has(Keys.VIEW_COMMAND.namespacedKey()))
        {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void viewCommandInventoryClose(InventoryCloseEvent event)
    {
        if (event.getPlayer().getPersistentDataContainer().has(Keys.VIEW_COMMAND.namespacedKey()))
        {
            event.getPlayer().getPersistentDataContainer().remove(Keys.VIEW_COMMAND.namespacedKey());
        }
    }
}
