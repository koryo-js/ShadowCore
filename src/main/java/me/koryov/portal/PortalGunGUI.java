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
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PortalGunGUI implements Listener {

    private static PortalSavedLocations savedLocations;

    private static final java.util.Map<
            java.util.UUID,
            java.util.UUID
            > SELECTED_PLAYERS =
            new java.util.HashMap<>();

    private static final String TITLE =
            ChatColor.DARK_GREEN + "Portal Gun";

    private static final Map<
            UUID,
            org.bukkit.Location
            > SELECTED_LOCATIONS =
            new HashMap<>();

    public static void open(Player player) {

        Inventory gui =
                Bukkit.createInventory(
                        null,
                        18,
                        TITLE
                );

        gui.setItem(
                1,
                createItem(
                        Material.PAPER,
                        ChatColor.GREEN + "Coordinates",
                        ChatColor.GRAY + "Nhập tọa độ X Y Z"
                )
        );

        gui.setItem(
                3,
                createItem(
                        Material.PLAYER_HEAD,
                        ChatColor.YELLOW + "Player",
                        ChatColor.GRAY + "Chọn một người chơi"
                )
        );

        gui.setItem(
                5,
                createItem(
                        Material.LIME_DYE,
                        ChatColor.GREEN + "Fire Portal",
                        ChatColor.GRAY + "Bắn Portal tới mục tiêu"
                )
        );

        gui.setItem(
                7,
                createItem(
                        Material.ORANGE_DYE,
                        ChatColor.GOLD + "Portal B",
                        ChatColor.GRAY + "Đặt Portal B"
                )
        );
        gui.setItem(
                8,
                createItem(
                        Material.CHEST,
                        ChatColor.AQUA + "Saved Locations",
                        ChatColor.GRAY + "Quản lý các vị trí đã lưu"
                )
        );

        player.openInventory(gui);
    }

    private static ItemStack createItem(
            Material material,
            String name,
            String lore
    ) {

        ItemStack item =
                new ItemStack(material);

        ItemMeta meta =
                item.getItemMeta();

        meta.setDisplayName(name);

        meta.setLore(
                List.of(lore)
        );

        item.setItemMeta(meta);

        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {

        if (!event.getView().getTitle().equals(TITLE)) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        event.setCancelled(true);

        switch (event.getRawSlot()) {

            case 1:

                event.setCancelled(true);

                player.closeInventory();

                Bukkit.getScheduler().runTask(
                        Bukkit.getPluginManager()
                                .getPlugin("ShadowCore"),
                        () -> PortalCoordinateChat.start(player)
                );

                break;

            case 3:

                player.closeInventory();

                Bukkit.getScheduler().runTaskLater(
                        Bukkit.getPluginManager()
                                .getPlugin("ShadowCore"),
                        () -> PortalPlayerGUI.open(player),
                        1L
                );

                break;

            case 5:

                firePortalToPlayer(player);

                break;

            case 7:

                firePortalToCoordinates(player);

                break;
            case 8:

                player.closeInventory();

                PortalSavedLocationsGUI.open(
                        player
                );

                break;

            default:
                break;
        }
    }

    public static void setCoordinates(
            Player player,
            Location location
    ) {

        SELECTED_LOCATIONS.put(
                player.getUniqueId(),
                location.clone()
        );
    }

    public static org.bukkit.Location getCoordinates(
            Player player
    ) {

        return SELECTED_LOCATIONS.get(
                player.getUniqueId()
        );
    }

    private void firePortal(Player player) {

        org.bukkit.Location targetLocation =
                getCoordinates(player);

        if (targetLocation == null) {

            player.sendMessage(
                    ChatColor.RED
                            + "Chưa nhập tọa độ Portal B."
            );

            return;
        }

        if (targetLocation.getWorld() == null) {

            player.sendMessage(
                    ChatColor.RED
                            + "Không tìm thấy dimension của tọa độ."
            );

            return;
        }

        org.bukkit.Location firstLocation =
                player.getEyeLocation()
                        .clone()
                        .add(
                                player.getLocation()
                                        .getDirection()
                                        .normalize()
                                        .multiply(3)
                        );

        org.bukkit.Location secondLocation =
                targetLocation.clone();

        PortalData first =
                new PortalData(
                        firstLocation,
                        player.getLocation()
                                .getDirection()
                );

        PortalData second =
                new PortalData(
                        secondLocation,
                        player.getLocation()
                                .getDirection()
                );

        PortalManager.setPortal(
                player.getUniqueId(),
                new PortalPair(
                        first,
                        second
                )
        );

        player.getWorld().playSound(
                firstLocation,
                org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT,
                1.0f,
                1.2f
        );

        if (secondLocation.getWorld() != null) {

            secondLocation.getWorld().playSound(
                    secondLocation,
                    org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT,
                    1.0f,
                    1.2f
            );
        }

        player.closeInventory();

        new PortalTask(
                player,
                first,
                second
        ).runTaskTimer(
                Bukkit.getPluginManager()
                        .getPlugin("ShadowCore"),
                0,
                1
        );

        player.sendMessage(
                ChatColor.GREEN
                        + "Đã tạo Portal tới "
                        + secondLocation.getBlockX()
                        + " "
                        + secondLocation.getBlockY()
                        + " "
                        + secondLocation.getBlockZ()
        );
    }
    public static void setTarget(
            Player player,
            Player target
    ) {

        SELECTED_PLAYERS.put(
                player.getUniqueId(),
                target.getUniqueId()
        );
    }
    public static Player getTarget(
            Player player
    ) {

        java.util.UUID uuid =
                SELECTED_PLAYERS.get(
                        player.getUniqueId()
                );

        if (uuid == null) {
            return null;
        }

        return Bukkit.getPlayer(uuid);
    }
    private void firePortalToPlayer(Player player) {

        Player target =
                getTarget(player);

        if (target == null) {

            player.sendMessage(
                    ChatColor.RED
                            + "Chưa chọn player."
            );

            return;
        }

        Location firstLocation =
                player.getLocation()
                        .clone()
                        .add(
                                player.getLocation()
                                        .getDirection()
                                        .normalize()
                                        .multiply(3)
                        )
                        .add(0, 1, 0);

        Location targetLocation =
                target.getLocation().clone();

        Vector targetDirection =
                target.getLocation()
                        .getDirection()
                        .clone();

        targetDirection.setY(0);

        if (targetDirection.lengthSquared() > 0) {
            targetDirection.normalize();
        }

        Location secondLocation =
                targetLocation
                        .clone()
                        .add(
                                targetDirection.clone()
                                        .multiply(7)
                        )
                        .add(0, 1, 0);

        PortalData first =
                new PortalData(
                        firstLocation,
                        player.getLocation()
                                .getDirection()
                );

        PortalData second =
                new PortalData(
                        secondLocation,
                        targetDirection.clone().multiply(-1)
                );

        PortalManager.setPortal(
                player.getUniqueId(),
                new PortalPair(
                        first,
                        second
                )
        );

        player.closeInventory();

        Bukkit.getScheduler().runTask(
                Bukkit.getPluginManager()
                        .getPlugin("ShadowCore"),
                () -> {

                    new PortalTask(
                            player,
                            first,
                            second
                    ).runTaskTimer(
                            Bukkit.getPluginManager()
                                    .getPlugin("ShadowCore"),
                            0,
                            1
                    );

                }
        );

        player.sendMessage(
                ChatColor.GREEN
                        + "Đã bắn Portal tới "
                        + target.getName()
                        + "!"
        );
    }
    private void firePortalToCoordinates(
            Player player
    ) {

        Location targetLocation =
                getCoordinates(player);

        if (targetLocation == null) {

            player.sendMessage(
                    ChatColor.RED
                            + "Chưa nhập tọa độ Portal B."
            );

            return;
        }

        Location firstLocation =
                player.getEyeLocation()
                        .clone()
                        .add(
                                player.getLocation()
                                        .getDirection()
                                        .normalize()
                                        .multiply(3)
                        );

        Location secondLocation =
                targetLocation.clone();

        PortalData first =
                new PortalData(
                        firstLocation,
                        player.getLocation()
                                .getDirection()
                );

        PortalData second =
                new PortalData(
                        secondLocation,
                        player.getLocation()
                                .getDirection()
                );

        PortalManager.setPortal(
                player.getUniqueId(),
                new PortalPair(
                        first,
                        second
                )
        );

        player.closeInventory();

        new PortalTask(
                player,
                first,
                second
        ).runTaskTimer(
                Bukkit.getPluginManager()
                        .getPlugin("ShadowCore"),
                0,
                1
        );

        player.sendMessage(
                ChatColor.GREEN
                        + "Đã đặt Portal B tại "
                        + secondLocation.getBlockX()
                        + " "
                        + secondLocation.getBlockY()
                        + " "
                        + secondLocation.getBlockZ()
        );
    }

    public static void init(
            PortalSavedLocations locations
    ) {
        savedLocations = locations;
    }

    public static PortalSavedLocations getSavedLocations() {
        return savedLocations;
    }

    public static void fireSavedLocation(
            Player player
    ) {

        Location targetLocation =
                getCoordinates(player);

        if (targetLocation == null) {

            player.sendMessage(
                    ChatColor.RED
                            + "Chưa chọn Saved Location."
            );

            return;
        }

        if (targetLocation.getWorld() == null) {

            player.sendMessage(
                    ChatColor.RED
                            + "World của location không tồn tại."
            );

            return;
        }

        Location firstLocation =
                player.getEyeLocation()
                        .clone()
                        .add(
                                player.getLocation()
                                        .getDirection()
                                        .normalize()
                                        .multiply(3)
                        );

        Location secondLocation =
                targetLocation.clone();

        PortalData first =
                new PortalData(
                        firstLocation,
                        player.getLocation()
                                .getDirection()
                );

        PortalData second =
                new PortalData(
                        secondLocation,
                        player.getLocation()
                                .getDirection()
                );

        PortalManager.setPortal(
                player.getUniqueId(),
                new PortalPair(
                        first,
                        second
                )
        );

        player.getWorld().playSound(
                firstLocation,
                org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT,
                1.0f,
                1.2f
        );

        if (secondLocation.getWorld() != null) {

            secondLocation.getWorld().playSound(
                    secondLocation,
                    org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT,
                    1.0f,
                    1.2f
            );
        }

        new PortalTask(
                player,
                first,
                second
        ).runTaskTimer(
                Bukkit.getPluginManager()
                        .getPlugin("ShadowCore"),
                0L,
                1L
        );

        player.sendMessage(
                ChatColor.GREEN
                        + "Đã tạo Portal tới Saved Location."
        );
    }
}