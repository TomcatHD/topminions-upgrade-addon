package com.itemsadder_topminions.util;

import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import com.itemsadder_topminions.config.ConfigHandler;

import java.util.Arrays;

public class ItemUtil {

    public static String getFriendlyName(String itemId) {
        FileConfiguration messages = ConfigHandler.getMessages();
        FileConfiguration items = ConfigHandler.getItems();


        if (itemId.startsWith("minion#")) {
            try {
                String[] parts = itemId.split("#");
                String type = parts[1];
                int level = Integer.parseInt(parts[3]);
                String raw = messages.getString("minion-display-names." + type, type);
                return ChatColor.translateAlternateColorCodes('&', raw.replace("{level}", String.valueOf(level)));
            } catch (Exception e) {
                return itemId;
            }
        }

        // Check custom translation in items.yml
        if (items.contains(itemId)) {
            return ChatColor.translateAlternateColorCodes('&', items.getString(itemId));
        }

        // Try ItemsAdder custom item
        CustomStack cs = CustomStack.getInstance(itemId);
        if (cs != null) return cs.getDisplayName();

        // Try vanilla item via Bukkit fallback
        Material mat = Material.matchMaterial(itemId);
        if (mat != null) {
            ItemStack tempItem = new ItemStack(mat);
            ItemMeta meta = tempItem.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                return meta.getDisplayName();
            }
            String formatted = mat.name().toLowerCase().replace("_", " ");
            return ChatColor.GRAY.toString() + Arrays.stream(formatted.split(" "))
                    .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
                    .reduce((a, b) -> a + " " + b).orElse(mat.name());
        }

        return itemId;
    }
}