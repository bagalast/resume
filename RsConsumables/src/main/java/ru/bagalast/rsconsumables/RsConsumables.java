package ru.bagalast.rsconsumables;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import ru.bagalast.rsconsumables.trap.TrapItem;
import ru.bagalast.rsconsumables.trap.command.Give;
import ru.bagalast.rsconsumables.trap.events.TrapListener;
import ru.bagalast.rsconsumables.trap.scm.Schematic;
import ru.bagalast.rsconsumables.utils.Utils;

public final class RsConsumables extends JavaPlugin {

    public static RsConsumables instance;
    private Schematic schematic;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        TrapItem.createTrap(instance);
        getCommand("rs").setExecutor(new Give(this));

        // Initialize managers
        getServer().getPluginManager().registerEvents((Listener)new TrapListener(), (Plugin)this);

        Bukkit.getLogger().info(Utils.color("&7[&6ResolverConsumables&7] &aEnabled"));
    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().info(Utils.color("&7[&6ResolverConsumables&7] &cDisabled"));
    }

    public static RsConsumables getInstance() {
        return instance;
    }
}
