package ru.bagalast.bestdevrwmarket.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.bagalast.bestdevrwmarket.BestDevRWMarket;
import ru.bagalast.bestdevrwmarket.MarketManager;
import ru.bagalast.bestdevrwmarket.Utils;

public class MarketCommand implements CommandExecutor {
    private final MarketManager manager;

    public MarketCommand(MarketManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Команда доступна игроку");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            manager.openMarketMenu(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "sell":
                if (args.length < 2) {
                    String message = BestDevRWMarket.getInstance().getConfig().getString("messages.help-sell");
                    if (message == null) message = "&cЭтот предмет нельзя выставить на продажу!";
                    player.sendMessage(Utils.color(message));
                    return true;
                }
                try {
                    double price = Double.parseDouble(args[1]);
                    ItemStack item = player.getInventory().getItemInMainHand();
                    if (item == null || item.getType() == Material.AIR) {
                        String message = BestDevRWMarket.getInstance().getConfig().getString("messages.item-error");
                        if (message == null) message = "&cВозьмите предмет в руку";
                        player.sendMessage(Utils.color(message));
                        return true;
                    }


                    if (!manager.getAllowedItems().contains(item.getType())) {
                        String message = BestDevRWMarket.getInstance().getConfig().getString("messages.item-not-allowed");
                        if (message == null) message = "&cЭтот предмет нельзя выставить на продажу";
                        player.sendMessage(Utils.color(message));
                        return true;
                    }


                    int amount = item.getAmount();
                    player.getInventory().setItemInMainHand(null);
                    ItemStack toSell = new ItemStack(item.getType(), amount);
                    toSell.setItemMeta(item.getItemMeta());
                    manager.addListing(player, toSell, price);
                    String message = BestDevRWMarket.getInstance().getConfig().getString("messages.market-sell");
                    player.sendMessage(Utils.color(message.replace("{price}", String.format("%.2f",price)).replace(
                            "{amount}", String.valueOf(amount)).replace("{item}", item.getType().toString().toLowerCase()
                    )));
                } catch (NumberFormatException e) {
                    String message = BestDevRWMarket.getInstance().getConfig().getString("messages.price-error");
                    if (message == null) message = "&cНеверная цена";
                    player.sendMessage(Utils.color(message));
                }
                break;

            case "my":
                manager.openMarketMenu(player);
                break;

            default:
                String message = BestDevRWMarket.getInstance().getConfig().getString("messages.error");
                if (message == null) message = "&cНеизвестная команда";
                player.sendMessage(Utils.color(message));
        }

        return true;
    }
}
