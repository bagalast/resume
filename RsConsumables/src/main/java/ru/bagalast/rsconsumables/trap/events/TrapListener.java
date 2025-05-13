package ru.bagalast.rsconsumables.trap.events;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import ru.bagalast.rsconsumables.RsConsumables;
import ru.bagalast.rsconsumables.trap.TrapItem;
import ru.bagalast.rsconsumables.trap.scm.Schematic;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

public class TrapListener implements Listener {

    private final Map<UUID, Long> cooldowns = new HashMap<>(); // Track cooldowns per player
    private final ItemStack trapItem = TrapItem.Trap.clone();
    private final int cooldownTime; // in ticks

    public TrapListener() {
        this.cooldownTime = RsConsumables.instance.getConfig().getInt("trap.cooldown") * 20; // Cooldown time in ticks
    }

    @EventHandler
    private void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) && player.getInventory().getItemInMainHand().getItemMeta() != null) {
            ItemStack currentItem = player.getInventory().getItemInMainHand();

            if (currentItem.isSimilar(trapItem)) {
                UUID playerUUID = player.getUniqueId();
                long currentTime = System.currentTimeMillis();

                // Check cooldown
                if (cooldowns.containsKey(playerUUID)) {
                    long lastUseTime = cooldowns.get(playerUUID);
                    if ((currentTime - lastUseTime) < cooldownTime * 50L) { // Convert ticks to milliseconds
                        event.setCancelled(true);
                        return;
                    }
                }

                event.setCancelled(true);
                RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                RegionManager regions = container.get(BukkitAdapter.adapt(player.getWorld()));

                // Update cooldown

                ApplicableRegionSet set = regions.getApplicableRegions(BukkitAdapter.asBlockVector(player.getLocation()));
                for (ProtectedRegion region : set) {
                    if (region.getId().equalsIgnoreCase((RsConsumables.instance.getConfig().getString("disabled-regions")))) {

                        event.setCancelled(true);
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);

                        return;

                    }
                    else {
                        break;
                    }

                }

                cooldowns.put(playerUUID, currentTime);
                player.setCooldown(currentItem.getType(), cooldownTime);
                (new Schematic()).loadSchematic(player.getLocation(),RsConsumables.instance.getConfig().getString("trap.file-schematic"));
                Schematic.createRegion(event.getPlayer().getLocation().getBlockX(),event.getPlayer().getLocation().getBlockY(), event.getPlayer().getLocation().getBlockZ(), event.getPlayer().getWorld(),player);
                player.playSound(player.getLocation(),Sound.valueOf(RsConsumables.instance.getConfig().getString("trap.sound")),1.0F,1.0F);
                int amount = player.getInventory().getItemInMainHand().getAmount();
                player.getInventory().getItemInMainHand().setAmount(amount - 1);

            }
        }
    }
}
