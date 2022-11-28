package com.github.neapovil.fishingdrops.manager;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.inventory.ItemStack;

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
}
