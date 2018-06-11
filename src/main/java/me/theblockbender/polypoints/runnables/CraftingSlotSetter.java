package me.theblockbender.polypoints.runnables;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class CraftingSlotSetter extends BukkitRunnable implements Listener {

    private Set<UUID> interactingWith = new HashSet<>();

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR)
                continue;
            Inventory topInventory = player.getOpenInventory().getTopInventory();
            if (topInventory == null)
                continue;
            if (topInventory.getType() != InventoryType.CRAFTING)
                continue;
            if (interactingWith.contains(player.getUniqueId()))
                continue;
            // This would be the point to load the player's stored items.
            setItems(topInventory);
        }
    }

    private void setItems(Inventory topInventory) {
        topInventory.setItem(1, new ItemStack(Material.WOOD, 1));
        topInventory.setItem(2, new ItemStack(Material.SAND, 1));
        topInventory.setItem(3, new ItemStack(Material.GRASS, 1));
        topInventory.setItem(4, new ItemStack(Material.GRAVEL, 1));
    }

    private void removeItems(Inventory inv) {
        inv.setItem(0, null);
        inv.setItem(1, null);
        inv.setItem(2, null);
        inv.setItem(3, null);
        inv.setItem(4, null);
    }

    @EventHandler
    public void onPlayerClickCraftingSlot(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player))
            return;
        if (event.getClickedInventory() == null)
            return;
        Player player = (Player) event.getWhoClicked();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR)
            return;
        Inventory topInventory = player.getOpenInventory().getTopInventory();
        if (topInventory == null)
            return;
        if (topInventory.getType() != InventoryType.CRAFTING)
            return;
        interactingWith.add(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerCloseInventory(InventoryCloseEvent event) {
        interactingWith.remove(event.getPlayer().getUniqueId());
        if (event.getInventory().getType() != InventoryType.CRAFTING)
            return;
        Player player = (Player) event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR)
            return;
        // This would be the point to store the current items.
        removeItems(event.getInventory());
    }

    @EventHandler
    public void onPlayerCraft(PrepareItemCraftEvent event) {
        Inventory inventory = event.getInventory();
        if (inventory == null)
            return;
        if (inventory.getType() != InventoryType.CRAFTING)
            return;
        event.getInventory().setItem(0, null);
    }
}
