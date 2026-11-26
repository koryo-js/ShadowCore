package me.koryov.phoenix;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

/**
 * Buồng hồi sinh dạng trụ đứng 3x3x5 với cột bong bóng đẩy lên,
 * hiệu ứng vỡ kính và nước tràn ra khi hoàn tất.
 */
public final class PhoenixChamber {

    private static final Material GLASS_TYPE = Material.LIGHT_BLUE_STAINED_GLASS;

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

        // Đáy 3x3: Viền khối sắt, tâm là cát linh hồn tạo cột bong bóng
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Material floorMat = (x == 0 && z == 0) ? Material.SOUL_SAND : Material.IRON_BLOCK;
                set(world, cx + x, cy - 1, cz + z, floorMat, true);
            }
        }

        // Tường kính bao quanh 3x3 cao 3 block (chừa lõi 1x1 ở giữa)
        for (int y = 0; y <= 2; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && z == 0) {
                        continue;
                    }
                    set(world, cx + x, cy + y, cz + z, GLASS_TYPE, false);
                }
            }
        }

        // Nóc 3x3: Khối sắt với Beacon ở chính giữa
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Material roofMat = (x == 0 && z == 0) ? Material.BEACON : Material.IRON_BLOCK;
                set(world, cx + x, cy + 3, cz + z, roofMat, true);
            }
        }

        // Đổ đầy nước cột giữa (bật physics = true để tạo cột bong bóng)
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

        // Chỉ đóng lại cột 1x3 chính giữa ở mặt trước (z + 1)
        for (int y = 0; y <= 2; y++) {
            set(world, cx, cy + y, cz + 1, GLASS_TYPE, false);
        }
        clearSpilledWater(spawn);
        fillWater(spawn);
    }

    /**
     * Chỉ phá vỡ cột kính 1x3 chính giữa mặt trước để nước tràn qua khe cửa.
     */
    public static void shatterAndSpill(Location spawn, Plugin plugin) {
        World world = spawn.getWorld();
        if (world == null) {
            return;
        }

        int cx = spawn.getBlockX();
        int cy = spawn.getBlockY();
        int cz = spawn.getBlockZ();

        // Âm thanh vỡ kính + xả áp lực nước
        Location frontCenter = spawn.clone().add(0.0, 1.0, 1.0);
        world.playSound(frontCenter, Sound.BLOCK_GLASS_BREAK, 1.6F, 0.85F);
        world.playSound(frontCenter, Sound.ENTITY_PLAYER_SPLASH_HIGH_SPEED, 1.2F, 1.0F);

        // Chỉ phá 3 block kính ngay chính giữa (x = 0, z + 1)
        for (int y = 0; y <= 2; y++) {
            Block block = world.getBlockAt(cx, cy + y, cz + 1);
            if (block.getType() == GLASS_TYPE) {
                world.playEffect(block.getLocation(), Effect.STEP_SOUND, GLASS_TYPE);
                block.setType(Material.AIR, true);
            }
        }

        // Sau 2.5 giây (50 ticks), tự dọn nước tràn phía ngoài
        Bukkit.getScheduler().runTaskLater(plugin, () -> clearSpilledWater(spawn), 50L);
    }

    private static void fillWater(Location spawn) {
        World world = spawn.getWorld();
        if (world == null) {
            return;
        }

        int cx = spawn.getBlockX();
        int cy = spawn.getBlockY();
        int cz = spawn.getBlockZ();

        // Đặt nước nguồn từ y = 0 đến y = 2 với physics = true để kích hoạt bọt khí Soul Sand
        for (int y = 0; y <= 2; y++) {
            set(world, cx, cy + y, cz, Material.WATER, true);
        }
    }

    private static void clearSpilledWater(Location spawn) {
        World world = spawn.getWorld();
        if (world == null) {
            return;
        }

        int cx = spawn.getBlockX();
        int cy = spawn.getBlockY();
        int cz = spawn.getBlockZ();

        // Dọn nước chảy tràn ở khu vực phía trước buồng (z từ +1 đến +8)
        for (int x = -6; x <= 6; x++) {
            for (int z = 1; z <= 8; z++) {
                for (int y = -1; y <= 2; y++) {
                    Block block = world.getBlockAt(cx + x, cy + y, cz + z);
                    if (block.getType() == Material.WATER) {
                        block.setType(Material.AIR, false);
                    }
                }
            }
        }
    }

    private static void set(World world, int x, int y, int z, Material material, boolean applyPhysics) {
        Block block = world.getBlockAt(x, y, z);
        block.setType(material, applyPhysics);
    }
}
