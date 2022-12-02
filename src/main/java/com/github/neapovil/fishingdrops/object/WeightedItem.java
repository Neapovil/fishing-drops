package com.github.neapovil.fishingdrops.object;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class WeightedItem
{
    @Nullable
    private ItemStack itemStack;
    private final Material material;
    private final int weight;
    private final int minCount;
    private final int maxCount;

    public WeightedItem(Material material, int weight, int minCount, int maxCount)
    {
        this.material = material;
        this.weight = weight;
        this.minCount = minCount;
        this.maxCount = maxCount;
    }

    public ItemStack itemStack()
    {
        ItemStack itemstack = this.itemStack;

        if (itemstack == null)
        {
            itemstack = new ItemStack(this.material);
        }

        return itemstack;
    }

    public void itemStack(ItemStack itemStack)
    {
        this.itemStack = itemStack;
    }

    public Material material()
    {
        return this.material;
    }

    public int weight()
    {
        return this.weight;
    }

    public int minCount()
    {
        return this.minCount;
    }

    public int maxCount()
    {
        return this.maxCount;
    }
}
