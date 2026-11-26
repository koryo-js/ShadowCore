package me.koryov.forcefield;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public final class ForcefieldManager {

    public static final String ITEM_NAME = "Eye Patch";

    private ForcefieldManager() {
    }

    public static ItemStack createEyePatch() {
        ItemStack item = new ItemStack(Material.LEATHER_HELMET);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();

        if (meta != null) {
            meta.setColor(Color.fromRGB(0, 0, 0));
            meta.setDisplayName(ChatColor.DARK_GRAY + ITEM_NAME);
            meta.setLore(java.util.List.of(
                    ChatColor.GRAY + "A compact defensive field."
            ));
            item.setItemMeta(meta);
        }

        return item;
    }

    public static boolean isEyePatch(ItemStack item) {
        if (item == null || item.getType() != Material.LEATHER_HELMET || !item.hasItemMeta()) {
            return false;
        }

        if (!(item.getItemMeta() instanceof LeatherArmorMeta meta) || !meta.hasDisplayName()) {
            return false;
        }

        return ITEM_NAME.equals(ChatColor.stripColor(meta.getDisplayName()))
                && Color.fromRGB(0, 0, 0).equals(meta.getColor());
    }
}
