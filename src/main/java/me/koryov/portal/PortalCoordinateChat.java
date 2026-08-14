package me.koryov.portal;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PortalCoordinateChat implements Listener {

    private static final Map<UUID, double[]> WAITING_COORDINATES =
            new HashMap<>();

    private static final Map<UUID, Boolean> WAITING_WORLD =
            new HashMap<>();

    public static void start(Player player) {

        UUID uuid =
                player.getUniqueId();

        WAITING_WORLD.remove(uuid);

        WAITING_COORDINATES.put(
                uuid,
                new double[0]
        );

        player.sendMessage(
                ChatColor.YELLOW
                        + "Nhập tọa độ X Y Z:"
        );

        player.sendMessage(
                ChatColor.GRAY
                        + "Ví dụ: 100 70 -250"
        );

        player.sendMessage(
                ChatColor.RED
                        + "Gõ cancel để hủy."
        );
    }
    @EventHandler
    public void onChat(AsyncChatEvent event) {

        Player player = event.getPlayer();

        UUID uuid = player.getUniqueId();

        boolean waitingCoordinates =
                WAITING_COORDINATES.containsKey(uuid);

        boolean waitingWorld =
                WAITING_WORLD.containsKey(uuid);

        if (!waitingCoordinates && !waitingWorld) {
            return;
        }

        String message =
                PlainTextComponentSerializer.plainText()
                        .serialize(event.message())
                        .trim();

        event.setCancelled(true);

        // PHẦN CODE XỬ LÝ COORDINATE / WORLD CỦA ÔNG Ở ĐÂY

        /*
         * =========================
         * NHẬP WORLD
         * =========================
         */

        if (waitingWorld) {

            if (message.equalsIgnoreCase("cancel")) {

                clear(player);

                Bukkit.getScheduler().runTask(
                        Bukkit.getPluginManager()
                                .getPlugin("ShadowCore"),
                        () -> {

                            player.sendMessage(
                                    ChatColor.YELLOW
                                            + "Đã hủy."
                            );

                            PortalGunGUI.open(player);
                        }
                );

                return;
            }

            World world =
                    findWorld(message);

            if (world == null) {

                Bukkit.getScheduler().runTask(
                        Bukkit.getPluginManager()
                                .getPlugin("ShadowCore"),
                        () -> {

                            player.sendMessage(
                                    ChatColor.RED
                                            + "Không tìm thấy world: "
                                            + message
                            );

                            player.sendMessage(
                                    ChatColor.YELLOW
                                            + "World hiện có:"
                            );

                            for (World w :
                                    Bukkit.getWorlds()) {

                                player.sendMessage(
                                        ChatColor.WHITE
                                                + "- "
                                                + w.getName()
                                );
                            }

                            player.sendMessage(
                                    ChatColor.YELLOW
                                            + "Nhập lại tên world:"
                            );
                        }
                );

                return;
            }

            double[] coordinates =
                    WAITING_COORDINATES.get(uuid);

            if (coordinates == null) {

                clear(player);

                return;
            }

            Location location =
                    new Location(
                            world,
                            coordinates[0],
                            coordinates[1],
                            coordinates[2]
                    );

            clear(player);

            Bukkit.getScheduler().runTask(
                    Bukkit.getPluginManager()
                            .getPlugin("ShadowCore"),
                    () -> {

                        PortalGunGUI.setCoordinates(
                                player,
                                location
                        );

                        player.sendMessage(
                                ChatColor.GREEN
                                        + "Đã lưu Portal B!"
                        );

                        player.sendMessage(
                                ChatColor.GRAY
                                        + "World: "
                                        + ChatColor.WHITE
                                        + world.getName()
                        );

                        player.sendMessage(
                                ChatColor.GRAY
                                        + "XYZ: "
                                        + ChatColor.WHITE
                                        + coordinates[0]
                                        + " "
                                        + coordinates[1]
                                        + " "
                                        + coordinates[2]
                        );

                        PortalGunGUI.open(player);
                    }
            );

            return;
        }

        /*
         * =========================
         * NHẬP TỌA ĐỘ
         * =========================
         */

        if (message.equalsIgnoreCase("cancel")) {

            clear(player);

            Bukkit.getScheduler().runTask(
                    Bukkit.getPluginManager()
                            .getPlugin("ShadowCore"),
                    () -> {

                        player.sendMessage(
                                ChatColor.YELLOW
                                        + "Đã hủy."
                        );

                        PortalGunGUI.open(player);
                    }
            );

            return;
        }

        String[] parts =
                message.split("\\s+");

        if (parts.length != 3) {

            Bukkit.getScheduler().runTask(
                    Bukkit.getPluginManager()
                            .getPlugin("ShadowCore"),
                    () -> {

                        player.sendMessage(
                                ChatColor.RED
                                        + "Sai định dạng!"
                        );

                        player.sendMessage(
                                ChatColor.YELLOW
                                        + "Ví dụ: "
                                        + ChatColor.WHITE
                                        + "100 70 -250"
                        );
                    }
            );

            return;
        }

        try {

            double x =
                    Double.parseDouble(parts[0]);

            double y =
                    Double.parseDouble(parts[1]);

            double z =
                    Double.parseDouble(parts[2]);

            WAITING_COORDINATES.put(
                    uuid,
                    new double[]{
                            x,
                            y,
                            z
                    }
            );

            WAITING_WORLD.put(
                    uuid,
                    true
            );

            Bukkit.getScheduler().runTask(
                    Bukkit.getPluginManager()
                            .getPlugin("ShadowCore"),
                    () -> {

                        player.sendMessage(
                                ChatColor.GREEN
                                        + "Đã nhận tọa độ:"
                        );

                        player.sendMessage(
                                ChatColor.WHITE.toString()
                                        + x
                                        + " "
                                        + y
                                        + " "
                                        + z
                        );

                        player.sendMessage("");

                        player.sendMessage(
                                ChatColor.YELLOW
                                        + "Các world hiện có:"
                        );

                        for (World world :
                                Bukkit.getWorlds()) {

                            player.sendMessage(
                                    ChatColor.WHITE
                                            + "- "
                                            + world.getName()
                            );
                        }

                        player.sendMessage("");

                        player.sendMessage(
                                ChatColor.YELLOW
                                        + "Nhập tên world:"
                        );
                    }
            );

        } catch (NumberFormatException e) {

            Bukkit.getScheduler().runTask(
                    Bukkit.getPluginManager()
                            .getPlugin("ShadowCore"),
                    () -> {

                        player.sendMessage(
                                ChatColor.RED
                                        + "Tọa độ không hợp lệ!"
                        );
                    }
            );
        }
    }

    private static World findWorld(
            String name
    ) {

        for (World world :
                Bukkit.getWorlds()) {

            if (world.getName()
                    .equalsIgnoreCase(name)) {

                return world;
            }
        }

        return null;
    }

    private static void clear(
            Player player
    ) {

        UUID uuid =
                player.getUniqueId();

        WAITING_COORDINATES.remove(uuid);
        WAITING_WORLD.remove(uuid);
    }
}