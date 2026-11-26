package me.koryov.forcefield;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public final class ForcefieldDisplay {

    private ForcefieldDisplay() {
    }

    public static void show(JavaPlugin plugin, Player player, double damage, Location attackerLocation) {
        Location origin = player.getLocation();
        org.bukkit.util.Vector toward = attackerLocation == null
                ? origin.getDirection().multiply(-1)
                : attackerLocation.toVector().subtract(origin.toVector());
        toward.setY(0);

        if (toward.lengthSquared() < 0.0001) {
            toward = origin.getDirection().multiply(-1);
            toward.setY(0);
        }
        toward.normalize();

        double width = Math.max(1.1, Math.min(2.8, 1.0 + damage * 0.12));
        Location center = origin.clone().add(0, 1.05, 0).add(toward.clone().multiply(0.72));
        org.bukkit.util.Vector right = new org.bukkit.util.Vector(-toward.getZ(), 0, toward.getX());

        List<Entity> displays = new ArrayList<>();
        int segments = 7;

        for (int i = 0; i < segments; i++) {
            double t = (i / (double) (segments - 1)) - 0.5;
            double angle = t * Math.toRadians(105);
            org.bukkit.util.Vector offset = right.clone().multiply(Math.sin(angle) * width * 0.55)
                    .add(toward.clone().multiply((Math.cos(angle) - 1.0) * 0.18));
            Location point = center.clone().add(offset);

            BlockDisplay display = (BlockDisplay) player.getWorld().spawnEntity(point, EntityType.BLOCK_DISPLAY);
            display.setBlock(Material.GLASS.createBlockData());
            display.setBillboard(Display.Billboard.FIXED);
            display.setInvulnerable(true);
            display.setPersistent(false);
            display.setGravity(false);

            float segmentWidth = (float) (width / segments * 1.08);
            display.setTransformation(new Transformation(
                    new Vector3f(-segmentWidth / 2f, -0.58f, -0.035f),
                    new Quaternionf(),
                    new Vector3f(segmentWidth, 1.16f, 0.07f),
                    new Quaternionf()
            ));

            float yaw = (float) Math.atan2(-toward.getX(), toward.getZ());
            display.setRotation((float) Math.toDegrees(yaw), 0);
            displays.add(display);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Entity entity : displays) {
                    if (entity.isValid()) {
                        entity.remove();
                    }
                }
            }
        }.runTaskLater(plugin, 5L);
    }
}
