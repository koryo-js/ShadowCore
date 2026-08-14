package me.koryov.bomb;

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

public class RickPrimeBombGUI implements Listener {

    private static final String TITLE =
            ChatColor.DARK_RED + "Rick Prime Bomb";


    public static void open(Player player) {

        Inventory gui =
                Bukkit.createInventory(
                        null,
                        27,
                        TITLE
                );


        /*
         * ==============================================
         * COORDINATES
         * ==============================================
         */

        ItemStack coordinates =
                new ItemStack(Material.COMPASS);

        ItemMeta coordinatesMeta =
                coordinates.getItemMeta();

        if (coordinatesMeta != null) {

            coordinatesMeta.setDisplayName(
                    ChatColor.GREEN
                            + "Coordinates"
            );

            coordinatesMeta.setLore(
                    List.of(
                            ChatColor.GRAY
                                    + "Set bomb coordinates"
                    )
            );

            coordinates.setItemMeta(
                    coordinatesMeta
            );
        }


        /*
         * ==============================================
         * PLAYER
         * ==============================================
         */

        ItemStack playerItem =
                new ItemStack(Material.PLAYER_HEAD);

        ItemMeta playerMeta =
                playerItem.getItemMeta();

        if (playerMeta != null) {

            playerMeta.setDisplayName(
                    ChatColor.YELLOW
                            + "Player"
            );

            playerMeta.setLore(
                    List.of(
                            ChatColor.GRAY
                                    + "Select a player"
                    )
            );

            playerItem.setItemMeta(
                    playerMeta
            );
        }


        /*
         * ==============================================
         * ACTIVATE BOMB
         * ==============================================
         */

        ItemStack activate =
                new ItemStack(Material.NETHER_STAR);

        ItemMeta activateMeta =
                activate.getItemMeta();

        if (activateMeta != null) {

            activateMeta.setDisplayName(
                    ChatColor.GREEN
                            + "Activate Bomb"
            );

            activateMeta.setLore(
                    List.of(
                            ChatColor.GRAY
                                    + "Activate the selected target"
                    )
            );

            activate.setItemMeta(
                    activateMeta
            );
        }


        /*
         * Đặt item vào GUI.
         */

        gui.setItem(
                11,
                coordinates
        );

        gui.setItem(
                15,
                playerItem
        );

        gui.setItem(
                22,
                activate
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


        ItemStack item =
                event.getCurrentItem();

        if (item == null
                || item.getType()
                == Material.AIR) {

            return;
        }


        /*
         * ==============================================
         * COORDINATES
         * ==============================================
         */

        if (event.getRawSlot() == 11
                && item.getType()
                == Material.COMPASS) {

            player.closeInventory();

            RickPrimeBomb.openCoordinates(
                    player
            );

            return;
        }


        /*
         * ==============================================
         * PLAYER
         * ==============================================
         */

        if (event.getRawSlot() == 15
                && item.getType()
                == Material.PLAYER_HEAD) {

            player.closeInventory();

            RickPrimeBombPlayerGUI.open(
                    player
            );

            return;
        }


        /*
         * ==============================================
         * ACTIVATE BOMB
         * ==============================================
         */

        if (event.getRawSlot() == 22
                && item.getType()
                == Material.NETHER_STAR) {

            player.closeInventory();

            RickPrimeBomb.activate(
                    player
            );

            return;
        }
    }
}