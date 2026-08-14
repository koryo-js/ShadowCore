package me.koryov.bomb;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.Location;

public class RickPrimeBombChat implements Listener {

    @EventHandler
    public void onChat(AsyncChatEvent event) {

        Player player =
                event.getPlayer();

        String message =
                PlainTextComponentSerializer.plainText()
                        .serialize(event.message())
                        .trim();


        /*
         * ==============================================
         * KHÔNG ĐANG NHẬP BOM
         * ==============================================
         */

        if (!RickPrimeBomb.isWaiting(player)) {
            return;
        }


        /*
         * Không cho tin nhắn hiện lên chat thường.
         */

        event.setCancelled(true);


        /*
         * ==============================================
         * CANCEL
         * ==============================================
         */

        if (message.equalsIgnoreCase("cancel")) {

            RickPrimeBomb.cancelCoordinates(
                    player
            );

            Bukkit.getScheduler().runTask(
                    Bukkit.getPluginManager()
                            .getPlugin("ShadowCore"),

                    () -> player.sendMessage(
                            ChatColor.YELLOW
                                    + "Đã hủy Rick Prime Bomb."
                    )
            );

            return;
        }


        /*
         * ==============================================
         * ĐANG CHỜ WORLD
         * ==============================================
         */

        if (RickPrimeBomb.isWaitingWorld(player)) {

            final String worldName =
                    message.trim();

            Bukkit.getScheduler().runTask(
                    Bukkit.getPluginManager()
                            .getPlugin("ShadowCore"),

                    () -> {

                        World world = null;


                        /*
                         * Tìm world không phân biệt
                         * hoa thường.
                         */

                        for (World currentWorld :
                                Bukkit.getWorlds()) {

                            if (currentWorld.getName()
                                    .equalsIgnoreCase(worldName)) {

                                world = currentWorld;

                                break;
                            }
                        }


                        /*
                         * Không tìm thấy world.
                         */

                        if (world == null) {

                            player.sendMessage(
                                    ChatColor.RED
                                            + "Không tìm thấy world: "
                                            + worldName
                            );

                            player.sendMessage("");

                            player.sendMessage(
                                    ChatColor.YELLOW
                                            + "Các world hiện có:"
                            );

                            for (World currentWorld :
                                    Bukkit.getWorlds()) {

                                player.sendMessage(
                                        ChatColor.WHITE
                                                + "- "
                                                + currentWorld.getName()
                                );
                            }

                            player.sendMessage("");

                            player.sendMessage(
                                    ChatColor.YELLOW
                                            + "Nhập lại tên world:"
                            );

                            return;
                        }


                        /*
                         * World hợp lệ.
                         */

                        double[] coordinates =
                                RickPrimeBomb.getCoordinates(
                                        player
                                );

                        if (coordinates == null) {

                            player.sendMessage(
                                    ChatColor.RED
                                            + "Không tìm thấy tọa độ."
                            );

                            return;
                        }

                        Location location =
                                new Location(
                                        world,
                                        coordinates[0],
                                        coordinates[1],
                                        coordinates[2]
                                );

                        RickPrimeBomb.setLocationTarget(
                                player,
                                location
                        );

                        RickPrimeBomb.cancelCoordinates(
                                player
                        );

                        player.sendMessage(
                                ChatColor.GREEN
                                        + "Target set: "
                                        + ChatColor.WHITE
                                        + location.getBlockX()
                                        + " "
                                        + location.getBlockY()
                                        + " "
                                        + location.getBlockZ()
                                        + " ("
                                        + world.getName()
                                        + ")"
                        );
                    }
            );

            return;
        }


        /*
         * ==============================================
         * ĐANG CHỜ TỌA ĐỘ
         * ==============================================
         */

        RickPrimeBomb.setCoordinates(
                player,
                message
        );
    }
}