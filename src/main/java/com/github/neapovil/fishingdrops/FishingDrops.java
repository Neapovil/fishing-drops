package com.github.neapovil.fishingdrops;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.file.FileConfig;

public class FishingDrops extends JavaPlugin implements Listener
{
    private static FishingDrops instance;
    private FileConfig dropsConfig;

    @Override
    public void onEnable()
    {
        instance = this;

        this.getServer().getPluginManager().registerEvents(this, this);

        this.saveResource("drops.json", false);

        this.dropsConfig = FileConfig.builder(this.getDataFolder().toPath().resolve("drops.json"))
                .autoreload()
                .autosave()
                .build();

        this.dropsConfig.load();
    }

    @Override
    public void onDisable()
    {
    }

    public static FishingDrops getInstance()
    {
        return instance;
    }

    private List<WeightedItem> getDropsByBiome(Biome biome)
    {
        final List<WeightedItem> drops = new ArrayList<>();

        final List<UnmodifiableConfig> data = this.dropsConfig.get("drops." + biome.toString());

        if (data != null)
        {
            for (UnmodifiableConfig c : data)
            {
                final String item = c.get("item");
                final String material = c.get("material");
                final int weight = c.getInt("weight");
                final int countmin = c.getInt("count.min");
                final int countmax = c.getInt("count.max");

                final WeightedItem weighteditem = new WeightedItem();

                try
                {
                    weighteditem.setItemStack(ItemStack.deserializeBytes(Base64.getDecoder().decode(item)));
                }
                catch (Exception e)
                {
                }

                weighteditem.setMaterial(Material.getMaterial(material));
                weighteditem.setWeight(weight);
                weighteditem.setMinCount(countmin);
                weighteditem.setMaxCount(countmax);

                drops.add(weighteditem);
            }
        }

        return drops;
    }

    @EventHandler
    private void playerFishing(PlayerFishEvent event)
    {
        if (!event.getState().equals(PlayerFishEvent.State.CAUGHT_FISH))
        {
            return;
        }

        final Location location = event.getHook().getLocation();
        final Biome biome = event.getHook().getWorld().getComputedBiome(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        final List<WeightedItem> items = this.getDropsByBiome(biome);

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

    class WeightedItem
    {
        @Nullable
        private ItemStack itemStack;
        private Material material;
        private int weight;
        private int minCount;
        private int maxCount;

        public ItemStack getItemStack()
        {
            ItemStack itemstack = this.itemStack;

            if (itemstack == null)
            {
                itemstack = new ItemStack(this.material);
            }

            return itemstack;
        }

        public Material getMaterial()
        {
            return this.material;
        }

        public int getWeight()
        {
            return this.weight;
        }

        public int getMinCount()
        {
            return this.minCount;
        }

        public int getMaxCount()
        {
            return this.maxCount;
        }

        public void setItemStack(ItemStack itemStack)
        {
            this.itemStack = itemStack;
        }

        public void setMaterial(Material material)
        {
            this.material = material;
        }

        public void setWeight(int weight)
        {
            this.weight = weight;
        }

        public void setMinCount(int minCount)
        {
            this.minCount = minCount;
        }

        public void setMaxCount(int maxCount)
        {
            this.maxCount = maxCount;
        }
    }
}
