package me.koryov.omega;

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

import java.util.List;

public class OmegaDevicePlayerGUI implements Listener {

    private static final String TITLE =
            ChatColor.DARK_PURPLE + "Omega - Select Player";

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
                        ChatColor.LIGHT_PURPLE
                                + target.getName()
                );

                meta.setLore(
                        List.of(
                                ChatColor.GRAY
                                        + "Select this target"
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
    public void onClick(
            InventoryClickEvent event
    ) {

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

        ItemMeta meta =
                item.getItemMeta();

        if (meta == null
                || !meta.hasDisplayName()) {

            return;
        }

        String targetName =
                ChatColor.stripColor(
                        meta.getDisplayName()
                );

        Player target =
                Bukkit.getPlayerExact(
                        targetName
                );

        if (target == null) {

            player.sendMessage(
                    ChatColor.RED
                            + "Player không còn online."
            );

            return;
        }

        player.closeInventory();

        /*
         * Chỉ lưu target.
         * CHƯA kick.
         * CHƯA activate.
         */

        OmegaDevice.setTarget(
                player,
                target
        );

        player.sendMessage(
                ChatColor.LIGHT_PURPLE
                        + "Target set: "
                        + ChatColor.WHITE
                        + target.getName()
        );

        /*
         * Mở lại Omega GUI để có thể
         * bấm Activate Omega.
         */

        Bukkit.getScheduler().runTaskLater(
                Bukkit.getPluginManager()
                        .getPlugin("ShadowCore"),

                () -> {

                    if (player.isOnline()) {

                        OmegaDeviceGUI.open(
                                player
                        );
                    }
                },

                2L
        );
    }
}