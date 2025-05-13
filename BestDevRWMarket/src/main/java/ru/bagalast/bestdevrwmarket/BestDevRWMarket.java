package ru.bagalast.bestdevrwmarket;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import ru.bagalast.bestdevrwmarket.commands.MarketCommand;

import java.io.File;
import java.io.IOException;

public final class BestDevRWMarket extends JavaPlugin {
    private static BestDevRWMarket instance;
    private Economy economy;
    private MarketManager manager;
    private File marketDataFile;
    private FileConfiguration marketData;
    private File allowedItemsFile;
    private FileConfiguration allowedItemsData;

    @Override
    public void onEnable() {
        instance = this;
        createFiles();
        saveDefaultConfig();

        if (!setupEconomy()) {
            getLogger().severe("Vault не найден! Плагин отключен.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.manager = new MarketManager(this);
        getCommand("market").setExecutor(new MarketCommand(manager));

        getServer().getPluginManager().registerEvents(new MenuListener(), this);
    }

    private void createFiles() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        marketDataFile = new File(getDataFolder(), "data.yml");
        if (!marketDataFile.exists()) {
            saveResource("data.yml", false);
        }
        marketData = YamlConfiguration.loadConfiguration(marketDataFile);

        allowedItemsFile = new File(getDataFolder(), "allowed_items.yml");
        if (!allowedItemsFile.exists()) {
            saveResource("allowed_items.yml", false);
        }
        allowedItemsData = YamlConfiguration.loadConfiguration(allowedItemsFile);
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return true;
    }

    public static BestDevRWMarket getInstance() {
        return instance;
    }

    public Economy getEconomy() {
        return economy;
    }

    public FileConfiguration getData() {
        return marketData;
    }

    public FileConfiguration getAllowedData() {
        return allowedItemsData;
    }

    public void saveData() {
        try {
            marketData.save(marketDataFile);
        } catch (IOException e) {
            getLogger().severe("Ошибка сохранения data.yml: " + e.getMessage());
        }
    }
}