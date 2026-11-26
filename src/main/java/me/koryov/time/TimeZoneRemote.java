package me.koryov.time;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public final class TimeZoneRemote {

    public static final String ITEM_NAME = "Time Zone Remote";

    private TimeZoneRemote() {
    }

    public static ItemStack createItem() {
        ItemStack item = new ItemStack(Material.CLOCK);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + ITEM_NAME);
            meta.setLore(List.of(
                    ChatColor.GRAY + "Right-click to toggle the time field.",
                    ChatColor.GRAY + "Freezes entities within 10 blocks."
            ));
            item.setItemMeta(meta);
        }

        return item;
    }

    public static boolean isRemote(ItemStack item) {
        if (item == null || item.getType() != Material.CLOCK || !item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasDisplayName()
                && ITEM_NAME.equals(ChatColor.stripColor(meta.getDisplayName()));
    }
}
