package ru.bagalast.bestdevrwmarket;

public class MarketStats {
    private final int count;
    private final double minPrice;
    private final double avgPrice;

    public MarketStats(int count, double minPrice, double avgPrice) {
        this.count = count;
        this.minPrice = minPrice;
        this.avgPrice = avgPrice;
    }
    public int getCount(){
        return count;
    }
    public double getMinPrice(){
        return minPrice;
    }
    public double getAvgPrice(){
        return avgPrice;
    }
}
