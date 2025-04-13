package com.itemsadder_topminions.gui;

import com.itemsadder_topminions.util.ItemUtil;
import com.itemsadder_topminions.util.SkullUtil;
import com.itemsadder_topminions.itemsadder_topminions;
import com.sarry20.topminion.models.minion.MinionObj;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import com.itemsadder_topminions.config.ConfigHandler;




import java.util.*;

public class MinionCraftingMenu implements Listener {

    private static final NamespacedKey ghostKey = new NamespacedKey("itemsadder_topminions", "ghost_item");
    private static final NamespacedKey fillerKey = new NamespacedKey("itemsadder_topminions", "filler_item");
    private static final NamespacedKey minionKey = new NamespacedKey("topminion", "topminions");
    private static final Map<UUID, String> lastRecipeKey = new HashMap<>();
    private static final int[] ghostSlots = {10, 11, 12, 19, 20, 21, 28, 29, 30};
    private static final int[] inputSlots = {14, 15, 16, 23, 24, 25, 32, 33, 34};

    @EventHandler
    public void onCraftingClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        String expectedTitle = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',
                ConfigHandler.getCrafting().getString("gui.title", "Craft Minion")));

        if (!ChatColor.stripColor(event.getView().getTitle()).equalsIgnoreCase(expectedTitle)) return;

        Inventory top = event.getView().getTopInventory();
        Inventory clickedInv = event.getClickedInventory();
        if (clickedInv == null) return;

        // Cancel shift-click if any ghost slot is a valid target
        if (event.isShiftClick() && event.getClickedInventory() != null &&
                !event.getClickedInventory().equals(top)) {

            ItemStack shiftItem = event.getCurrentItem();
            if (shiftItem != null && shiftItem.getType() != Material.AIR) {
                for (int slot : ghostSlots) {
                    if (slot >= 0 && slot < top.getSize()) {
                        ItemStack ghostItem = top.getItem(slot);
                        if (ghostItem == null || ghostItem.getType() == Material.AIR ||
                                ghostItem.isSimilar(shiftItem) && ghostItem.getAmount() < ghostItem.getMaxStackSize()) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        }

        if (clickedInv.equals(top)) {
            ItemStack clicked = event.getCurrentItem();
            if (clicked != null && clicked.hasItemMeta()) {
                PersistentDataContainer container = clicked.getItemMeta().getPersistentDataContainer();
                if (container.has(ghostKey, PersistentDataType.BYTE) || container.has(fillerKey, PersistentDataType.BYTE)) {
                    event.setCancelled(true);
                    return;
                }
            }

            if (event.getRawSlot() >= 0 && event.getRawSlot() < 54 &&
                    Arrays.stream(ghostSlots).anyMatch(s -> s == event.getRawSlot())) {
                event.setCancelled(true);
                return;
            }

            int slot = event.getSlot();
            if (slot == 31) {
                event.setCancelled(true);
                handleCraft(player, event.getInventory());
                return;
            }
            if (slot == 49) {
                event.setCancelled(true);
                MinionObj minion = UpgradeMenu.minionContext.get(player.getUniqueId());
                if (minion != null) UpgradeMenu.openUpgradeGUI(player, minion);
                return;
            }
        }

        Bukkit.getScheduler().runTaskLater(itemsadder_topminions.getInstance(), () -> {
            updateCraftButton(player, event.getInventory());
        }, 1L);
        event.setCancelled(false);
    }


    @EventHandler
    public void onCraftingDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        String expectedTitle = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',
                ConfigHandler.getCrafting().getString("gui.title", "Craft Minion")));

        if (!ChatColor.stripColor(event.getView().getTitle()).equalsIgnoreCase(expectedTitle)) return;


        for (int slot : event.getRawSlots()) {
            if (slot >= 0 && slot < 54 && Arrays.stream(ghostSlots).anyMatch(s -> s == slot)) {
                event.setCancelled(true);
                return;
            }
        }
    }


    @EventHandler
    public void onCraftingClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();

        String expectedTitle = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',
                ConfigHandler.getCrafting().getString("gui.title", "Craft Minion")));

        if (!ChatColor.stripColor(event.getView().getTitle()).equalsIgnoreCase(expectedTitle)) return;


        Inventory inv = event.getInventory();
        for (int slot : inputSlots) {
            ItemStack item = inv.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                player.getInventory().addItem(item);
            }
        }
    }



    public static void openCraftingGUI(Player player, String type, String material, int level) {
        FileConfiguration config = ConfigHandler.getCrafting();


        String key;
        ConfigurationSection recipeSection;

        if ("global".equalsIgnoreCase(type)) {
            key = "global-crafting.recipes." + material;
            recipeSection = config.getConfigurationSection(key);
            if (recipeSection == null) {
                player.sendMessage(ChatColor.RED + "Global crafting recipe not found: " + key);
                return;
            }
        } else {
            String recipeKey = type.toLowerCase() + "_" + material.toLowerCase() + "_minion_" + level;
            key = "levels." + level + ".recipes." + recipeKey;
            recipeSection = config.getConfigurationSection(key);
            if (recipeSection == null) {
                player.sendMessage(ChatColor.RED + "Crafting recipe not found: " + key);
                return;
            }
        }

        lastRecipeKey.put(player.getUniqueId(), key);


        ConfigurationSection ingredientsSection = recipeSection.getConfigurationSection("ingredients");
        if (ingredientsSection == null) {
            player.sendMessage(ChatColor.RED + "Missing 'ingredients' section in recipe: " + key);
            return;
        }

        List<String> pattern = recipeSection.getStringList("pattern");
        String title = ChatColor.translateAlternateColorCodes('&', config.getString("gui.title", "Craft Minion"));
        Inventory gui = Bukkit.createInventory(null, 54, title);

        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.setDisplayName(" ");
            fillerMeta.getPersistentDataContainer().set(fillerKey, PersistentDataType.BYTE, (byte) 1);
            filler.setItemMeta(fillerMeta);
        }
        for (int i = 0; i < gui.getSize(); i++) gui.setItem(i, filler);

        Map<Character, ItemStack> ghostItems = new HashMap<>();
        for (String symbol : ingredientsSection.getKeys(false)) {
            String itemId = ingredientsSection.getString(symbol + ".item");
            int amount = ingredientsSection.getInt(symbol + ".amount", 1);

            ItemStack stack;
            String displayName = ChatColor.GRAY + "Item";


            if (itemId.startsWith("minion#")) {
                // Example: minion#ZOMBIE#IRON#3
                String[] parts = itemId.split("#");
                if (parts.length >= 4) {
                    String minionType = parts[1].toUpperCase();
                    String minionMaterial = parts[2].toUpperCase();
                    int minionLevel;

                    try {
                        minionLevel = Integer.parseInt(parts[3]);
                    } catch (NumberFormatException e) {
                        minionLevel = level; // fallback
                    }

                    // Load display name template and texture from config
                    String rawName = ConfigHandler.getMessages().getString(
                            "minion-display-names." + minionType,
                            "&7" + minionType + " Minion"
                    );
                    String customDisplayName = ChatColor.translateAlternateColorCodes('&',
                            rawName.replace("{level}", String.valueOf(minionLevel)));


                    String base64 = ConfigHandler.getMessages().getString(
                            "minion-heads." + minionType
                    );

                    if (base64 != null && !base64.isEmpty()) {
                        stack = SkullUtil.getCustomSkull(base64, customDisplayName);
                    } else {
                        stack = new ItemStack(Material.PLAYER_HEAD);
                        ItemMeta meta = stack.getItemMeta();
                        if (meta != null) {
                            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', customDisplayName));
                            stack.setItemMeta(meta);
                        }
                    }

                    // Set persistent data
                    ItemMeta meta = stack.getItemMeta();
                    if (meta != null) {
                        meta.getPersistentDataContainer().set(minionKey, PersistentDataType.STRING, minionType + "#" + minionMaterial + "#" + minionLevel);
                        meta.getPersistentDataContainer().set(ghostKey, PersistentDataType.BYTE, (byte) 1);
                        stack.setItemMeta(meta);
                    }
                } else {
                    // fallback ghost head
                    stack = new ItemStack(Material.BARRIER);
                    ItemMeta meta = stack.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(ChatColor.RED + "Invalid minion item format");
                        stack.setItemMeta(meta);
                    }
                }
            }


            else if (itemId.contains(":")) {
                CustomStack cs = CustomStack.getInstance(itemId);
                if (cs != null) {
                    stack = cs.getItemStack().clone();
                    displayName = cs.getDisplayName();
                } else {
                    stack = new ItemStack(Material.BARRIER);
                }
            } else {
                Material mat = Material.matchMaterial(itemId);
                stack = mat != null ? new ItemStack(mat) : new ItemStack(Material.BARRIER);
                displayName = ItemUtil.getFriendlyName(itemId);
            }

            stack.setAmount(amount);

