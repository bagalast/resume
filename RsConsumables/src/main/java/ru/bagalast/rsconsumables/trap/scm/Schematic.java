package ru.bagalast.rsconsumables.trap.scm;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import ru.bagalast.rsconsumables.RsConsumables;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;

public class Schematic {

    public final int timeUndo = RsConsumables.instance.getConfig().getInt("trap.active-time") * 20;

    public void loadSchematic(Location location, String fileName) {
        File file = new File(RsConsumables.getInstance().getDataFolder() + "/schematics/" + fileName + ".schem");
        ClipboardFormat format = ClipboardFormats.findByFile(file);
        try {
            ClipboardReader reader = format.getReader(Files.newInputStream(file.toPath(), new java.nio.file.OpenOption[0]));
            try {
                Clipboard clipboard = reader.read();
                BlockVector3 cord = BlockVector3.at(location.getX(), location.getY(), location.getZ());
                final EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(location.getWorld()));
                Operation operation = (new ClipboardHolder(clipboard)).createPaste((Extent)editSession).to(cord).ignoreAirBlocks(false).build();
                Operations.complete(operation);
                editSession.close();
                BukkitRunnable timer = new BukkitRunnable() {
                    public void run() {
                        editSession.undo(editSession);
                    }
                };
                timer.runTaskLater((Plugin)RsConsumables.getInstance(), this.timeUndo);
                if (reader != null)
                    reader.close();
            } catch (Throwable throwable) {
                if (reader != null)
                    try {
                        reader.close();
                    } catch (Throwable throwable1) {
                        throwable.addSuppressed(throwable1);
                    }
                throw throwable;
            }
        } catch (IOException|com.sk89q.worldedit.WorldEditException e) {
            e.printStackTrace();
        }
    }
    public static RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();

    public static ProtectedCuboidRegion trapRegion;

    public static void createRegion(int x, int y, int z, final World worldBukkit, Player player) {
        int radius = 6;
        if (container != null) {
            RegionManager regionManager = container.get(BukkitAdapter.adapt(worldBukkit));
            BlockVector3 minPoint = BlockVector3.at(x - radius, y - radius, z - radius);
            BlockVector3 maxPoint = BlockVector3.at(x + radius, y + radius, z + radius);
            String nameRegion = String.valueOf(UUID.randomUUID());
            trapRegion = new ProtectedCuboidRegion(nameRegion, minPoint, maxPoint);
            trapRegion.setFlag((Flag) Flags.USE, StateFlag.State.ALLOW);
            trapRegion.setFlag((Flag)Flags.BLOCK_BREAK, StateFlag.State.DENY);
            trapRegion.setFlag((Flag)Flags.USE, StateFlag.State.ALLOW);
            trapRegion.setFlag((Flag)Flags.BUILD, StateFlag.State.DENY);
            trapRegion.setFlag((Flag)Flags.MOB_SPAWNING, StateFlag.State.DENY);
            trapRegion.setFlag((Flag)Flags.PLACE_VEHICLE, StateFlag.State.DENY);
            trapRegion.setFlag((Flag)Flags.PVP, StateFlag.State.ALLOW);
            trapRegion.setFlag((Flag)Flags.CREEPER_EXPLOSION, StateFlag.State.DENY);
            trapRegion.setFlag((Flag)Flags.OTHER_EXPLOSION, StateFlag.State.DENY);
            trapRegion.setFlag((Flag)Flags.TNT, StateFlag.State.DENY);
            trapRegion.setFlag((Flag)Flags.PISTONS, StateFlag.State.DENY);
            regionManager.addRegion((ProtectedRegion)trapRegion);
            BukkitRunnable timer = new BukkitRunnable() {
                public void run() {
                    List<Player> trappedPlayers = getPlayersInRegion(worldBukkit, x, y, z, radius);
                    Schematic.deleteRegion(worldBukkit);
                    for (Player trappedPlayer : trappedPlayers) {
                        ejectPlayer(trappedPlayer);
                    }
                    player.playSound(player.getLocation(),Sound.valueOf(RsConsumables.instance.getConfig().getString("trap.sound-disabled")),1.0F,1.0F);;
                }
            };
            timer.runTaskLater((Plugin) RsConsumables.getInstance(), (RsConsumables.getInstance().getConfig().getInt("trap.active-time") * 20L));
        }
    }
    public static void deleteRegion(World worldBukkit) {
        if (container != null) {
            RegionManager regionManager = container.get(BukkitAdapter.adapt(worldBukkit));
            regionManager.removeRegion(trapRegion.getId());
        }
    }



    private static List<Player> getPlayersInRegion(World world, int x, int y, int z, int radius) {
        List<Player> playersInRegion = new ArrayList<>();
        for (Player player : world.getPlayers()) {
            Location playerLocation = player.getLocation();
            if (playerLocation.getX() >= x - radius && playerLocation.getX() <= x + radius &&
                    playerLocation.getY() >= y - radius && playerLocation.getY() <= y + radius &&
                    playerLocation.getZ() >= z - radius && playerLocation.getZ() <= z + radius) {
                playersInRegion.add(player);
            }
        }
        return playersInRegion;
    }

    private static void ejectPlayer(Player player) {
        Location playerLocation = player.getLocation();
        World world = player.getWorld();

        // 1. Проверка по вертикали вверх (как и раньше)

        int horizontalRadius = 4;
        // 2. Генерация координат для телепортации
        List<Location> possibleLocations = new ArrayList<>();
        for (int x = -horizontalRadius; x <= horizontalRadius; x++) {
            for (int y = -1; y <= 1; y++) { // Проверяем немного выше и ниже текущей позиции
                for (int z = -horizontalRadius; z <= horizontalRadius; z++) {
                    if (x == 0 && y == 0 && z == 0) continue; // Пропускаем текущую позицию

                    Location checkLocation = playerLocation.clone().add(x, y, z);
                    if (checkLocation.getBlock().getType() == Material.AIR && checkLocation.clone().add(0,1,0).getBlock().getType() == Material.AIR) {
                        possibleLocations.add(checkLocation); // Если воздух - добавляем
                    }
                }
            }
        }


        // 3. Поиск ближайшей координаты
        Location closestLocation = null;
        double closestDistance = Double.MAX_VALUE;
        for (Location location : possibleLocations) {
            double distance = playerLocation.distanceSquared(location);
            if (distance < closestDistance) {
                closestLocation = location;
                closestDistance = distance;
            }
        }

        // 4. Телепортация (если нашли координату)
        if (closestLocation != null) {
            player.teleport(closestLocation);
            return;
        }

        for (int i = 1; i <= 5; i++) { // Проверяем до 5 блоков вверх
            Location checkLocation = playerLocation.clone().add(0, i, 0);
            if (checkLocation.getBlock().getType() == Material.AIR) {
                player.teleport(checkLocation);
                return; // Нашли свободное место, телепортируем и выходим
            }
        }


        // 5. Если ничего не помогло, применяем импульс (как и раньше)
        Random random = new Random();
        double xImpulse = random.nextDouble() * 0.2 - 0.1; // Случайный импульс по X
        double zImpulse = random.nextDouble() * 0.2 - 0.1; // Случайный импульс по Z
        player.setVelocity(new Vector(xImpulse, 0.2, zImpulse)); // Небольшой импульс вверх и в стороны
    }
}
