package me.koryov.portal;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class PortalPlayerGUI implements Listener {

    private static final String TITLE =
            ChatColor.DARK_GREEN + "Select Player";

    public static void open(Player player) {

        Inventory gui =
                Bukkit.createInventory(
                        null,
                        54,
                        TITLE
                );

        int slot = 0;

        for (Player target : Bukkit.getOnlinePlayers()) {

            if (target.equals(player)) {
                continue;
            }

            if (slot >= 54) {
                break;
            }

            ItemStack head =
                    new ItemStack(Material.PLAYER_HEAD);

            ItemMeta meta =
                    head.getItemMeta();

            if (meta != null) {

                meta.setDisplayName(
                        ChatColor.YELLOW
                                + target.getName()
                );

                meta.setLore(
                        java.util.List.of(
                                ChatColor.GRAY
                                        + "Click để chọn player"
                        )
                );

                head.setItemMeta(meta);
            }

            gui.setItem(slot, head);

            slot++;
        }

        player.openInventory(gui);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {

        if (!event.getView()
                .getTitle()
                .equals(TITLE)) {

            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked()
                instanceof Player player)) {

            return;
        }

        ItemStack item =
                event.getCurrentItem();

        if (item == null
                || item.getType()
                != Material.PLAYER_HEAD) {

            return;
        }

        if (!item.hasItemMeta()) {
            return;
        }

        ItemMeta meta =
                item.getItemMeta();

        if (meta == null
                || !meta.hasDisplayName()) {

            return;
        }

        String name =
                ChatColor.stripColor(
                        meta.getDisplayName()
                );

        Player target =
                Bukkit.getPlayerExact(name);

        Location targetLocation =
                target.getLocation().clone();

        Vector direction =
                target.getLocation()
                        .getDirection()
                        .normalize();

        Location exitLocation =
                targetLocation
                        .subtract(direction.multiply(6))
                        .add(0, 3, 0);

        if (target == null) {

            player.sendMessage(
                    ChatColor.RED
                            + "Player không còn online."
            );

            return;
        }

        PortalGunGUI.setTarget(
                player,
                target
        );

        player.closeInventory();

        player.sendMessage(
                ChatColor.GREEN
                        + "Đã chọn: "
                        + target.getName()
        );

        Bukkit.getScheduler().runTaskLater(
                Bukkit.getPluginManager()
                        .getPlugin("ShadowCore"),
                () -> {

                    if (player.isOnline()) {
                        PortalGunGUI.open(player);
                    }

                },
                2L
        );
    }
}