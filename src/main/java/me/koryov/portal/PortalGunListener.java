package me.koryov.portal;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class PortalGunListener implements Listener {

    private final List<String> allowedPlayers;

    public PortalGunListener(List<String> allowedPlayers) {
        this.allowedPlayers = allowedPlayers;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent event) {

        Action action = event.getAction();

        // Chỉ chuột phải
        if (action != Action.RIGHT_CLICK_AIR
                && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();

        // Chưa whitelist
        if (!isAllowed(player)) {
            return;
        }

        ItemStack item =
                player.getInventory().getItemInMainHand();

        // Không phải Portal Gun
        if (!isPortalGun(item)) {
            return;
        }

        event.setCancelled(true);

        PortalGunGUI.open(player);
    }

    private boolean isAllowed(Player player) {

        for (String name : allowedPlayers) {

            if (name.equalsIgnoreCase(player.getName())) {
                return true;
            }

        }

        return false;
    }

    private boolean isPortalGun(ItemStack item) {

        if (item == null
                || item.getType() == Material.AIR) {
            return false;
        }

        if (item.getType()
                != Material.GUSTER_BANNER_PATTERN) {
            return false;
        }

        if (!item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta =
                item.getItemMeta();

        if (meta == null
                || !meta.hasDisplayName()) {
            return false;
        }

        String name =
                ChatColor.stripColor(
                        meta.getDisplayName()
                );

        return name.equalsIgnoreCase(
                "Portal Gun"
        );
    }
}