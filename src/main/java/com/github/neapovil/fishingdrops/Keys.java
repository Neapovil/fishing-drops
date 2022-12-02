package com.github.neapovil.fishingdrops;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

public class Keys<T, Z>
{
    public static Keys<Integer, Integer> VIEW_COMMAND = new Keys<>("view-command", PersistentDataType.INTEGER);

    private final String key;
    private final PersistentDataType<T, Z> type;

    public Keys(String key, PersistentDataType<T, Z> type)
    {
        this.key = key;
        this.type = type;
    }

    public PersistentDataType<T, Z> type()
    {
        return this.type;
    }

    public NamespacedKey namespacedKey()
    {
        return new NamespacedKey(FishingDrops.getInstance(), this.key);
    }
}
