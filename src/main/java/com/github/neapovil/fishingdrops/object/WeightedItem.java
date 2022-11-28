package com.github.neapovil.fishingdrops.object;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class WeightedItem
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
