package me.koryov.jutsu;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public final class JutsuItems {
    public static final String CHEST_NAME = "§9Shinobi Chestplate";
    public static final String PATTERN_NAME = "§5Jikūkan Pattern";
    public static final String HOKAGE_HAT_NAME = "§6Hokage Hat";

    private JutsuItems() {}

    public static ItemStack createChestplate(JavaPlugin plugin) {
        ItemStack item = new ItemStack(Material.LEATHER_CHESTPLATE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(CHEST_NAME);
        meta.setUnbreakable(true);
        meta.setLore(java.util.List.of(
                "§7Trang bị áo này để kích hoạt KCM khi đội Hokage Hat.",
                "§bRasengan: Ngồi + chuột phải để phóng."
        ));
        if (meta instanceof LeatherArmorMeta leatherMeta) {
            leatherMeta.setColor(Color.fromRGB(255, 176, 32));
        }
        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "shinobi_chestplate"),
                PersistentDataType.BYTE, (byte) 1
        );
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createHokageHat(JavaPlugin plugin) {
        ItemStack item = new ItemStack(Material.LEATHER_HELMET);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(HOKAGE_HAT_NAME);
        meta.setUnbreakable(true);
        meta.setLore(java.util.List.of(
                "§7Mũ Hokage — mở khóa KCM khi mặc cùng Shinobi Chestplate."
        ));
        if (meta instanceof LeatherArmorMeta leatherMeta) {
            leatherMeta.setColor(Color.fromRGB(190, 35, 35));
        }
        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "hokage_hat"),
                PersistentDataType.BYTE, (byte) 1
        );
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isHokageHat(JavaPlugin plugin, ItemStack item) {
        if (item == null || item.getType() != Material.LEATHER_HELMET || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(
                new NamespacedKey(plugin, "hokage_hat"), PersistentDataType.BYTE
        );
    }

    public static ItemStack createPattern(JavaPlugin plugin) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(PATTERN_NAME);
        meta.setCustomModelData(21001);
        meta.setLore(java.util.List.of(
                "§7Shift + chuột phải: đặt kết ấn tại vị trí hiện tại.",
                "§7Chuột phải: bật hoặc tắt cổng không-thời gian.",
                "§7Projectile xuyên qua cổng sẽ xuất hiện tại Hiraishin Kunai."
        ));
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
