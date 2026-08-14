package me.koryov.portal;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.util.Vector;

public class PortalRenderer {

    private static final DustOptions OUTER =
            new DustOptions(
                    Color.fromRGB(40, 255, 40),
                    1.7F
            );

    private static final DustOptions INNER =
            new DustOptions(
                    Color.fromRGB(120, 255, 120),
                    1.1F
            );

    public static void render(

            Location center,

            Vector forward,

            double rot,

            float portalScale

    ) {

        Vector normal =
                forward.clone().normalize();

        Vector up =
                new Vector(0, 1, 0);

        Vector right =
                normal.clone().crossProduct(up);

        if (right.lengthSquared() < 0.001) {

            right = new Vector(1, 0, 0);

        }

        right.normalize();

        up =
                right.clone()
                        .crossProduct(normal)
                        .normalize();

        double radius =
                1.6
                        + Math.sin(rot * 2.4) * 0.08;

        for (double t = 0; t < Math.PI * 2; t += Math.PI / 72) {

            double swirl =

                    Math.sin(rot * 4 + t * 5)

                            * 0.10;

            double x =
                    Math.cos(t + rot) * (1 + swirl);

            double y =
                    Math.sin(t + rot) * (1 + swirl);

            Vector offset =

                    right.clone().multiply(x * radius)
                            .add(
                                    up.clone().multiply(y * radius)
                            );

            center.getWorld().spawnParticle(

                    Particle.DUST,

                    center.clone().add(offset),

                    1,

                    0,
                    0,
                    0,

                    0,

                    OUTER

            );

        }
        for (double t = 0; t < Math.PI * 2; t += Math.PI / 72) {

            double x = Math.cos(-(t + rot * 1.6));

            double y = Math.sin(-(t + rot * 1.6));

            Vector offset =

                    right.clone().multiply(x * (radius * 0.82))
                            .add(
                                    up.clone().multiply(y * (radius * 0.82))
                            );

            center.getWorld().spawnParticle(

                    Particle.DUST,

                    center.clone().add(offset),

                    1,

                    0,
                    0,
                    0,

                    0,

                    new DustOptions(
                            Color.fromRGB(180, 255, 180),
                            0.8F
                    )

            );

        }

        for (double r = .2; r < radius; r += .14) {

            double a = rot * 3 + r * 7;

            Vector offset =

                    right.clone().multiply(
                            Math.cos(a) * r
                    ).add(

                            up.clone().multiply(
                                    Math.sin(a) * r
                            )

                    );

            center.getWorld().spawnParticle(

                    Particle.DUST,

                    center.clone().add(offset),

                    1,

                    0,
                    0,
                    0,

                    0,

                    INNER

            );

        }
        for (double x = -0.9; x <= 0.9; x += 0.15) {

            for (double y = -0.9; y <= 0.9; y += 0.15) {

                if (x * x + y * y > 0.82)
                    continue;

                double wave =

                        Math.sin(

                                rot * 5

                                        + x * 8

                                        + y * 8

                        ) * 0.10;

                Vector offset =

                        right.clone().multiply(x)
                                .add(

                                        up.clone().multiply(y)

                                )
                                .add(

                                        forward.clone().normalize().multiply(wave)

                                );

                center.getWorld().spawnParticle(

                        Particle.DUST,

                        center.clone().add(offset),

                        1,

                        0,
                        0,
                        0,

                        0,

                        new DustOptions(

                                Color.fromRGB(
                                        70,
                                        255,
                                        120
                                ),

                                0.55F

                        )

                );

            }

            // ==========================
            // Energy Sparks
            // ==========================

            for (int i = 0; i < 12; i++) {

                double angle = rot * 1.5 + (Math.PI * 2 / 12D) * i;

                double distance=

                        (1.7+

                                Math.sin(

                                        rot*6+i

                                )*0.15)

                                *portalScale;

                Vector offset =
                        right.clone().multiply(Math.cos(angle) * distance)
                                .add(
                                        up.clone().multiply(Math.sin(angle) * distance)
                                );

                center.getWorld().spawnParticle(

                        Particle.DUST,

                        center.clone().add(offset),

                        1,

                        0,
                        0,
                        0,

                        0,

                        new DustOptions(
                                Color.fromRGB(210, 255, 210),
                                0.45F
                        )

                );

            }
            for (int i = 0; i < 40; i++) {

                double angle =
                        -rot * 2.5 +
                                (Math.PI * 2 / 40D) * i;

                double distance =
                        (0.95 +
                                Math.sin(rot * 5 + i) * 0.08)
                                * portalScale;

                Vector offset =
                        right.clone()
                                .multiply(Math.cos(angle) * distance)
                                .add(
                                        up.clone()
                                                .multiply(Math.sin(angle) * distance)
                                );

                center.getWorld().spawnParticle(

                        Particle.DUST,

                        center.clone().add(offset),

                        1,

                        0,
                        0,
                        0,

                        0,

                        INNER

                );

            }
            center.getWorld().spawnParticle(

                    Particle.END_ROD,

                    center,

                    2,

                    0.08,
                    0.08,
                    0.08,

                    0

            );

        }

    }
}