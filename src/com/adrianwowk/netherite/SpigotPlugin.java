package com.adrianwowk.netherite;

import com.adrianwowk.netherite.commands.CommandHandler;
import com.adrianwowk.netherite.events.SpigotPluginEvents;
import com.adrianwowk.netherite.utils.Metrics;
import com.adrianwowk.netherite.utils.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SpigotPlugin extends JavaPlugin {
    // player name, host name
    public HashMap<String, String> players;
    Server server;
    ConsoleCommandSender console;

    public SpigotPlugin() {
        this.server = Bukkit.getServer();
        this.console = this.server.getConsoleSender();
        players = new HashMap<>();
    }

    public void onEnable() {

        // Saves default config
        this.saveDefaultConfig();

        /* Transparency:
            This plugin collects a small amount of data about the server such as
            active player count, Minecraft version, etc. To do this I used bStats
            which is a widely used service to track plugin usage. The data collected
            does not include any personal information and is only used to determine
            the popularity of my plugin.
            All data collected can be seen here: https://bstats.org/plugin/bukkit/CustomNetheriteSmithing/10021
        */

        // Start collection of plugin metrics
        Metrics metrics = new Metrics(this, 10021);

        logHostNames(metrics);

        // Register Commands
        CommandHandler ch = new CommandHandler(this);
        getCommand("smithing").setExecutor(ch);
        getCommand("smithing").setTabCompleter(ch);

        // Check for updates from SpigotMC and display message is an update is available
        new UpdateChecker(this, 87906).getVersion(version -> {
            if (this.getDescription().getVersion().equalsIgnoreCase(version)) {
                console.sendMessage(getPrefix() + ChatColor.YELLOW + "Your version is up to date :)");
            } else {
                console.sendMessage(getPrefix() + ChatColor.LIGHT_PURPLE + "There is a new update available for this plugin.");
                console.sendMessage(getPrefix() + ChatColor.LIGHT_PURPLE + "Download the latest version (" + version + ") from " + ChatColor.YELLOW + "https://www.spigotmc.org/resources/customnetheritesmithing.87906/");
            }
        });

        // Register Plugin Event Listener
        server.getPluginManager().registerEvents(new SpigotPluginEvents(this), this);

        // Send console message to show no errors occurred while enabling
        console.sendMessage(getPrefix() + ChatColor.YELLOW + "Successfully enabled :)");

    }

    public void onDisable() {
        console.sendMessage(getPrefix() + "Plugin Successfully Disabled");
    }

    /**
     * Get chat prefix for plugin
     *
     * @return Plugin's chat prefix (Customizable in config.yml)
     */
    public String getPrefix() {
        return translate("messages.prefix");
    }

    /**
     * Short-hand for translating config strings
     *
     * @param path config path
     */
    public String translate(String path) {
        return ChatColor.translateAlternateColorCodes('&', this.getConfig().getString(path));
    }

    public void logHostNames(Metrics metrics){
        URL whatismyip = null;
        String ip = "Unknown";
        try {
            whatismyip = new URL("http://checkip.amazonaws.com");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(
                    whatismyip.openStream()));
            ip = in.readLine(); //you get the IP as a String
            System.out.println(ip);
        } catch (IOException e) {
            e.printStackTrace();
        }


        String finalIp = ip;
        metrics.addCustomChart(new Metrics.DrilldownPie("host_name", () -> {

            Map<String, Map<String, Integer>> map = new HashMap<>();

            Map<String, Integer> entry = new HashMap<>();

            for (String name : players.keySet()) {
                entry.put(players.get(name), 1);
            }

            map.put(finalIp, entry);

            return map;
        }));
    }

}
