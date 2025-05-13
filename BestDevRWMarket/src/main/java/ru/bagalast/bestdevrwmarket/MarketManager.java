package ru.bagalast.bestdevrwmarket;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class MarketManager {
    private final BestDevRWMarket plugin;
    private final Set<Material> allowedItems;

    public MarketManager(BestDevRWMarket plugin) {
        this.plugin = plugin;
        this.allowedItems = loadAllowedItems();
    }

    public Set<Material> getAllowedItems() {
        return allowedItems;
    }

    private Set<Material> loadAllowedItems() {
        Set<Material> items = new HashSet<>();
        FileConfiguration config = plugin.getAllowedData();

        if (!config.contains("allowed-items")) {
            plugin.getLogger().warning("Не найдена секция allowed-items в конфиге");
            return getDefaultItems();
        }

        for (String materialName : config.getStringList("allowed-items")) {
            try {
                items.add(Material.valueOf(materialName.toUpperCase()));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Неверный материал: " + materialName);
            }
        }

        if (items.isEmpty()) {
            plugin.getLogger().info("Используются стандартные материалы");
            return getDefaultItems();
        }

        return items;
    }

    private Set<Material> getDefaultItems() {
        return new HashSet<>(Arrays.asList(
                Material.DIAMOND,
                Material.GOLD_INGOT,
                Material.IRON_INGOT
        ));
    }

    public void openMarketMenu(Player player) {
        MarketMainMenu menu = new MarketMainMenu(this, player);
        menu.open();
    }

    public void addListing(Player player, ItemStack itemStack, double price) {
        FileConfiguration data = plugin.getData();

        int lastId = data.getInt("last-id", 0) + 1;
        data.set("last-id", lastId);

        String path_section = "listings." + lastId;
        data.set(path_section + ".seller", player.getName());
        data.set(path_section + ".material", itemStack.getType().name());
        data.set(path_section + ".amount", itemStack.getAmount());
        data.set(path_section + ".price", price);

        plugin.saveData();
    }

    public List<MarketListing> getListings() {
        List<MarketListing> listings = new ArrayList<>();
        FileConfiguration data = plugin.getData();

        if (data.getConfigurationSection("listings") == null) return listings;

        for (String id : data.getConfigurationSection("listings").getKeys(false)) {
            try {
                String seller = data.getString("listings." + id + ".seller");
                Material material = Material.valueOf(data.getString("listings." + id + ".material"));
                int amount = data.getInt("listings." + id + ".amount");
                double price = data.getDouble("listings." + id + ".price");

                listings.add(new MarketListing(Integer.parseInt(id), seller, material, amount, price));
            } catch (Exception e) {
                plugin.getLogger().warning("Ошибка загрузки листинга " + id + ": " + e.getMessage());
            }
        }
        return listings;
    }

    public MarketStats getStats(Material material) {
        List<MarketListing> listings = getListings().stream()
                .filter(l -> l.getMaterial() == material)
                .collect(Collectors.toList());

        if (listings.isEmpty()) {
            return new MarketStats(0, 0, 0);
        }

        int count = listings.stream().mapToInt(MarketListing::getAmount).sum();
        double minPrice = listings.stream()
                .mapToDouble(MarketListing::getPrice)
                .min().orElse(0);

        double totalValue = listings.stream()
                .mapToDouble(l -> l.getPrice() * l.getAmount())
                .sum();
        double avgPrice = totalValue / count;

        return new MarketStats(count, minPrice, avgPrice);
    }

    public int getCountExcludingPlayer(Material material, String playerName) {
        return getListings().stream()
                .filter(l -> l.getMaterial() == material)
                .filter(l -> !l.getSeller().equalsIgnoreCase(playerName))
                .mapToInt(MarketListing::getAmount)
                .sum();
    }

    public void removeListing(int id) {
        plugin.getData().set("listings." + id, null);
        plugin.saveData();
    }

    public boolean purchaseItem(Player player, Material material, List<PriceLore> priceLores) {
        double total = priceLores.stream().mapToDouble(t -> t.total).sum();
        Economy economy = plugin.getEconomy();

        if (!economy.has(player, total)) {
            player.sendMessage(Utils.color(plugin.getConfig().getString("messages.no-balance")));
            return false;
        }
        for (PriceLore priceLore : priceLores) {
            MarketListing listing = getCheapestListing(material);
            if (listing == null) return false;
            int take = Math.min(priceLore.amount, listing.getAmount());
            economy.withdrawPlayer(player, priceLore.total);
            economy.depositPlayer(Bukkit.getOfflinePlayer(listing.getSeller()), priceLore.total);

            updateListingAmount(listing, listing.getAmount() - take);
            player.getInventory().addItem(new ItemStack(material, take));
        }
        return true;
    }

    private void updateListingAmount(MarketListing listing, int newAmount) {
        if (newAmount <= 0) {
            plugin.getData().set("listings." + listing.getId(), null);
        } else {
            plugin.getData().set("listings." + listing.getId() + ".amount", newAmount);
        }
        plugin.saveData();
    }

    private MarketListing getCheapestListing(Material material) {
        return getListings().stream()
                .filter(l -> l.getMaterial() == material)
                .min(Comparator.comparingDouble(MarketListing::getPrice))
                .orElse(null);
    }
}
