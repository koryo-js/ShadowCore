package me.koryov.portal;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class PortalSavedLocationsGUI implements Listener {

    private static final String TITLE =
            ChatColor.DARK_GREEN + "Saved Locations";

    public static void open(Player player) {

        Inventory gui =
                Bukkit.createInventory(
                        null,
                        54,
                        TITLE
                );

        if (PortalGunGUI.getSavedLocations() == null) {

            player.openInventory(gui);

            return;
        }

        int slot = 0;

        for (String name :
                PortalGunGUI
                        .getSavedLocations()
                        .getNames()) {

            if (slot >= 45) {
                break;
            }

            PortalSavedLocation location =
                    PortalGunGUI
                            .getSavedLocations()
                            .get(name);

            if (location == null) {
                continue;
            }

            ItemStack item =
                    new ItemStack(
                            Material.ENDER_PEARL
                    );

            ItemMeta meta =
                    item.getItemMeta();

            if (meta != null) {

                meta.setDisplayName(
                        ChatColor.AQUA
                                + name
                );

                meta.setLore(
                        List.of(
                                ChatColor.GRAY
                                        + "World: "
                                        + location.getWorld(),

                                ChatColor.GRAY
                                        + "X: "
                                        + location.getX(),

                                ChatColor.GRAY
                                        + "Y: "
                                        + location.getY(),

                                ChatColor.GRAY
                                        + "Z: "
                                        + location.getZ(),

                                "",

                                ChatColor.GREEN
                                        + "Left click: Use",

                                ChatColor.YELLOW
                                        + "Right click: Manage"
                        )
                );

                item.setItemMeta(meta);
            }

            gui.setItem(
                    slot,
                    item
            );

            slot++;
        }

        /*
         * Save Current Location
         */

        ItemStack save =
                new ItemStack(
                        Material.NAME_TAG
                );

        ItemMeta saveMeta =
                save.getItemMeta();

        if (saveMeta != null) {

            saveMeta.setDisplayName(
                    ChatColor.GREEN
                            + "Save Current Location"
            );

            saveMeta.setLore(
                    List.of(
                            ChatColor.GRAY
                                    + "Lưu vị trí hiện tại"
                    )
            );

            save.setItemMeta(saveMeta);
        }

        gui.setItem(
                49,
                save
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
                || !item.hasItemMeta()) {

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


        /*
         * ==========================================
         * SAVE CURRENT LOCATION
         * ==========================================
         */

        if (event.getRawSlot() == 49) {

            player.closeInventory();

            PortalSavedLocationChat.start(
                    player
            );

            return;
        }


        /*
         * ==========================================
         * FIND SAVED LOCATION
         * ==========================================
         */

        PortalSavedLocation saved =
                PortalGunGUI
                        .getSavedLocations()
                        .get(name);

        if (saved == null) {
            return;
        }


        /*
         * ==========================================
         * RIGHT CLICK = MANAGE
         * ==========================================
         */

        if (event.getClick().isRightClick()) {

            PortalSavedLocationManageGUI.open(
                    player,
                    name
            );

            return;
        }


        /*
         * ==========================================
         * LEFT CLICK = USE LOCATION
         * ==========================================
         */

        Location location =
                saved.toLocation();

        if (location == null
                || location.getWorld() == null) {

            player.sendMessage(
                    ChatColor.RED
                            + "World của location này không tồn tại."
            );

            return;
        }

        PortalGunGUI.setCoordinates(
                player,
                location
        );

        player.closeInventory();

        player.sendMessage(
                ChatColor.GREEN
                        + "Đã chọn location: "
                        + ChatColor.WHITE
                        + name
        );

        Bukkit.getScheduler().runTask(
                Bukkit.getPluginManager()
                        .getPlugin("ShadowCore"),

                () -> PortalGunGUI.fireSavedLocation(
                        player
                )
        );
    }
}