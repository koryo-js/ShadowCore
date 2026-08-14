package me.koryov.bomb;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PortalBombTask extends BukkitRunnable {

    private final Player source;
    private final Location target;

    private int ticks = 0;

    public PortalBombTask(
            Player source,
            Location target
    ) {

        this.source = source;

        this.target =
                target.clone();
    }


    @Override
    public void run() {

        /*
         * Nếu người kích hoạt offline
         * thì hủy task.
         */

        if (!source.isOnline()) {

            cancel();

            return;
        }


        World world =
                target.getWorld();

        if (world == null) {

            cancel();

            return;
        }


        /*
         * ==========================================
         * PORTAL RƠI XUỐNG
         * ==========================================
         */

        double progress =
                Math.min(
                        ticks / 40.0,
                        1.0
                );


        Location portalLocation =
                target.clone().add(
                        0,
                        10.0 * (1.0 - progress),
                        0
                );


        /*
         * Particle portal.
         */

        world.spawnParticle(
                org.bukkit.Particle.PORTAL,
                portalLocation,
                30,
                1.0,
                1.0,
                1.0,
                0.2
        );


        /*
         * Sound khi portal đang rơi.
         */

        if (ticks % 10 == 0) {

            world.playSound(
                    portalLocation,
                    Sound.BLOCK_PORTAL_AMBIENT,
                    0.3f,
                    1.0f
            );
        }


        /*
         * ==========================================
         * ĐÃ RƠI XUỐNG
         * ==========================================
         */

        if (ticks >= 40) {

            /*
             * Cảnh báo trước khi nổ.
             */

            world.playSound(
                    target,
                    Sound.BLOCK_BEACON_POWER_SELECT,
                    0.3f,
                    0.5f
            );


            /*
             * Chờ thêm 2 giây.
             */

            if (ticks < 80) {

                world.spawnParticle(
                        org.bukkit.Particle.PORTAL,
                        target,
                        80,
                        1.5,
                        1.5,
                        1.5,
                        0.3
                );

                ticks++;

                return;
            }


            /*
             * ======================================
             * BOOM
             * ======================================
             */

            world.playSound(
                    target,
                    Sound.ENTITY_GENERIC_EXPLODE,
                    0.3f,
                    0.6f
            );


            world.createExplosion(
                    target,
                    6.0f,
                    false,
                    true
            );


            /*
             * Xóa target đã lưu.
             */

            RickPrimeBomb.clearTarget(
                    source
            );


            cancel();

            return;
        }


        ticks++;
    }
}