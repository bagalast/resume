package ru.bagalast.enderchestoff;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class EnderChestOff extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
    String message = getConfig().getString("message");

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK
                && event.getPlayer().getGameMode() == GameMode.CREATIVE
                && event.getClickedBlock().getType() == Material.ENDER_CHEST) {
            event.setCancelled(true);

            event.getPlayer().sendMessage(Utils.color(message));
        }
    }
    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage().toLowerCase();

        if (player.getGameMode() == GameMode.CREATIVE
                && (command.equals("/ec") || command.startsWith("/ec ") || command.equals("/essentials:ec") || command.startsWith("/essentials:ec"))) {
            event.setCancelled(true);
            player.sendMessage(Utils.color(message));
        }
    }
}
