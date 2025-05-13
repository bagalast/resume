package ru.bagalast.bestdevrwmarket;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MarketMyMenu extends Menu {
    private final MarketManager manager;
    private final FileConfiguration config;
    private int currentPage = 0;

    private static final int[] ITEM_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    public MarketMyMenu(Player player, MarketManager manager) {
        super(player, "Мои лоты", 54);
        this.manager = manager;
        this.config = BestDevRWMarket.getInstance().getConfig();
    }

    @Override
    public void open() {
        initialize();
        super.open();
    }

    @Override
    public void initialize() {
        List<MarketListing> myListings = manager.getListings().stream()
                .filter(l -> l.getSeller().equalsIgnoreCase(player.getName()))
                .collect(Collectors.toList());

        if (myListings.isEmpty()) {
            setupEmptyMenu();
            return;
        }

        int itemsPerPage = ITEM_SLOTS.length;
        int totalPages = (int) Math.ceil(myListings.size() / (double) itemsPerPage);

        if (currentPage >= totalPages) {
            currentPage = totalPages - 1;
        }
        if (currentPage < 0) {
            currentPage = 0;
        }

        int startIdx = currentPage * itemsPerPage;
        int endIdx = Math.min(startIdx + itemsPerPage, myListings.size());

        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, null);
        }
        clickActions.clear();

        for (int i = startIdx; i < endIdx; i++) {
            MarketListing listing = myListings.get(i);
            ItemStack item = new ItemStack(listing.getMaterial(), 1);
            ItemMeta meta = item.getItemMeta();

            List<String> lore = new ArrayList<>();
            lore.add("Цена за предмет: " + listing.getPrice() + " за штуку");
            lore.add("Количество: " + listing.getAmount());
            lore.add("");
            lore.add("§aЛКМ - забрать предмет");

            meta.setLore(lore);
            item.setItemMeta(meta);

            int slotIndex = i - startIdx;
            int slot = ITEM_SLOTS[slotIndex];

            setItem(slot, item, e -> {
                if (takeBackItem(listing)) {
                    String message = BestDevRWMarket.getInstance().getConfig().getString("messages.item-returned");
                    if (message != null){
                        player.sendMessage(Utils.color(message));
                    }

                    currentPage = 0;
                    initialize();
                } else {
                    String message = BestDevRWMarket.getInstance().getConfig().getString("messages.no-space");
                    if (message != null){
                        player.sendMessage(Utils.color(message));
                    }
                }
            });
        }

        if (currentPage > 0) {
            setItem(45, createPreviousButton(), e -> {
                currentPage--;
                initialize();
            });
        }

        if (currentPage < totalPages - 1) {
            setItem(53, createNextButton(), e -> {
                currentPage++;
                initialize();
            });
        }
    }

    private boolean takeBackItem(MarketListing listing) {
        if (!hasInventorySpace(player, listing.getMaterial(), listing.getAmount())) {
            return false;
        }

        player.getInventory().addItem(new ItemStack(
                listing.getMaterial(),
                listing.getAmount()
        ));

        manager.removeListing(listing.getId());
        return true;
    }

    private void setupEmptyMenu() {
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, null);
        }
        clickActions.clear();
    }

    private ItemStack createPreviousButton() {
        String path = "arrows.left";
        ItemStack item = new ItemStack(Material.valueOf(config.getString(path + ".material")));
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(Utils.color(config.getString(path + ".name")));
        //meta.setLore(Collections.singletonList(Utils.color("&7Текущая страница: &e" + (currentPage + 1))));

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createNextButton() {
        String path = "arrows.right";
        ItemStack item = new ItemStack(Material.valueOf(config.getString(path + ".material")));
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(Utils.color(config.getString(path + ".name")));
        //meta.setLore(Collections.singletonList(Utils.color("&7Текущая страница: &e" + (currentPage + 1))));

        item.setItemMeta(meta);
        return item;
    }

    private boolean hasInventorySpace(Player player, Material material, int amount) {
        int remaining = amount;
        ItemStack[] contents = player.getInventory().getStorageContents();

        for (ItemStack item : contents) {
            if (remaining <= 0) break;

            if (item == null || item.getType() == Material.AIR) {
                remaining -= material.getMaxStackSize();
            } else if (item.getType() == material && item.getAmount() < item.getMaxStackSize()) {
                remaining -= (item.getMaxStackSize() - item.getAmount());
            }
        }

        return remaining <= 0;
    }
}
