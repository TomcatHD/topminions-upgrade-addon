package com.itemsadder_topminions.command;

import com.itemsadder_topminions.gui.MinionCraftingMenu;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MinionCraftCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (args.length != 3) {
            player.sendMessage(ChatColor.RED + "Usage: /minioncraft <type> <material> <level>");
            return true;
        }

        String type = args[0];
        String material = args[1];
        int level;

        try {
            level = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Level must be a number.");
            return true;
        }

        MinionCraftingMenu.openCraftingGUI(player, type, material, level);
        MinionCraftingMenu.markManualOpen(player);

        return true;
    }
}
