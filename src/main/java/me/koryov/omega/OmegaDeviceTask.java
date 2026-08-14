package me.koryov.omega;

import net.kyori.adventure.text.Component;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class OmegaDeviceTask extends BukkitRunnable {

    private final Player source;
    private final Player target;

    private int ticks = 0;

    public OmegaDeviceTask(
            Player source,
            Player target
    ) {

        this.source = source;
        this.target = target;
    }

    @Override
    public void run() {

        /*
         * Source hoặc target offline
         */

        if (!source.isOnline()
                || !target.isOnline()) {

            OmegaDevice.clearTarget(source);

            cancel();

            return;
        }

        Location center =
                target.getLocation()
                        .clone()
                        .add(0, 1, 0);

        World world =
                center.getWorld();

        if (world == null) {

            OmegaDevice.clearTarget(source);

            cancel();

            return;
        }


        /*
         * ==========================================
         * PHASE 1
         * Tím xuất hiện quanh target
         * 30 ticks = 1.5 giây
         * ==========================================
         */

        if (ticks < 150) {

            world.spawnParticle(
                    Particle.DUST_COLOR_TRANSITION,
                    center,
                    12,
                    0.8,
                    1.0,
                    0.8,
                    0,
                    new Particle.DustTransition(
                            Color.PURPLE,
                            Color.BLACK,
                            1.2f
                    )
            );

            /*
             * Sound rất nhỏ
             */

            if (ticks % 15 == 0) {

                world.playSound(
                        center,
                        Sound.BLOCK_PORTAL_AMBIENT,
                        0.25f,
                        0.8f
                );
            }

            ticks++;

            return;
        }


        /*
         * ==========================================
         * PHASE 2
         * Vòng khóa mục tiêu
         * 20 ticks = 1 giây
         * ==========================================
         */

        if (ticks < 250) {

            double radius = 1.5;

            for (int i = 0; i < 32; i++) {

                double angle =
                        (Math.PI * 2 * i / 32.0)
                                + ticks * 0.15;

                double x =
                        Math.cos(angle)
                                * radius;

                double z =
                        Math.sin(angle)
                                * radius;

                Location ring =
                        center.clone()
                                .add(
                                        x,
                                        -0.8,
                                        z
                                );

                world.spawnParticle(
                        Particle.DUST_COLOR_TRANSITION,
                        ring,
                        1,
                        0,
                        0,
                        0,
                        0,
                        new Particle.DustTransition(
                                Color.PURPLE,
                                Color.BLACK,
                                1.5f
                        )
                );
            }

            /*
             * Âm thanh khóa mục tiêu
             */

            if (ticks % 10 == 0) {

                world.playSound(
                        center,
                        Sound.BLOCK_BEACON_POWER_SELECT,
                        0.35f,
                        0.7f
                );
            }

            ticks++;

            return;
        }


        /*
         * ==========================================
         * OMEGA ERASE
         * ==========================================
         */

        target.kick(
                Component.text(
                        "Your existence has been erased by the Omega Device."
                )
        );

        source.sendMessage(
                ChatColor.DARK_PURPLE
                        + "☢ Omega Device activated."
        );

        OmegaDevice.clearTarget(
                source
        );

        cancel();
    }
}