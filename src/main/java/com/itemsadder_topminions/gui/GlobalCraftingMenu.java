package com.itemsadder_topminions.gui;

import com.itemsadder_topminions.config.ConfigHandler;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class GlobalCraftingMenu implements Listener {

    private static final String GUI_TITLE = ChatColor.translateAlternateColorCodes('&',
            ConfigHandler.getCrafting().getString("global-crafting.title", "&6Global Crafting"));

    public static void open(Player player) {
        FileConfiguration config = ConfigHandler.getCrafting();
        ConfigurationSection recipesSection = config.getConfigurationSection("global-crafting.recipes");
        if (recipesSection == null) {
            player.sendMessage(ChatColor.RED + "No global recipes configured.");
            return;
        }

        int size = config.getInt("global-crafting.size", 27);
        Inventory gui = Bukkit.createInventory(null, size, GUI_TITLE);

        for (String key : recipesSection.getKeys(false)) {
            ConfigurationSection recipe = recipesSection.getConfigurationSection(key);
            if (recipe == null) continue;

            String displayItemId = recipe.getString("display_item", "minecraft:paper");
            String displayName = recipe.getString("display_name", key);
            List<String> lore = recipe.getStringList("lore");
            int slot = recipe.getInt("slot", -1);

            ItemStack item;
            if (displayItemId.contains(":")) {
                CustomStack cs = CustomStack.getInstance(displayItemId);
                item = (cs != null) ? cs.getItemStack().clone() : new ItemStack(Material.PAPER);
            } else {
                Material mat = Material.matchMaterial(displayItemId);
                item = (mat != null) ? new ItemStack(mat) : new ItemStack(Material.PAPER);
            }

            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
                if (lore != null && !lore.isEmpty()) {
                    List<String> coloredLore = new ArrayList<>();
                    for (String line : lore) {
                        coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
                    }
                    meta.setLore(coloredLore);
                }
                item.setItemMeta(meta);
            }

            if (slot >= 0 && slot < size) {
                gui.setItem(slot, item);
            }
        }

        player.openInventory(gui);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!ChatColor.stripColor(event.getView().getTitle()).equalsIgnoreCase(ChatColor.stripColor(GUI_TITLE))) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        FileConfiguration config = ConfigHandler.getCrafting();
        ConfigurationSection recipes = config.getConfigurationSection("global-crafting.recipes");
        if (recipes == null) return;

        for (String key : recipes.getKeys(false)) {
            ConfigurationSection recipe = recipes.getConfigurationSection(key);
            if (recipe == null) continue;

            int expectedSlot = recipe.getInt("slot", -1);
            if (event.getSlot() == expectedSlot) {
                player.closeInventory();
                MinionCraftingMenu.openCraftingGUI(player, "global", key, 0);
                return;
            }
        }
    }
}
