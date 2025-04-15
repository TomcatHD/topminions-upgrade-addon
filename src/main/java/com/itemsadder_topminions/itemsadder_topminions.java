package com.itemsadder_topminions;

import com.itemsadder_topminions.command.GlobalCraftCommand;
import com.itemsadder_topminions.command.MinionCraftCommand;
import com.itemsadder_topminions.command.ReloadCommand;
import com.itemsadder_topminions.gui.MinionCraftingListener;
import com.itemsadder_topminions.gui.MinionCraftingMenu;
import com.itemsadder_topminions.gui.UpgradeMenuListener;
import com.itemsadder_topminions.gui.GlobalCraftingMenu;
import com.itemsadder_topminions.listener.ItemsAdderProvider;
import com.itemsadder_topminions.listener.MinionUpgradeListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import com.itemsadder_topminions.config.ConfigHandler;

public class itemsadder_topminions extends JavaPlugin {

    private static itemsadder_topminions instance;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        ConfigHandler.init(this); // Load all config files through new handler

        Bukkit.getPluginManager().registerEvents(new ItemsAdderProvider(), this);
        Bukkit.getPluginManager().registerEvents(new UpgradeMenuListener(), this);
        Bukkit.getPluginManager().registerEvents(new MinionUpgradeListener(), this);
        Bukkit.getPluginManager().registerEvents(new MinionCraftingMenu(), this);
        Bukkit.getPluginManager().registerEvents(new MinionCraftingListener(), this);
        Bukkit.getPluginManager().registerEvents(new GlobalCraftingMenu(), this);


        getCommand("iatopminions").setExecutor(new ReloadCommand());
        getCommand("globalcraft").setExecutor(new GlobalCraftCommand());
        getCommand("minioncraft").setExecutor(new MinionCraftCommand());



        getLogger().info(ChatColor.GREEN + "ItemsAdder_TopMinions has been enabled.");
    }

    public static itemsadder_topminions getInstance() {
        return instance;
    }
}
