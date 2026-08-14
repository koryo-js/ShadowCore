package me.koryov.omega;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Location;
import org.bukkit.Sound;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;

public class OmegaDevice implements Listener {

    private static final Map<UUID, UUID> TARGETS =
            new HashMap<>();

    private static final String OMEGA_NAME =
            ChatColor.DARK_PURPLE + "Omega Device";

    public static ItemStack createOmegaItem() {

        ItemStack item =
                new ItemStack(Material.RESPAWN_ANCHOR);

        ItemMeta meta =
                item.getItemMeta();

        if (meta != null) {

            meta.setDisplayName(
                    OMEGA_NAME
            );

            meta.setLore(
                    java.util.List.of(
                            ChatColor.GRAY
                                    + "Activate the Omega Device"
                    )
            );

            item.setItemMeta(meta);
        }

        return item;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent event) {

        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) {

            return;
        }

        ItemStack item =
                event.getItem();

        if (item == null
                || item.getType()
                != Material.RESPAWN_ANCHOR) {

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

        if (!name.equals("Omega Device")) {
            return;
        }

        event.setCancelled(true);

        Player player =
                event.getPlayer();

        OmegaDeviceGUI.open(player);
    }
    public static void setTarget(
            Player source,
            Player target
    ) {

        TARGETS.put(
                source.getUniqueId(),
                target.getUniqueId()
        );
    }
    public static Player getTarget(
            Player source
    ) {

        UUID targetUUID =
                TARGETS.get(
                        source.getUniqueId()
                );

        if (targetUUID == null) {
            return null;
        }

        return Bukkit.getPlayer(
                targetUUID
        );
    }
    public static void clearTarget(
            Player source
    ) {

        TARGETS.remove(
                source.getUniqueId()
        );
    }
    public static void activate(
            Player source
    ) {

        Player target =
                getTarget(source);

        if (target == null
                || !target.isOnline()) {

            source.sendMessage(
                    ChatColor.RED
                            + "Target không còn online."
            );

            clearTarget(source);

            return;
        }

        new OmegaDeviceTask(
                source,
                target
        ).runTaskTimer(
                Bukkit.getPluginManager()
                        .getPlugin("ShadowCore"),
                0L,
                1L
        );
    }
}