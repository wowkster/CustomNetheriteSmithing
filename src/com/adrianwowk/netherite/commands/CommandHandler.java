package com.adrianwowk.netherite.commands;

import com.adrianwowk.netherite.SpigotPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.ArrayList;
import java.util.List;

/**
 * Class used to process custom commands
 */
public class CommandHandler implements TabExecutor {

    private SpigotPlugin instance;

    /**
     * Create new instance of CommandHandler
     * @param plugin the instance of the Main Class (used to easily send data and get config settings non-statically)
     */
    public CommandHandler(SpigotPlugin plugin){
        this.instance = plugin;
    }

    /**
     * Method called when "/smithing" command is sent
     * Sends different customizable messages
     * Used for "/smithing reload" command
     */
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // Check if command sent was "/smithing"
        if (label.equalsIgnoreCase("smithing")) {
            // Check for permissions
            if (!sender.hasPermission("smithing.reload"))
                sender.sendMessage(instance.getPrefix() + instance.translate("messages.no-permission"));
            else if (args.length == 0)
                // Send invalid argument message if command is sent without arguments
                sender.sendMessage(instance.getPrefix() + instance.translate("messages.reload.invalid-argument"));
            else if (args.length > 0) {
                if (args[0].equalsIgnoreCase("reload")) {
                    // Send reload message and reload config file
                    sender.sendMessage(instance.getPrefix() + instance.translate("messages.reload.done"));
                    instance.reloadConfig();
                } else
                    // Send invalid argument message if command is sent with unknown argument(s)
                    sender.sendMessage(instance.getPrefix() + instance.translate("messages.reload.invalid-argument"));
            }
            return true;
        }
        return false;
    }

    /**
     * Used to autocomplete "/smithing" command
     */
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        // Check if command is "/smithing"
        if (alias.equalsIgnoreCase("smithing")) {
            List<String> list = new ArrayList<>();
            // Add reload to autocomplete is sender has valid permissions
            if (sender.hasPermission("smithing.reload"))
                list.add("reload");
            return list;
        }
        return null;
    }

}
