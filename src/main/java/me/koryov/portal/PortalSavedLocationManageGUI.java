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
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PortalSavedLocationManageGUI
        implements Listener {

    private static final String TITLE =
            ChatColor.DARK_GREEN
                    + "Manage Location";

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
         * RENAME
         */

        ItemStack rename =
                new ItemStack(
                        Material.NAME_TAG
                );

        ItemMeta renameMeta =
                rename.getItemMeta();

        if (renameMeta != null) {

            renameMeta.setDisplayName(
                    ChatColor.YELLOW
                            + "Rename"
            );

            renameMeta.setLore(
                    List.of(
                            ChatColor.GRAY
                                    + "Đổi tên location"
                    )
            );

            rename.setItemMeta(
                    renameMeta
            );
        }


        /*
         * DELETE
         */

        ItemStack delete =
                new ItemStack(
                        Material.BARRIER
                );

        ItemMeta deleteMeta =
                delete.getItemMeta();

        if (deleteMeta != null) {

            deleteMeta.setDisplayName(
                    ChatColor.RED
                            + "Delete"
            );

            deleteMeta.setLore(
                    List.of(
                            ChatColor.GRAY
                                    + "Xóa location này"
                    )
            );

            delete.setItemMeta(
                    deleteMeta
            );
        }


        /*
         * BACK
         */

        ItemStack back =
                new ItemStack(
                        Material.ARROW
                );

        ItemMeta backMeta =
                back.getItemMeta();

        if (backMeta != null) {

            backMeta.setDisplayName(
                    ChatColor.GRAY
                            + "Back"
            );

            back.setItemMeta(
                    backMeta
            );
        }


        gui.setItem(
                11,
                rename
        );

        gui.setItem(
                15,
                delete
        );

        gui.setItem(
                22,
                back
        );

        player.openInventory(gui);
    }


    public static String getSelected(
            Player player
    ) {

        return SELECTED.get(
                player.getUniqueId()
        );
    }


    public static void clearSelected(
            Player player
    ) {

        SELECTED.remove(
                player.getUniqueId()
        );
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


        /*
         * RENAME
         */

        if (event.getRawSlot() == 11) {

            String name =
                    getSelected(player);

            if (name == null) {
                return;
            }

            player.closeInventory();

            PortalSavedLocationChat.startRename(
                    player,
                    name
            );

            return;
        }


        /*
         * DELETE
         */

        if (event.getRawSlot() == 15) {

            String name =
                    getSelected(player);

            if (name == null) {
                return;
            }

            PortalSavedLocationDeleteGUI.open(
                    player,
                    name
            );

            return;
        }


        /*
         * BACK
         */

        if (event.getRawSlot() == 22) {

            clearSelected(player);

            PortalSavedLocationsGUI.open(
                    player
            );
        }
    }
}