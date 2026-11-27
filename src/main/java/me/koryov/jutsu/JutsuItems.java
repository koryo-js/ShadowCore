package me.koryov.jutsu;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public final class JutsuItems {
    public static final String CHEST_NAME = "§9Shinobi Chestplate";
    public static final String PATTERN_NAME = "§5Jikūkan Pattern";

    private JutsuItems() {}

    public static ItemStack createChestplate(JavaPlugin plugin) {
        ItemStack item = new ItemStack(Material.LEATHER_CHESTPLATE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(CHEST_NAME);
        meta.setUnbreakable(true);
        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "shinobi_chestplate"),
                PersistentDataType.BYTE, (byte) 1
        );
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createPattern(JavaPlugin plugin) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(PATTERN_NAME);
        meta.setCustomModelData(21001);
        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "jikukan_pattern"),
                PersistentDataType.BYTE, (byte) 1
        );
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isChestplate(JavaPlugin plugin, ItemStack item) {
        if (item == null || item.getType() != Material.LEATHER_CHESTPLATE || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(
                new NamespacedKey(plugin, "shinobi_chestplate"), PersistentDataType.BYTE
        );
    }

    public static boolean isPattern(JavaPlugin plugin, ItemStack item) {
        if (item == null || item.getType() != Material.PAPER || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(
                new NamespacedKey(plugin, "jikukan_pattern"), PersistentDataType.BYTE
        );
    }
}
