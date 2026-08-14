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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PortalSavedLocationDeleteGUI
        implements Listener {

    private static final String TITLE =
            ChatColor.DARK_RED
                    + "Confirm Delete";

    private static final Map<
            UUID,
            String
            > SELECTED =
            new HashMap<>();


    public static void open(
            Player player,
            String name
    ) {

        SELECTED.put(
                player.getUniqueId(),
                name
        );

        Inventory gui =
                Bukkit.createInventory(
                        null,
                        27,
                        TITLE
                );


        /*
         * CONFIRM
         */

        ItemStack confirm =
                new ItemStack(
                        Material.RED_WOOL
                );

        ItemMeta confirmMeta =
                confirm.getItemMeta();

        if (confirmMeta != null) {

            confirmMeta.setDisplayName(
                    ChatColor.RED
                            + "Confirm Delete"
            );

            confirm.setItemMeta(
                    confirmMeta
            );
        }


        /*
         * CANCEL
         */

        ItemStack cancel =
                new ItemStack(
                        Material.LIME_WOOL
                );

        ItemMeta cancelMeta =
                cancel.getItemMeta();

        if (cancelMeta != null) {

            cancelMeta.setDisplayName(
                    ChatColor.GREEN
                            + "Cancel"
            );

            cancel.setItemMeta(
                    cancelMeta
            );
        }


        gui.setItem(
                11,
                confirm
        );

        gui.setItem(
                15,
                cancel
        );

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

        String name =
                SELECTED.get(
                        player.getUniqueId()
                );

        if (name == null) {
            return;
        }


        /*
         * CONFIRM DELETE
         */

        if (event.getRawSlot() == 11) {

            PortalGunGUI
                    .getSavedLocations()
                    .delete(name);

            SELECTED.remove(
                    player.getUniqueId()
            );

            PortalSavedLocationManageGUI
                    .clearSelected(player);

            player.closeInventory();

            player.sendMessage(
                    ChatColor.RED
                            + "Đã xóa location: "
                            + ChatColor.WHITE
                            + name
            );

            PortalSavedLocationsGUI.open(
                    player
            );

            return;
        }


        /*
         * CANCEL
         */

        if (event.getRawSlot() == 15) {

            SELECTED.remove(
                    player.getUniqueId()
            );

            PortalSavedLocationManageGUI
                    .open(
                            player,
                            name
                    );
        }
    }
}