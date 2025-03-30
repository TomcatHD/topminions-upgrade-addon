package com.itemsadder_topminions;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class itemsadder_topminions extends JavaPlugin {

    private static itemsadder_topminions instance;

    private static FileConfiguration upgradesConfig;
    private static FileConfiguration craftingConfig;
    private static FileConfiguration messagesConfig;
    private static FileConfiguration itemsConfig;

    private static File upgradesFile;
    private static File craftingFile;
    private static File messagesFile;
    private static File itemsFile;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        loadConfigs();

        Bukkit.getPluginManager().registerEvents(new ItemsAdderProvider(), this);
        Bukkit.getPluginManager().registerEvents(new UpgradeMenuListener(), this);
        Bukkit.getPluginManager().registerEvents(new MinionUpgradeListener(), this);
        Bukkit.getPluginManager().registerEvents(new MinionCraftingMenu(), this);
        Bukkit.getPluginManager().registerEvents(new MinionCraftingListener(), this);

        getCommand("iatopminions").setExecutor(new ReloadCommand());

        getLogger().info(ChatColor.GREEN + "ItemsAdder_TopMinions has been enabled.");
    }

    public static itemsadder_topminions getInstance() {
        return instance;
    }

    public static void loadConfigs() {
        upgradesFile = new File(instance.getDataFolder(), "upgrades.yml");
        craftingFile = new File(instance.getDataFolder(), "crafting.yml");
        messagesFile = new File(instance.getDataFolder(), "messages.yml");
        itemsFile = new File(instance.getDataFolder(), "items.yml");

        if (!upgradesFile.exists()) instance.saveResource("upgrades.yml", false);
        if (!craftingFile.exists()) instance.saveResource("crafting.yml", false);
        if (!messagesFile.exists()) instance.saveResource("messages.yml", false);
        if (!itemsFile.exists()) instance.saveResource("items.yml", false);

        upgradesConfig = YamlConfiguration.loadConfiguration(upgradesFile);
        craftingConfig = YamlConfiguration.loadConfiguration(craftingFile);
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);
    }

    public static void reloadConfigs() {
        upgradesConfig = YamlConfiguration.loadConfiguration(upgradesFile);
        craftingConfig = YamlConfiguration.loadConfiguration(craftingFile);
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);
    }

    public static FileConfiguration getUpgradesConfig() {
        return upgradesConfig;
    }

    public static FileConfiguration getCraftingConfig() {
        return craftingConfig;
    }

    public static FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }

    public static FileConfiguration getItemsConfig() {
        return itemsConfig;
    }
}
