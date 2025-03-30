package com.itemsadder_topminions.listener;

import dev.lone.itemsadder.api.ItemsAdder;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class ItemsAdderProvider implements Listener {

    public ItemsAdderProvider() {
        // Optionally, you can put any ItemsAdder setup logic here
        if (isItemsAdderAvailable()) {
            Bukkit.getLogger().info("[TopMinions] Successfully hooked into ItemsAdder.");
        } else {
            Bukkit.getLogger().warning("[TopMinions] ItemsAdder not found! Some features may not work.");
        }
    }

    public boolean isItemsAdderAvailable() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("ItemsAdder");
        return plugin != null && plugin.isEnabled();
    }
}
