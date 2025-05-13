package ru.bagalast.bestdevrwmarket;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

public class MarketMainMenu extends Menu {
    private final MarketManager manager;
    private final FileConfiguration config;
    private int currentPage = 0;

    private static final int[] ITEM_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    public MarketMainMenu(MarketManager manager, Player player) {
        super(player, "Рынок", 54);
        if (manager == null) {
            throw new IllegalArgumentException("MarketManager не должен быть null!");
        }
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

        List<MarketListing> listings = manager.getListings().stream()
                .filter(l -> manager.getAllowedItems().contains(l.getMaterial()))
                .collect(Collectors.toList());


        List<Material> materialsOnSale = listings.stream()
                .map(MarketListing::getMaterial)
                .distinct()
                .collect(Collectors.toList());

        if (materialsOnSale.isEmpty()) {
            setupEmptyMenu();
            return;
        }

        int itemsPerPage = ITEM_SLOTS.length;
        int totalPages = (int) Math.ceil(materialsOnSale.size() / (double) itemsPerPage);

        if (currentPage >= totalPages) {
            currentPage = totalPages - 1;
        }
        if (currentPage < 0) {
            currentPage = 0;
        }

        int startIdx = currentPage * itemsPerPage;
        int endIdx = Math.min(startIdx + itemsPerPage, materialsOnSale.size());

        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, null);
        }
        clickActions.clear();

        for (int i = startIdx; i < endIdx; i++) {
            Material material = materialsOnSale.get(i);
            MarketStats marketStats = manager.getStats(material);

            List<String> lore = new ArrayList<>();
            List<String> cfgLore = config.getStringList("market-items.lore");
            for (String line : cfgLore) {
                String replaced = line
                        .replace("{amount}", String.valueOf(marketStats.getCount()))
                        .replace("{min}", String.format("%.2f", marketStats.getMinPrice()))
                        .replace("{avg}", String.format("%.2f", marketStats.getAvgPrice()));
                lore.add(Utils.color(replaced));
            }

            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            meta.setLore(lore);
            item.setItemMeta(meta);

            int slotIndex = i - startIdx;
            int slot = ITEM_SLOTS[slotIndex];

            setItem(slot, item, e -> {
                e.setCancelled(true);
                int countExcludingPlayer = manager.getCountExcludingPlayer(material, player.getName());
                if (countExcludingPlayer <= 0) {
                    String msg = config.getString("messages.only-own-lots");
                    if (msg == null) msg = "&cЭтот предмет продаёшь только ты, купить нельзя.";
                    player.sendMessage(Utils.color(msg));
                    player.closeInventory();
                    return;
                }
                new MarketBuyMenu(player, material, manager).open();
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

    private void setupEmptyMenu() {
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, null);
        }
        clickActions.clear();

        String path = "empty-market";
        int slot = config.getInt(path + ".slot");
        Material material = Material.valueOf(config.getString(path + ".material"));
        String name = Utils.color(config.getString(path + ".name"));
        List<String> lore = new ArrayList<>();
        List<String> cfgLore = config.getStringList(path + ".lore");
        for (String line : cfgLore) {
            lore.add(Utils.color(line));
        }
        ItemStack emptyItem = new ItemStack(material);
        ItemMeta meta = emptyItem.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        emptyItem.setItemMeta(meta);

        setItem(slot, emptyItem, null);
    }

    private ItemStack createPreviousButton() {
        String path = "arrows.left";
        ItemStack item = new ItemStack(Material.valueOf(config.getString(path + ".material")));
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(Utils.color(config.getString(path + ".name")));
        meta.setLore(Collections.singletonList(Utils.color("&7Текущая страница: &e" + (currentPage + 1))));

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createNextButton() {
        String path = "arrows.right";
        ItemStack item = new ItemStack(Material.valueOf(config.getString(path + ".material")));
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(Utils.color(config.getString(path + ".name")));
        meta.setLore(Collections.singletonList(Utils.color("&7Текущая страница: &e" + (currentPage + 1))));

        item.setItemMeta(meta);
        return item;
    }
}
