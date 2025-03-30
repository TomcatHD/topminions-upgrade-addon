package com.itemsadder_topminions.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import com.itemsadder_topminions.config.ConfigHandler;

public class ReloadCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("itemsadder_topminions.reload")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to run this command.");
            return true;
        }

        ConfigHandler.reload();
        sender.sendMessage(ChatColor.GREEN + "✔ Configs reloaded: upgrades.yml, crafting.yml, messages.yml, items.yml");
        return true;
    }
}
