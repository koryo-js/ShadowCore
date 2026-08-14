package me.koryov.bomb;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class RickPrimeBombItem implements Listener {

    @EventHandler
    public void onUse(PlayerInteractEvent event) {

        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) {

            return;
        }

        Player player =
                event.getPlayer();

        ItemStack item =
                event.getItem();

        /*
         * Không cầm item gì.
         */
        if (item == null) {
            return;
        }

        /*
         * Phải là TNT.
         */
        if (item.getType() != Material.TNT) {
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
         * Không phải Neutrino Bomb.
         */
        if (!name.equals("Neutrino Bomb")) {
            return;
        }

        /*
         * Không cho đặt TNT vanilla.
         */
        event.setCancelled(true);

        /*
         * KHÔNG trừ item.
         *
         * Neutrino Bomb chỉ là
         * cái cần gạt mở GUI.
         */

        RickPrimeBombGUI.open(player);
    }
}