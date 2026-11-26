package me.koryov.phoenix;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

/**
 * Builds and animates a compact physical resurrection chamber.
 * The chamber is a 5x5x5 cyan-glass capsule with a water-filled interior,
 * a soul-sand floor and a beacon cap.
 */
public final class PhoenixChamber {

    private PhoenixChamber() {
    }

    public static void build(Location spawn) {
        World world = spawn.getWorld();
        if (world == null) {
            return;
        }

        int cx = spawn.getBlockX();
        int cy = spawn.getBlockY();
        int cz = spawn.getBlockZ();

        // Floor: quartz frame with a soul-sand revival platform.
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                set(world, cx + x, cy - 1, cz + z,
                        Math.abs(x) == 2 || Math.abs(z) == 2
                                ? Material.QUARTZ_BLOCK : Material.SOUL_SAND);
            }
        }

        // Cyan-glass walls and a sealed front panel.
        for (int y = 0; y <= 2; y++) {
            for (int offset = -2; offset <= 2; offset++) {
                set(world, cx - 2, cy + y, cz + offset, Material.CYAN_STAINED_GLASS);
                set(world, cx + 2, cy + y, cz + offset, Material.CYAN_STAINED_GLASS);
                set(world, cx + offset, cy + y, cz - 2, Material.CYAN_STAINED_GLASS);
                set(world, cx + offset, cy + y, cz + 2, Material.CYAN_STAINED_GLASS);
            }
        }

        // Roof with a beacon core.
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                set(world, cx + x, cy + 3, cz + z,
                        (x == 0 && z == 0) ? Material.BEACON : Material.IRON_BLOCK);
            }
        }

        // Water fills the sealed capsule while the revival sequence runs.
        fillWater(spawn);
    }

    public static void close(Location spawn) {
        World world = spawn.getWorld();
        if (world == null) {
            return;
        }

        int cx = spawn.getBlockX();
        int cy = spawn.getBlockY();
        int cz = spawn.getBlockZ();

        for (int y = 0; y <= 2; y++) {
            for (int x = -1; x <= 1; x++) {
                set(world, cx + x, cy + y, cz + 2, Material.CYAN_STAINED_GLASS);
            }
        }
        fillWater(spawn);
    }

    public static void open(Location spawn) {
        World world = spawn.getWorld();
        if (world == null) {
            return;
        }

        int cx = spawn.getBlockX();
        int cy = spawn.getBlockY();
        int cz = spawn.getBlockZ();

        // Drain the interior before opening the front.
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int y = 0; y <= 1; y++) {
                    set(world, cx + x, cy + y, cz + z, Material.AIR);
                }
            }
        }

        for (int y = 0; y <= 2; y++) {
            for (int x = -1; x <= 1; x++) {
                set(world, cx + x, cy + y, cz + 2, Material.AIR);
            }
        }
    }

    public static Location getExit(Location spawn) {
        Location exit = spawn.clone().add(0.0, 0.0, 3.5);
        exit.setYaw(0.0F);
        exit.setPitch(0.0F);
        return exit;
    }

    private static void fillWater(Location spawn) {
        World world = spawn.getWorld();
        if (world == null) {
            return;
        }

        int cx = spawn.getBlockX();
        int cy = spawn.getBlockY();
        int cz = spawn.getBlockZ();

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int y = 0; y <= 1; y++) {
                    set(world, cx + x, cy + y, cz + z, Material.WATER);
                }
            }
        }
    }

    private static void set(World world, int x, int y, int z, Material material) {
        Block block = world.getBlockAt(x, y, z);
        block.setType(material, false);
    }
}
