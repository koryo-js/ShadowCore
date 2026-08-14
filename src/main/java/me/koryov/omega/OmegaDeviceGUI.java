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

public class OmegaDeviceGUI implements Listener {

    private static final String TITLE =
            ChatColor.DARK_PURPLE + "Omega Device";

    public static void open(Player player) {

        Inventory gui =
                Bukkit.createInventory(
                        null,
                        27,
                        TITLE
                );

        /*
         * PLAYER
         */

        ItemStack playerItem =
                new ItemStack(Material.PLAYER_HEAD);

        ItemMeta playerMeta =
                playerItem.getItemMeta();

        if (playerMeta != null) {

            playerMeta.setDisplayName(
                    ChatColor.LIGHT_PURPLE
                            + "Select Player"
            );

            playerMeta.setLore(
                    List.of(
                            ChatColor.GRAY
                                    + "Select a player to erase"
                    )
            );

            playerItem.setItemMeta(
                    playerMeta
            );
        }

        /*
         * ACTIVATE
         */

        ItemStack activate =
                new ItemStack(Material.NETHER_STAR);

        ItemMeta activateMeta =
                activate.getItemMeta();

        if (activateMeta != null) {

            activateMeta.setDisplayName(
                    ChatColor.DARK_PURPLE
                            + "Activate Omega"
            );

            activateMeta.setLore(
                    List.of(
                            ChatColor.GRAY
                                    + "Erase the selected target"
                    )
            );

            activate.setItemMeta(
                    activateMeta
            );
        }

        gui.setItem(
                13,
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
         * SELECT PLAYER
         */

        if (event.getRawSlot() == 13
                && item.getType()
                == Material.PLAYER_HEAD) {

            player.closeInventory();

            OmegaDevicePlayerGUI.open(
                    player
            );

            return;
        }

        /*
         * ACTIVATE OMEGA
         */

        if (event.getRawSlot() == 22
                && item.getType()
                == Material.NETHER_STAR) {

            player.closeInventory();

            OmegaDevice.activate(
                    player
            );
        }
    }
}