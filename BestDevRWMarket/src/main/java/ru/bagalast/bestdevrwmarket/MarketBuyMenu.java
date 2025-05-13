package ru.bagalast.bestdevrwmarket;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MarketBuyMenu extends Menu {
    private final Material material;
    private int amount;
    private final MarketManager manager;
    private List<PriceLore> priceLores;
    private final FileConfiguration config = BestDevRWMarket.getInstance().getConfig();

    public MarketBuyMenu(Player player, Material material, MarketManager manager) {
        super(player, "Покупка: " + material.name(), 54);
        this.material = material;
        this.amount = 1;
        this.manager = manager;
        updatePriceLores();
    }

    @Override
    public void open() {
        initialize();
        super.open();
    }

    @Override
    public void initialize() {
        updatePriceLores();
        setupItems();
    }

    private void setupItems() {
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, null);
        }
        clickActions.clear();

        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        Economy economy = BestDevRWMarket.getInstance().getEconomy();

        List<String> lore = new ArrayList<>();
        double total = 0;

        String path = "current-items";

        if (priceLores.isEmpty()) {
            String message = config.getString("messages.only-own-lots");
            if (message == null) message = "&cЭтот предмет продаёшь только ты, купить нельзя.";
            lore.add(Utils.color(message));

            meta.setLore(lore);
            item.setItemMeta(meta);

            setItem(21, item, null);
            return;
        }

        for (PriceLore priceLore : priceLores) {
            String line = config.getString(path + ".current-line");
            if (line == null) line = "&7{current_amount} x {price}";

            line = line.replace("{current_amount}", String.valueOf(priceLore.amount))
                    .replace("{price}", String.format("%.2f", priceLore.price))
                    .replace("{sum_price}", String.format("%.2f", priceLore.total));

            lore.add(Utils.color(line));
        }

        total = priceLores.stream().mapToDouble(pl -> pl.total).sum();

        lore.add("");
        String totalLine = config.getString(path + ".total-line");
        if (totalLine == null) totalLine = "&eИтого: {total}";
        lore.add(Utils.color(totalLine.replace("{total}", String.format("%.2f", total))));

        if (economy.has(player, total)) {
            String yesBalanceLine = config.getString(path + ".yes-balance-line");
            if (yesBalanceLine == null) yesBalanceLine = "&aУ вас достаточно средств";
            lore.add(Utils.color(yesBalanceLine));
        } else {
            String noBalanceLine = config.getString(path + ".no-balance-line");
            if (noBalanceLine == null) noBalanceLine = "&cНедостаточно средств";
            lore.add(Utils.color(noBalanceLine));
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        setItem(21, item, e -> completePurchase());

        setItem(19, createLeftButton(), e -> changeAmount(-1));
        setItem(23, createRightButton(), e -> changeAmount(1));
    }

    private void completePurchase() {
        if (priceLores.isEmpty()) {
            player.sendMessage(Utils.color(config.getString("messages.only-own-lots")));
            return;
        }

        if (manager.purchaseItem(player, material, priceLores)) {
            player.sendMessage(Utils.color(config.getString("messages.sell")));
            player.closeInventory();
        }
    }

    private ItemStack createLeftButton() {
        ItemStack item = new ItemStack(Material.valueOf(config.getString("arrows.left.material")));
        ItemMeta meta = item.getItemMeta();
        String name = Utils.color(config.getString("arrows.left.name"));
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createRightButton() {
        ItemStack item = new ItemStack(Material.valueOf(config.getString("arrows.right.material")));
        ItemMeta meta = item.getItemMeta();
        String name = Utils.color(config.getString("arrows.right.name"));
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    private void changeAmount(int change) {
        amount = Math.max(1, Math.min(64, amount + change));
        updatePriceLores();
        setupItems();
    }

    private void updatePriceLores() {
        priceLores = new ArrayList<>();
        int remaining = amount;

        List<MarketListing> sortedListings = manager.getListings().stream()
                .filter(l -> l.getMaterial() == material && !l.getSeller().equalsIgnoreCase(player.getName()))
                .sorted(Comparator.comparingDouble(MarketListing::getPrice))
                .collect(Collectors.toList());

        for (MarketListing listing : sortedListings) {
            if (remaining <= 0) break;
            int take = Math.min(remaining, listing.getAmount());
            priceLores.add(new PriceLore(take, listing.getPrice(), take * listing.getPrice()));
            remaining -= take;
        }
    }
}
