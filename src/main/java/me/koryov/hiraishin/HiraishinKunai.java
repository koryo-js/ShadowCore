package me.koryov.hiraishin;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class HiraishinKunai {

    private HiraishinKunai() {
    }

    private static NamespacedKey key(JavaPlugin plugin) {
        return new NamespacedKey(plugin, "hiraishin_kunai");
    }

    public static ItemStack create(JavaPlugin plugin) {
        ItemStack item = new ItemStack(Material.TRIDENT);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        meta.setDisplayName(ChatColor.YELLOW + "Phi Lôi Thần Kunai");
        meta.setLore(List.of(
                ChatColor.GRAY + "Một kunai được đánh dấu bằng Phi Lôi Thần Thuật.",
                ChatColor.YELLOW + "Ném để tạo dấu ấn.",
                ChatColor.GRAY + "Ngồi + vung tay để dịch chuyển đến kunai."
        ));
        meta.getPersistentDataContainer().set(
                key(plugin),
                PersistentDataType.BYTE,
                (byte) 1
        );

        item.setItemMeta(meta);
        return item;
    }

    public static boolean isKunai(JavaPlugin plugin, ItemStack item) {
        if (item == null || item.getType() != Material.TRIDENT || !item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.getPersistentDataContainer().has(
                key(plugin),
                PersistentDataType.BYTE
        );
    }
}
