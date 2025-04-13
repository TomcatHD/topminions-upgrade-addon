package com.itemsadder_topminions.gui;

import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import com.itemsadder_topminions.util.ItemUtil;
import com.itemsadder_topminions.config.ConfigHandler;
import com.itemsadder_topminions.gui.GlobalCraftingMenu;



import com.sarry20.topminion.models.minion.MinionObj;

import java.util.*;

public class UpgradeMenu {

    public static final Map<UUID, MinionObj> minionContext = new HashMap<>();
    private static final NamespacedKey minionKey = new NamespacedKey("topminion", "topminions");

    public static void openUpgradeGUI(Player player, MinionObj minion) {
        FileConfiguration config = ConfigHandler.getUpgrades();
        FileConfiguration messages = ConfigHandler.getMessages();
        FileConfiguration items = ConfigHandler.getItems();

        String key = minion.getType().toLowerCase() + "_" + minion.getMaterial().toString().toLowerCase();
        int level = minion.getLevel();

        String basePath = "upgrades." + key + ".gui";
        String itemPath = "upgrades." + key + ".levels." + level + ".items";
        String reqPath = "upgrades." + key + ".levels." + level + ".upgrade-requirements";

        String title = ChatColor.translateAlternateColorCodes('&', config.getString(basePath + ".title", "Upgrade Minion"));
        int size = config.getInt(basePath + ".size", 27);

        Inventory gui = Bukkit.createInventory(null, size, title);

        List<Map<?, ?>> buttons = config.getMapList(itemPath);
        for (Map<?, ?> button : buttons) {
            int slot = (int) button.get("slot");
            String name = (String) button.get("display_name");
            String materialId = (String) button.get("material");
            List<String> lore = (List<String>) button.get("lore");
            String type = (String) button.get("type");

            ItemStack icon;
            if (materialId.contains(":")) {
                CustomStack cs = CustomStack.getInstance(materialId);
                icon = cs != null ? cs.getItemStack().clone() : new ItemStack(Material.BARRIER);
            } else {
                Material mat = Material.matchMaterial(materialId);
                icon = mat != null ? new ItemStack(mat) : new ItemStack(Material.BARRIER);
            }

            ItemMeta meta = icon.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

                List<String> displayLore = new ArrayList<>();
                if (lore != null) {
                    for (String line : lore) {
                        displayLore.add(ChatColor.translateAlternateColorCodes('&', line));
                    }
                }

                if ("upgrade_button".equalsIgnoreCase(type) && config.contains(reqPath)) {
                    List<Map<?, ?>> requirements = config.getMapList(reqPath);
                    displayLore.add(" ");
                    String reqTitle = ChatColor.translateAlternateColorCodes('&',
                            messages.getString("gui.requirements-title", "&7Requirements:"));
                    displayLore.add(reqTitle);


                    String format = messages.getString("gui.requirement-lore", "%status% %name% x%amount% | (%found%)");

                    for (Map<?, ?> req : requirements) {
                        String itemId = (String) req.get("item");
                        int amount = (int) req.get("amount");

                        int found = countItem(player, itemId);
                        boolean hasEnough = found >= amount;
                        String symbol = hasEnough ? ChatColor.GREEN + "✔" : ChatColor.RED + "✘";

                        String friendlyName = ItemUtil.getFriendlyName(itemId);
                        String line = format
                                .replace("%status%", symbol)
                                .replace("%name%", friendlyName)
                                .replace("%amount%", String.valueOf(amount))
                                .replace("%found%", String.valueOf(found));

                        displayLore.add(ChatColor.translateAlternateColorCodes('&', line));
                    }
                }

                meta.setLore(displayLore);
                icon.setItemMeta(meta);
                gui.setItem(slot, icon);
            }
        }
// Inject global crafting button if enabled
        if (config.getBoolean("global-crafting-button.enabled", false)) {
            int slot = config.getInt("global-crafting-button.slot", 22);
            String name = config.getString("global-crafting-button.display_name", "&aGlobal Crafting");
            String materialName = config.getString("global-crafting-button.material", "CRAFTING_TABLE");
            List<String> loreList = config.getStringList("global-crafting-button.lore");

            Material material = Material.matchMaterial(materialName);
            if (material != null) {
                ItemStack button = new ItemStack(material);
                ItemMeta meta = button.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
                    List<String> coloredLore = loreList.stream()
                            .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                            .toList();
                    meta.setLore(coloredLore);
                    button.setItemMeta(meta);
                    gui.setItem(slot, button);
                }
            }
        }

        minionContext.put(player.getUniqueId(), minion);
        player.openInventory(gui);
    }

    private static int countItem(Player player, String itemId) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;

            // Check ItemsAdder item
            CustomStack cs = CustomStack.byItemStack(item);
            if (cs != null && cs.getNamespacedID().equalsIgnoreCase(itemId)) {
                count += item.getAmount();
                continue;
            }

            // Check vanilla item by material
            Material mat = Material.matchMaterial(itemId);
            if (mat != null && item.getType() == mat) {
                count += item.getAmount();
                continue;
            }

            // Check for minion items
            if (itemId.startsWith("minion#") && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    PersistentDataContainer container = meta.getPersistentDataContainer();
                    String expectedId = itemId.substring(7); // Ex: ZOMBIE#SLAYER#1
                    String actualId = container.get(minionKey, PersistentDataType.STRING);
                    if (actualId != null && actualId.equalsIgnoreCase(expectedId)) {
                        count += item.getAmount();
                    }
                }
            }
        }
        return count;
    }

}
