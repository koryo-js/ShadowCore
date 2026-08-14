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

public class RickPrimeBombPlayerGUI implements Listener {

    private static final String TITLE =
            ChatColor.DARK_RED + "Bomb - Select Player";

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
                                        + "Click để ném bom"
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
                Bukkit.getPlayerExact(targetName);

        if (target == null) {

            player.sendMessage(
                    ChatColor.RED
                            + "Player không còn online."
            );

            return;
        }

        player.closeInventory();

        RickPrimeBomb.setPlayerTarget(
                player,
                target
        );
    }
}