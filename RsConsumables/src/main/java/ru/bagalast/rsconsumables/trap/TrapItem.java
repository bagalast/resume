package ru.bagalast.rsconsumables.trap;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.bagalast.rsconsumables.RsConsumables;
import ru.bagalast.rsconsumables.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class TrapItem {
    public static ItemStack Trap;

    public static void createTrap(RsConsumables plugin) { // Receive plugin instance
        FileConfiguration config = plugin.getConfig();

        Material material = Material.getMaterial(config.getString("item.material", "STONE").toUpperCase());
        if (material == null) {
            plugin.getLogger().warning("Invalid material specified in config. Using STONE as default.");
            material = Material.STONE;
        }
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        String name = Utils.color(config.getString("item.name", "&cTrap Item"));

        List<String> lore = new ArrayList<>();
        List<String> configLore = config.getStringList("item.lore");
        for (String loreLine : configLore) {
            lore.add(Utils.color(loreLine));
        }

        if (config.getBoolean("item.glow", false)) {
            meta.addEnchant(Enchantment.DAMAGE_ALL, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);

        Trap = item;
    }
}
