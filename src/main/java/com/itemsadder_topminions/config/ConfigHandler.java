package com.itemsadder_topminions.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;

public class ConfigHandler {

    private static FileConfiguration upgradesConfig;
    private static FileConfiguration craftingConfig;
    private static FileConfiguration messagesConfig;
    private static FileConfiguration itemsConfig;

    private static File upgradesFile;
    private static File craftingFile;
    private static File messagesFile;
    private static File itemsFile;

    public static void init(Plugin plugin) {
        upgradesFile = new File(plugin.getDataFolder(), "upgrades.yml");
        craftingFile = new File(plugin.getDataFolder(), "crafting.yml");
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        itemsFile = new File(plugin.getDataFolder(), "items.yml");

        if (!upgradesFile.exists()) plugin.saveResource("upgrades.yml", false);
        if (!craftingFile.exists()) plugin.saveResource("crafting.yml", false);
        if (!messagesFile.exists()) plugin.saveResource("messages.yml", false);
        if (!itemsFile.exists()) plugin.saveResource("items.yml", false);

        reload();
    }

    public static void reload() {
        upgradesConfig = YamlConfiguration.loadConfiguration(upgradesFile);
        craftingConfig = YamlConfiguration.loadConfiguration(craftingFile);
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);
    }

    public static FileConfiguration getUpgrades() {
        return upgradesConfig;
    }

    public static FileConfiguration getCrafting() {
        return craftingConfig;
    }

    public static FileConfiguration getMessages() {
        return messagesConfig;
    }

    public static FileConfiguration getItems() {
        return itemsConfig;
    }
}
