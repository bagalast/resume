package ru.bagalast.bestdevrwmarket;

import org.bukkit.Material;

public class MarketListing {
    private final int id;
    private final String seller;
    private final Material material;
    private final int amount;
    private final double price;

    public MarketListing(int id, String seller, Material material, int amount, double price) {
        this.id = id;
        this.seller = seller;
        this.material = material;
        this.price = price;
        this.amount = amount;
    }
    public int getId(){
        return id;
    }

    public String getSeller(){
        return seller;
    }

    public Material getMaterial() {
        return material;
    }

    public int getAmount(){
        return amount;
    }

    public double getPrice(){
        return price;
    }
}