// Only re-apply display/meta if not a minion# head (to avoid overwriting custom logic)
            if (!itemId.startsWith("minion#")) {
                ItemMeta meta = stack.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ChatColor.GRAY + displayName + " x" + amount);
                    meta.getPersistentDataContainer().set(ghostKey, PersistentDataType.BYTE, (byte) 1);
                    stack.setItemMeta(meta);
                }
            } else {
                // Still mark as ghost for minion# heads
                ItemMeta meta = stack.getItemMeta();
                if (meta != null) {
                    meta.getPersistentDataContainer().set(ghostKey, PersistentDataType.BYTE, (byte) 1);
                    stack.setItemMeta(meta);
                }
            }


            ghostItems.put(symbol.charAt(0), stack);
        }

        for (int i = 0; i < pattern.size(); i++) {
            String line = pattern.get(i);
            for (int j = 0; j < line.length(); j++) {
                char c = line.charAt(j);
                int slot = ghostSlots[i * 3 + j];
                gui.setItem(slot, (c != 'X' && ghostItems.containsKey(c)) ? ghostItems.get(c) : null);
            }
        }

        for (int slot : inputSlots) gui.setItem(slot, null);

        FileConfiguration messages = ConfigHandler.getMessages();
        String name = ChatColor.translateAlternateColorCodes('&',
                messages.getString("craft-button.not-ready.name", "&cCraft Minion"));
        List<String> lore = messages.getStringList("craft-button.not-ready.lore");
        List<String> coloredLore = new ArrayList<>();
        for (String line : lore) {
            coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }

        ItemStack craft = new ItemStack(Material.RED_WOOL);
        ItemMeta craftMeta = craft.getItemMeta();
        if (craftMeta != null) {
            craftMeta.setDisplayName(name);
            if (!coloredLore.isEmpty()) craftMeta.setLore(coloredLore);
            craft.setItemMeta(craftMeta);
        }
        gui.setItem(31, craft);


        String backName = ChatColor.translateAlternateColorCodes('&',
                messages.getString("craft-button.back.name", "&cBack to Upgrade Menu"));

        ItemStack back = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(backName);
            back.setItemMeta(backMeta);
        }


        gui.setItem(49, back);

        lastRecipeKey.put(player.getUniqueId(), key);
        player.openInventory(gui);
    }

    private void updateCraftButton(Player player, Inventory inv) {
        FileConfiguration config = ConfigHandler.getCrafting();
        FileConfiguration messages = ConfigHandler.getMessages();

        String recipeKey = lastRecipeKey.get(player.getUniqueId());
        if (recipeKey == null) return;

        ConfigurationSection recipeSection = config.getConfigurationSection(recipeKey);
        if (recipeSection == null) return;

        List<String> pattern = recipeSection.getStringList("pattern");
        ConfigurationSection ingredients = recipeSection.getConfigurationSection("ingredients");
        if (ingredients == null) return;

        Map<Character, ItemStack> expected = new HashMap<>();
        for (String key : ingredients.getKeys(false)) {
            String itemId = ingredients.getString(key + ".item");
            int amount = ingredients.getInt(key + ".amount");

            ItemStack check;
            if (itemId.contains(":")) {
                CustomStack cs = CustomStack.getInstance(itemId);
                check = cs != null ? cs.getItemStack().clone() : null;
            } else if (itemId.startsWith("minion#")) {
                check = new ItemStack(Material.PLAYER_HEAD);
                ItemMeta meta = check.getItemMeta();
                if (meta != null) {
                    String minionId = itemId.substring(7);
                    meta.getPersistentDataContainer().set(minionKey, PersistentDataType.STRING, minionId);
                    check.setItemMeta(meta);
                }
            } else {
                Material mat = Material.matchMaterial(itemId);
                check = mat != null ? new ItemStack(mat) : null;
            }

            if (check != null) {
                check.setAmount(amount);
                expected.put(key.charAt(0), check);
            }
        }

        boolean matches = true;
        for (int i = 0; i < pattern.size(); i++) {
            String line = pattern.get(i);
            for (int j = 0; j < line.length(); j++) {
                char c = line.charAt(j);
                if (c == 'X') continue;
                int slot = inputSlots[i * 3 + j];
                ItemStack required = expected.get(c);
                ItemStack actual = inv.getItem(slot);
                if (!isMatch(required, actual)) {
                    matches = false;
                    break;
                }
            }
            if (!matches) break;
        }

        Material color = matches ? Material.LIME_WOOL : Material.RED_WOOL;
        String prefix = matches ? "craft-button.ready" : "craft-button.not-ready";

        String name = ChatColor.translateAlternateColorCodes('&',
                messages.getString(prefix + ".name", (matches ? ChatColor.GREEN : ChatColor.RED) + "Craft Minion"));

        List<String> lore = messages.getStringList(prefix + ".lore");
        List<String> coloredLore = new ArrayList<>();
        for (String line : lore) {
            coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }

        ItemStack craft = new ItemStack(color);
        ItemMeta meta = craft.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (!coloredLore.isEmpty()) meta.setLore(coloredLore);
            craft.setItemMeta(meta);
        }

        ItemStack old = inv.getItem(31);
        if (old == null || old.getType() != color) {
            inv.setItem(31, craft);
            if (matches) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.2f);
            }
        }
    }


    private void handleCraft(Player player, Inventory inv) {
        FileConfiguration config = ConfigHandler.getCrafting();
        FileConfiguration messages = ConfigHandler.getMessages();
        String recipeKey = lastRecipeKey.get(player.getUniqueId());
        if (recipeKey == null) return;

        ConfigurationSection recipeSection = config.getConfigurationSection(recipeKey);
        if (recipeSection == null) return;

        List<String> pattern = recipeSection.getStringList("pattern");
        ConfigurationSection ingredients = recipeSection.getConfigurationSection("ingredients");
        if (ingredients == null) return;

        Map<Character, ItemStack> expected = new HashMap<>();
        for (String key : ingredients.getKeys(false)) {
            String itemId = ingredients.getString(key + ".item");
            int amount = ingredients.getInt(key + ".amount");

            ItemStack check;
            if (itemId.contains(":")) {
                CustomStack cs = CustomStack.getInstance(itemId);
                check = cs != null ? cs.getItemStack().clone() : null;
            } else if (itemId.startsWith("minion#")) {
                check = new ItemStack(Material.PLAYER_HEAD);
                ItemMeta meta = check.getItemMeta();
                if (meta != null) {
                    String minionId = itemId.substring(7);
                    meta.getPersistentDataContainer().set(minionKey, PersistentDataType.STRING, minionId);
                    check.setItemMeta(meta);
                }
            } else {
                Material mat = Material.matchMaterial(itemId);
                check = mat != null ? new ItemStack(mat) : null;
            }
            if (check != null) {
                check.setAmount(amount);
                expected.put(key.charAt(0), check);
            }
        }

        boolean matches = true;
        for (int i = 0; i < pattern.size(); i++) {
            String line = pattern.get(i);
            for (int j = 0; j < line.length(); j++) {
                char c = line.charAt(j);
                if (c == 'X') continue;
                int slot = inputSlots[i * 3 + j];
                ItemStack required = expected.get(c);
                ItemStack actual = inv.getItem(slot);
                if (!isMatch(required, actual)) {
                    matches = false;
                    break;
                }
            }
            if (!matches) break;
        }

        if (!matches) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    messages.getString("crafting.failed", "&cYou have not placed the required items in the correct slots.")));
            return;
        }

        for (int i = 0; i < pattern.size(); i++) {
            String line = pattern.get(i);
            for (int j = 0; j < line.length(); j++) {
                char c = line.charAt(j);
                if (c == 'X') continue;
                int slot = inputSlots[i * 3 + j];
                ItemStack item = inv.getItem(slot);
                int removeAmount = expected.get(c).getAmount();
                if (item != null) {
                    item.setAmount(item.getAmount() - removeAmount);
                    inv.setItem(slot, item.getAmount() > 0 ? item : null);
                }
            }
        }

        String resultId = recipeSection.getString("result.item");
        int resultAmount = recipeSection.getInt("result.amount", 1);

        if (resultId.startsWith("minion#")) {
            String[] parts = resultId.split("#");
            if (parts.length < 4) {
                player.sendMessage(ChatColor.RED + "Invalid minion result format: " + resultId);
                return;
            }
            String material = parts[1];
            String type = parts[2];
            int level = Integer.parseInt(parts[3]);

            String command = String.format("topminion giveminion %s %s %d %s", type, material, level, player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        } else {
            // Handle vanilla or ItemsAdder items
            ItemStack resultItem;
            if (resultId.contains(":")) {
                CustomStack cs = CustomStack.getInstance(resultId);
                resultItem = cs != null ? cs.getItemStack().clone() : null;
            } else {
                Material mat = Material.matchMaterial(resultId.toUpperCase());
                resultItem = mat != null ? new ItemStack(mat) : null;
            }

            if (resultItem == null || resultItem.getType() == Material.AIR) {
                player.sendMessage(ChatColor.RED + "Invalid result item: " + resultId);
                return;
            }

            resultItem.setAmount(resultAmount);
            player.getInventory().addItem(resultItem);
        }


        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                messages.getString("crafting.success", "&a✔ Minion successfully crafted!")));

        player.closeInventory();
    }


    private boolean isMatch(ItemStack required, ItemStack actual) {
        if (required == null || actual == null) return false;

        String requiredId = getRawId(required);
        String actualId = getRawId(actual);

        if (requiredId.startsWith("minion#")) {
            String expected = requiredId.substring(7); // Expects "ZOMBIE#SLAYER#1"
            if (actual.getType() != Material.PLAYER_HEAD || !actual.hasItemMeta()) return false;

            PersistentDataContainer container = actual.getItemMeta().getPersistentDataContainer();
            String value = container.get(minionKey, PersistentDataType.STRING);

            if (value == null) return false;

            // DEBUG
            //System.out.println("[Crafting] Required Minion ID: " + expected);
            //System.out.println("[Crafting] Actual Minion ID:   " + value);

            return value.equalsIgnoreCase(expected) && actual.getAmount() >= required.getAmount();
        }






        CustomStack requiredCustom = CustomStack.byItemStack(required);
        CustomStack actualCustom = CustomStack.byItemStack(actual);

        if (requiredCustom != null) {
            return actualCustom != null &&
                    requiredCustom.getNamespacedID().equals(actualCustom.getNamespacedID()) &&
                    actual.getAmount() >= required.getAmount();
        }

        return required.getType() == actual.getType() &&
                actual.getAmount() >= required.getAmount();
    }

    private String getRawId(ItemStack item) {
        if (item == null) return "";

        CustomStack cs = CustomStack.byItemStack(item);
        if (cs != null) return cs.getNamespacedID();

        if (item.getType() == Material.PLAYER_HEAD && item.hasItemMeta()) {
            PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
            String value = container.get(minionKey, PersistentDataType.STRING);
            if (value != null) return "minion#" + value;
        }

        return item.getType().name();
    }
}