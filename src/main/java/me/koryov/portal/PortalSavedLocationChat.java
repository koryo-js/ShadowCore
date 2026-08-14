package me.koryov.portal;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PortalSavedLocationChat
        implements Listener {

    private static final Map<
            UUID,
            Boolean
            > WAITING_SAVE =
            new HashMap<>();

    private static final Map<
            UUID,
            String
            > WAITING_RENAME =
            new HashMap<>();


    /*
     * SAVE
     */

    public static void start(
            Player player
    ) {

        WAITING_SAVE.put(
                player.getUniqueId(),
                true
        );

        player.sendMessage(
                ChatColor.YELLOW
                        + "Nhập tên location muốn lưu:"
        );

        player.sendMessage(
                ChatColor.GRAY
                        + "Ví dụ: Base, Spawn, NetherHub"
        );

        player.sendMessage(
                ChatColor.GRAY
                        + "Gõ cancel để hủy."
        );
    }


    /*
     * RENAME
     */

    public static void startRename(
            Player player,
            String oldName
    ) {

        WAITING_RENAME.put(
                player.getUniqueId(),
                oldName
        );

        player.sendMessage(
                ChatColor.YELLOW
                        + "Nhập tên mới cho "
                        + ChatColor.WHITE
                        + oldName
                        + ChatColor.YELLOW
                        + ":"
        );

        player.sendMessage(
                ChatColor.GRAY
                        + "Gõ cancel để hủy."
        );
    }


    @EventHandler
    public void onChat(
            AsyncChatEvent event
    ) {

        Player player =
                event.getPlayer();

        UUID uuid =
                player.getUniqueId();

        String message =
                PlainTextComponentSerializer
                        .plainText()
                        .serialize(
                                event.message()
                        )
                        .trim();


        /*
         * =========================================
         * RENAME
         * =========================================
         */

        if (WAITING_RENAME.containsKey(uuid)) {

            event.setCancelled(true);

            String oldName =
                    WAITING_RENAME.get(uuid);

            if (message.equalsIgnoreCase(
                    "cancel"
            )) {

                WAITING_RENAME.remove(uuid);

                Bukkit.getScheduler().runTask(
                        Bukkit.getPluginManager()
                                .getPlugin("ShadowCore"),

                        () -> {

                            player.sendMessage(
                                    ChatColor.YELLOW
                                            + "Đã hủy rename."
                            );

                            PortalSavedLocationManageGUI
                                    .open(
                                            player,
                                            oldName
                                    );
                        }
                );

                return;
            }


            if (message.isEmpty()) {

                player.sendMessage(
                        ChatColor.RED
                                + "Tên không được để trống."
                );

                return;
            }


            PortalSavedLocations locations =
                    PortalGunGUI
                            .getSavedLocations();

            PortalSavedLocation oldLocation =
                    locations.get(oldName);

            if (oldLocation == null) {

                WAITING_RENAME.remove(uuid);

                player.sendMessage(
                        ChatColor.RED
                                + "Location không tồn tại."
                );

                return;
            }


            Location location =
                    oldLocation.toLocation();

            if (location == null) {

                WAITING_RENAME.remove(uuid);

                player.sendMessage(
                        ChatColor.RED
                                + "World của location không tồn tại."
                );

                return;
            }


            /*
             * Không cho trùng tên.
             */

            if (locations.get(message) != null) {

                player.sendMessage(
                        ChatColor.RED
                                + "Tên này đã tồn tại."
                );

                return;
            }


            locations.delete(oldName);

            locations.save(
                    message,
                    location
            );

            WAITING_RENAME.remove(uuid);

            Bukkit.getScheduler().runTask(
                    Bukkit.getPluginManager()
                            .getPlugin("ShadowCore"),

                    () -> {

                        player.sendMessage(
                                ChatColor.GREEN
                                        + "Đã đổi tên "
                                        + ChatColor.WHITE
                                        + oldName
                                        + ChatColor.GREEN
                                        + " → "
                                        + ChatColor.WHITE
                                        + message
                        );

                        PortalSavedLocationsGUI.open(
                                player
                        );
                    }
            );

            return;
        }


        /*
         * =========================================
         * SAVE NEW LOCATION
         * =========================================
         */

        if (!WAITING_SAVE.containsKey(uuid)) {
            return;
        }

        event.setCancelled(true);


        if (message.equalsIgnoreCase("cancel")) {

            WAITING_SAVE.remove(uuid);

            Bukkit.getScheduler().runTask(
                    Bukkit.getPluginManager()
                            .getPlugin("ShadowCore"),

                    () -> {

                        player.sendMessage(
                                ChatColor.YELLOW
                                        + "Đã hủy lưu location."
                        );

                        PortalSavedLocationsGUI.open(
                                player
                        );
                    }
            );

            return;
        }


        if (message.isEmpty()) {

            player.sendMessage(
                    ChatColor.RED
                            + "Tên location không được để trống."
            );

            return;
        }


        PortalSavedLocations locations =
                PortalGunGUI.getSavedLocations();

        if (locations == null) {

            WAITING_SAVE.remove(uuid);

            player.sendMessage(
                    ChatColor.RED
                            + "Saved Locations chưa được khởi tạo."
            );

            return;
        }


        if (locations.get(message) != null) {

            player.sendMessage(
                    ChatColor.RED
                            + "Tên này đã tồn tại."
            );

            return;
        }


        Location location =
                player.getLocation()
                        .clone()
                        .add(0, 1, 0);

        locations.save(
                message,
                location
        );

        WAITING_SAVE.remove(uuid);

        Bukkit.getScheduler().runTask(
                Bukkit.getPluginManager()
                        .getPlugin("ShadowCore"),

                () -> {

                    player.sendMessage(
                            ChatColor.GREEN
                                    + "Đã lưu location: "
                                    + ChatColor.WHITE
                                    + message
                    );

                    PortalSavedLocationsGUI.open(
                            player
                    );
                }
        );
    }
}