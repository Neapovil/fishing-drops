package com.github.neapovil.fishingdrops.manager;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.inventory.ItemStack;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.github.neapovil.fishingdrops.object.WeightedItem;

public class DropsManager
{
    private final FileConfig fileConfig;

    public DropsManager(FileConfig fileConfig)
    {
        this.fileConfig = fileConfig;
    }

    public List<WeightedItem> getDropsByBiome(Biome biome)
    {
        final List<WeightedItem> drops = new ArrayList<>();

        final List<UnmodifiableConfig> data = this.fileConfig.get("drops." + biome.toString());

        if (data != null)
        {
            for (UnmodifiableConfig i : data)
            {
                final String item = i.get("item");
                final String material = i.get("material");
                final int weight = i.getInt("weight");
                final int countmin = i.getInt("count.min");
                final int countmax = i.getInt("count.max");

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

    public boolean hasBiome(Biome biome)
    {
        return this.fileConfig.get("drops." + biome.toString()) != null;
    }

    public void create(Biome biome)
    {
        this.fileConfig.set("drops." + biome.toString(), new ArrayList<>());
    }

    public void delete(Biome biome)
    {
        this.fileConfig.remove("drops." + biome.toString());
    }

    public void addDrop(Biome biome, ItemStack itemStack, boolean customItemStack)
    {
        if (!this.hasBiome(biome))
        {
            return;
        }

        final List<UnmodifiableConfig> drops = this.fileConfig.get("drops." + biome.toString());

        final Config config = Config.inMemory();

        if (customItemStack)
        {
            config.add("item", Base64.getEncoder().encodeToString(itemStack.serializeAsBytes()));
        }
        else
        {
            config.add("item", "");
        }

        config.add("material", itemStack.getType().toString());
        config.add("weight", 1);
        config.add("count.min", 1);
        config.add("count.max", 1);

        drops.add(config);

        this.fileConfig.set("drops." + biome.toString(), drops);
    }

    public void removeDrop(Biome biome, ItemStack itemStack)
    {
        if (!this.hasBiome(biome))
        {
            return;
        }

        final List<UnmodifiableConfig> drops = this.fileConfig.get("drops." + biome.toString());

        drops.removeIf(i -> {
            final String bytestring = (String) i.get("item");

            if (bytestring.isEmpty())
            {
                final Material material = Material.getMaterial((String) i.get("material"));

                return material.equals(itemStack.getType());
            }

            final byte[] bytes = Base64.getDecoder().decode(bytestring);
            final ItemStack itemstack = ItemStack.deserializeBytes(bytes);

            return itemstack.equals(itemStack);
        });

        this.fileConfig.set("drops." + biome.toString(), drops);
    }
}
