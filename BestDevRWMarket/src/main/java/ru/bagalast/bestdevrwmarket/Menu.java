package ru.bagalast.bestdevrwmarket;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Consumer;

import java.util.HashMap;
import java.util.Map;

public abstract class Menu implements InventoryHolder {
    protected Inventory inventory;
    protected Player player;
    protected Map<Integer, Consumer<InventoryClickEvent>> clickActions;

    public Menu(Player player, String title, int size) {
        this.player = player;
        this.inventory = Bukkit.createInventory(this, size, title);
        this.clickActions = new HashMap<>();
    }

    public abstract void initialize();

    public void open() {
        player.openInventory(inventory);
    }

    protected void setItem(int slot, ItemStack item, Consumer<InventoryClickEvent> action) {
        inventory.setItem(slot, item);
        if (action != null) {
            clickActions.put(slot, action);
        }
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (clickActions.containsKey(slot)) {
            clickActions.get(slot).accept(event);
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
