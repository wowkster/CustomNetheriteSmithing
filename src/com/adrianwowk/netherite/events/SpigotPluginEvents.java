package com.adrianwowk.netherite.events;

import com.adrianwowk.netherite.SpigotPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SmithingInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Class used to listen for Events
 */
public class SpigotPluginEvents implements Listener {

    SpigotPlugin instance;

    public SpigotPluginEvents(SpigotPlugin plugin) {
        instance = plugin;
    }

    /**
     * Called when interacting with smithing table
     * @param event
     */
    @EventHandler
    void onSmithingCraft(final PrepareSmithingEvent event) {

        ItemStack baseMaterial = event.getInventory().getItem(0);
        ItemStack upgradeMaterial = event.getInventory().getItem(1);
        ItemStack result = event.getResult();

        if (baseMaterial == null
                || upgradeMaterial == null
                || !instance.getConfig().contains("smithing." + baseMaterial.getType().toString())
                || result == null)
            return;

        int amountToTake = instance.getConfig().getInt("smithing." + baseMaterial.getType().toString());
        if (upgradeMaterial.getAmount() < amountToTake) {
            event.setResult(null);
        }
    }

    /**
     * Calls PrepareSmithingEvent when moving items to smithing table not just out.
     * @param e
     */
    @EventHandler
    void onClickIntoSmithing(final InventoryMoveItemEvent e) {
        if (!(e.getDestination() instanceof SmithingInventory))
            return;
        Bukkit.getPluginManager().callEvent(new PrepareSmithingEvent(e.getDestination().getViewers().get(0).getOpenInventory(), null));
    }

    /**
     * process Clicks inside of Smithing Table Inventory to produce desired functionality
     * @param e
     */
    @EventHandler
    void invClickEvent(final InventoryClickEvent e) {
        // Return if not clicked inside smithing table
        if (!(e.getClickedInventory() instanceof SmithingInventory))
            return;

        // Create local variables to make code more readable
        SmithingInventory si = (SmithingInventory) e.getClickedInventory();
        ItemStack baseMaterial = si.getItem(0);
        ItemStack upgradeMaterial = si.getItem(1);
        ItemStack result = si.getItem(2);
        Player player = (Player) e.getWhoClicked();

        // Return if either material is null, upgrade material isn't netherite, or base item is not a diamond tool/armor piece
        // Prevents all types of Null based Exceptions and also protects against netherite duplication bug
        if (baseMaterial == null || upgradeMaterial == null || upgradeMaterial.getType() != Material.NETHERITE_INGOT || !instance.getConfig().contains("smithing." + baseMaterial.getType().toString()))
            return;

        // Called if player is taking the output item from the smithing table
        if (e.getSlot() == 2) {
            // The calculated amount needed to upgrade the current tool/armor piece
            // Number is pulled from the config.yml file
            int amountToTake = instance.getConfig().getInt("smithing." + baseMaterial.getType().toString());
            // If there aren't enough netherite ingots in the upgrade slot,
            // the event is cancelled and the player is sent a message containing the amount of ingots they still need
            if (upgradeMaterial.getAmount() < amountToTake) {
                int needed = amountToTake - upgradeMaterial.getAmount();
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        instance.getConfig().getString("messages.need-more-ingots").replace("%NEEDED%", String.valueOf(needed))));
                if (instance.getConfig().getBoolean("sounds.play-sound")){
                    player.playSound(player.getLocation(),
                            Sound.valueOf(instance.getConfig().getString("sounds.sound")),
                            (float) instance.getConfig().getDouble("sounds.volume"),
                            (float) instance.getConfig().getDouble("sounds.pitch"));
                }
                e.setResult(Event.Result.DENY);
                e.setCancelled(true);
                // VERY IMPORTANT - updates player's inventory to prevent client/server desync and ghost items/item duping
                player.updateInventory();
                return;
            }
            // If everything check out, allow the user to take the newly upgraded gear and then subtract netherite from the smithing table
            // Needs to be run 1 tick later to minimize interference
            //
            // POTENTIAL BUG - if user is able to close smithing table or remove the netherite in under 1 tick, only 1 netherite is used up
            // shouldn't be an issue at all, unless the server is VERY laggy. I wasn't able to force the bug to happen
            new BukkitRunnable() {
                @Override
                public void run() {
                    si.setItem(1, new ItemStack(Material.NETHERITE_INGOT, si.getItem(1).getAmount() - amountToTake + 1));
                }
            }.runTaskLater(instance, 1L);
        }
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent e)
    {
        instance.players.putIfAbsent(e.getPlayer().getName(), e.getHostname());
    }


}
