package me.koryov.bomb;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RickPrimeBomb implements Listener {

    private static final Map<UUID, Player> BOMB_PLAYERS =
            new HashMap<>();

    private static final Map<UUID, Location> BOMB_LOCATIONS =
            new HashMap<>();

    private static final String BOMB_NAME =
            ChatColor.DARK_RED + "Neutrino Bomb";

    private static final Map<UUID, double[]> WAITING_COORDINATES =
            new HashMap<>();


    /*
     * ==================================================
     * COORDINATES
     * ==================================================
     */

    public static void openCoordinates(Player player) {

        WAITING_COORDINATES.put(
                player.getUniqueId(),
                new double[0]
        );

        player.sendMessage(
                ChatColor.YELLOW
                        + "Nhập tọa độ theo dạng: "
                        + ChatColor.WHITE
                        + "X Y Z"
        );
    }


    public static boolean setCoordinates(
            Player player,
            String message
    ) {

        String[] parts =
                message.trim().split("\\s+");

        if (parts.length != 3) {

            player.sendMessage(
                    ChatColor.RED
                            + "Sai định dạng. Ví dụ: 100 70 -250"
            );

            return false;
        }

        try {

            double x =
                    Double.parseDouble(parts[0]);

            double y =
                    Double.parseDouble(parts[1]);

            double z =
                    Double.parseDouble(parts[2]);

            WAITING_COORDINATES.put(
                    player.getUniqueId(),
                    new double[] {
                            x,
                            y,
                            z
                    }
            );

            player.sendMessage(
                    ChatColor.GREEN
                            + "Đã nhận tọa độ: "
                            + ChatColor.WHITE
                            + x + " "
                            + y + " "
                            + z
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
                            + "Nhập tên world:"
            );

            return true;

        } catch (NumberFormatException e) {

            player.sendMessage(
                    ChatColor.RED
                            + "Tọa độ không hợp lệ."
            );

            return false;
        }
    }


    public static boolean isWaiting(Player player) {

        return WAITING_COORDINATES.containsKey(
                player.getUniqueId()
        );
    }
    public static double[] getCoordinates(
            Player player
    ) {

        return WAITING_COORDINATES.get(
                player.getUniqueId()
        );
    }


    /*
     * Có đủ X Y Z
     * = đang chờ nhập WORLD.
     */

    public static boolean isWaitingWorld(
            Player player
    ) {

        double[] coordinates =
                WAITING_COORDINATES.get(
                        player.getUniqueId()
                );

        return coordinates != null
                && coordinates.length == 3;
    }


    public static void cancelCoordinates(
            Player player
    ) {

        WAITING_COORDINATES.remove(
                player.getUniqueId()
        );
    }


    /*
     * ==================================================
     * PLAYER TARGET
     * ==================================================
     */

    public static void explode(
            Player source,
            Player target
    ) {

        if (target == null
                || !target.isOnline()) {

            source.sendMessage(
                    ChatColor.RED
                            + "Player không còn online."
            );

            return;
        }

        Location location =
                target.getLocation().clone();

        World world =
                location.getWorld();

        if (world == null) {
            return;
        }

        TNTPrimed tnt =
                world.spawn(
                        location,
                        TNTPrimed.class
                );

        tnt.customName(
                net.kyori.adventure.text.Component
                        .text("Neutrino Bomb")
        );

        tnt.setFuseTicks(0);

        tnt.setYield(4.0f);

        tnt.setIsIncendiary(false);

        world.playSound(
                location,
                Sound.ENTITY_TNT_PRIMED,
                2.0f,
                1.0f
        );

        source.sendMessage(
                ChatColor.GREEN
                        + "Đã kích hoạt "
                        + BOMB_NAME
                        + ChatColor.GREEN
                        + " → "
                        + ChatColor.WHITE
                        + target.getName()
        );
    }


    /*
     * ==================================================
     * COORDINATE EXPLOSION
     * ==================================================
     */

    public static void explodeAt(
            Player source,
            World world
    ) {

        double[] coordinates =
                WAITING_COORDINATES.get(
                        source.getUniqueId()
                );

        if (coordinates == null
                || coordinates.length != 3) {

            source.sendMessage(
                    ChatColor.RED
                            + "Không tìm thấy tọa độ."
            );

            return;
        }

        double x =
                coordinates[0];

        double y =
                coordinates[1];

        double z =
                coordinates[2];

        Location location =
                new Location(
                        world,
                        x,
                        y,
                        z
                );

        world.playSound(
                location,
                Sound.ENTITY_GENERIC_EXPLODE,
                2.0f,
                0.8f
        );

        world.createExplosion(
                location,
                6.0f,
                false,
                true
        );

        WAITING_COORDINATES.remove(
                source.getUniqueId()
        );

        source.sendMessage(
                ChatColor.GREEN
                        + "💥 Neutrino Bomb đã nổ tại "
                        + ChatColor.WHITE
                        + x + " "
                        + y + " "
                        + z
                        + ChatColor.GRAY
                        + " ("
                        + world.getName()
                        + ")"
        );
    }


    /*
     * ==================================================
     * CREATE NEUTRINO BOMB ITEM
     * ==================================================
     */

    public static ItemStack createBombItem() {

        ItemStack bomb =
                new ItemStack(Material.TNT);

        ItemMeta meta =
                bomb.getItemMeta();

        if (meta != null) {

            meta.setDisplayName(
                    BOMB_NAME
            );

            bomb.setItemMeta(meta);
        }

        return bomb;
    }
    public static void setPlayerTarget(
            Player player,
            Player target
    ) {

        BOMB_PLAYERS.put(
                player.getUniqueId(),
                target
        );

        BOMB_LOCATIONS.remove(
                player.getUniqueId()
        );
    }
    public static void setLocationTarget(
            Player player,
            Location location
    ) {

        BOMB_LOCATIONS.put(
                player.getUniqueId(),
                location.clone()
        );

        BOMB_PLAYERS.remove(
                player.getUniqueId()
        );
    }
    public static void activate(
            Player player
    ) {

        Player target =
                BOMB_PLAYERS.get(
                        player.getUniqueId()
                );

        Location location =
                BOMB_LOCATIONS.get(
                        player.getUniqueId()
                );

        if (target == null
                && location == null) {

            player.sendMessage(
                    ChatColor.RED
                            + "Chưa chọn mục tiêu."
            );

            return;
        }

        if (target != null) {

            if (!target.isOnline()) {

                player.sendMessage(
                        ChatColor.RED
                                + "Player không còn online."
                );

                return;
            }

            location =
                    target.getLocation().clone();
        }

        /*
         * Không xóa target ở đây.
         * PortalBombTask sẽ xử lý animation
         * rồi mới clear sau khi nổ.
         */

        new PortalBombTask(
                player,
                location
        ).runTaskTimer(
                Bukkit.getPluginManager()
                        .getPlugin("ShadowCore"),
                0L,
                1L
        );
    }
    public static void clearTarget(
            Player player
    ) {

        BOMB_PLAYERS.remove(
                player.getUniqueId()
        );

        BOMB_LOCATIONS.remove(
                player.getUniqueId()
        );
    }
}