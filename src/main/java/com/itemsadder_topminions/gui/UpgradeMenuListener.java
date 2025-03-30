package com.itemsadder_topminions.gui;

import com.sarry20.topminion.models.minion.MinionObj;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import com.itemsadder_topminions.config.ConfigHandler;

import java.util.*;

public class UpgradeMenuListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        if (!event.getView().getTitle().equalsIgnoreCase("Upgrade Minion")) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        MinionObj minion = UpgradeMenu.minionContext.get(player.getUniqueId());
        if (minion == null) return;

        int currentLevel = minion.getLevel();
        int nextLevel = currentLevel + 1;

        String key = minion.getType().toLowerCase() + "_" + minion.getMaterial().toString().toLowerCase();
        FileConfiguration cfg = ConfigHandler.getUpgrades();
        String basePath = "upgrades." + key + ".levels." + currentLevel;

        if (!cfg.contains(basePath)) {
            player.sendMessage("§cNo upgrade data for this level.");
            return;
        }

        // Detect clicked action
        List<Map<?, ?>> items = cfg.getMapList(basePath + ".items");
        String action = null;

        for (Map<?, ?> map : items) {
            int slot = (int) map.get("slot");
            if (slot == event.getRawSlot()) {
                action = (String) map.get("action");
                break;
            }
        }

        if (action == null) return;

        if (action.equals("upgrade")) {
            // Load requirements
            List<Map<?, ?>> requirements = cfg.getMapList(basePath + ".upgrade-requirements");
            if (requirements == null) return;

            for (Map<?, ?> req : requirements) {
                String itemId = (String) req.get("item");
                int amount = (int) req.get("amount");

                if (!hasEnough(player, itemId, amount)) {
                    player.sendMessage("§cMissing: " + itemId + " x" + amount);
                    return;
                }
            }

            // Remove required items
            for (Map<?, ?> req : requirements) {
                String itemId = (String) req.get("item");
                int amount = (int) req.get("amount");

                removeItems(player, itemId, amount);
            }

            // Upgrade minion
            minion.setLevel(nextLevel);
            player.sendMessage("§aMinion upgraded to level " + nextLevel + "!");
            player.closeInventory();
            UpgradeMenu.minionContext.remove(player.getUniqueId());

        } else if (action.startsWith("craft_")) {
            String[] parts = action.split("_");
            if (parts.length == 4) {
                String type = parts[1].toUpperCase();
                String material = parts[2].toUpperCase();
                int level = Integer.parseInt(parts[3]);
                MinionCraftingMenu.openCraftingGUI(player, type, material, level);
            }
        }
    }

    private boolean hasEnough(Player player, String itemId, int amount) {
        int total = 0;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;

            if (itemId.startsWith("minion#")) {
                String expectedTag = itemId.replace("minion#", "");
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.getPersistentDataContainer().has(new NamespacedKey("topminion", "topminions"), PersistentDataType.STRING)) {
                    String tag = meta.getPersistentDataContainer().get(new NamespacedKey("topminion", "topminions"), PersistentDataType.STRING);
                    if (tag != null && tag.equalsIgnoreCase(expectedTag)) {
                        total += item.getAmount();
                    }
                }
            } else if (itemId.contains(":")) {
                CustomStack required = CustomStack.getInstance(itemId);
                CustomStack inInventory = CustomStack.byItemStack(item);
                if (required != null && inInventory != null && required.getId().equalsIgnoreCase(inInventory.getId())) {
                    total += item.getAmount();
                }
            } else {
                Material mat = Material.matchMaterial(itemId);
                if (mat != null && item.getType() == mat) {
                    total += item.getAmount();
                }
            }

            if (total >= amount) return true;
        }

        return false;
    }

    private void removeItems(Player player, String itemId, int amount) {
        int toRemove = amount;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || toRemove <= 0) continue;

            if (itemId.startsWith("minion#")) {
                String expectedTag = itemId.replace("minion#", "");
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.getPersistentDataContainer().has(new NamespacedKey("topminion", "topminions"), PersistentDataType.STRING)) {
                    String tag = meta.getPersistentDataContainer().get(new NamespacedKey("topminion", "topminions"), PersistentDataType.STRING);
                    if (tag != null && tag.equalsIgnoreCase(expectedTag)) {
                        int amt = item.getAmount();
                        if (amt <= toRemove) {
                            toRemove -= amt;
                            item.setAmount(0);
                        } else {
                            item.setAmount(amt - toRemove);
                            toRemove = 0;
                        }
                    }
                }
            } else if (itemId.contains(":")) {
                CustomStack required = CustomStack.getInstance(itemId);
                CustomStack inInventory = CustomStack.byItemStack(item);
                if (required != null && inInventory != null && required.getId().equalsIgnoreCase(inInventory.getId())) {
                    int amt = item.getAmount();
                    if (amt <= toRemove) {
                        toRemove -= amt;
                        item.setAmount(0);
                    } else {
                        item.setAmount(amt - toRemove);
                        toRemove = 0;
                    }
                }
            } else {
                Material mat = Material.matchMaterial(itemId);
                if (mat != null && item.getType() == mat) {
                    int amt = item.getAmount();
                    if (amt <= toRemove) {
                        toRemove -= amt;
                        item.setAmount(0);
                    } else {
                        item.setAmount(amt - toRemove);
                        toRemove = 0;
                    }
                }
            }
        }

        player.updateInventory();
    }
}
