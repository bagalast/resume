package ru.bagalast.rsconsumables.trap.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ru.bagalast.rsconsumables.RsConsumables;
import ru.bagalast.rsconsumables.trap.TrapItem;
import ru.bagalast.rsconsumables.utils.Utils;

import java.util.Objects;

public class Give implements CommandExecutor {

    private final RsConsumables plugin;
    private final FileConfiguration config;

    public Give(RsConsumables plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("rs.give")) {
            sender.sendMessage(Objects.requireNonNull(Utils.color(config.getString("no-perm-give"))));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(Objects.requireNonNull(Utils.color(config.getString("help"))));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give":
                if (args.length < 2) {
                    sender.sendMessage(Objects.requireNonNull(Utils.color(config.getString("help"))));
                    return true;
                }

                if (!args[1].equals("trap")) {
                    sender.sendMessage(Objects.requireNonNull(Utils.color(config.getString("no-item"))));
                    return true;
                }

                if (args.length == 2) {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage("Эта команда может быть выполнена только игроком");
                        return true;
                    }
                    Player player = (Player) sender;
                    player.getInventory().addItem(TrapItem.Trap.clone());
                    return true;
                }

                if (args.length >= 3) {
                    Player target = Bukkit.getPlayer(args[2]);
                    if (target == null) {
                        sender.sendMessage(Objects.requireNonNull(Utils.color(config.getString("no-player"))));
                        return true;
                    }

                    int amount = 1;
                    if (args.length == 4) {
                        try {
                            amount = Integer.parseInt(args[3]);
                            if (amount <= 0) {
                                sender.sendMessage(Objects.requireNonNull(Utils.color(config.getString("amount-error"))));
                                return true;
                            }
                        } catch (NumberFormatException e) {
                            sender.sendMessage(Objects.requireNonNull(Utils.color(config.getString("amount-error"))));
                            return true;
                        }
                    }

                    ItemStack item = TrapItem.Trap.clone();
                    item.setAmount(amount);
                    target.getInventory().addItem(item);

                    return true;
                }
                break;
            default:
                sender.sendMessage(Objects.requireNonNull(Utils.color(config.getString("help"))));
                return true;
        }
        // Reaching here means the command wasn't handled
        return false;
    }
}
