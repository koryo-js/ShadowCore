package me.koryov.jutsu;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;

public final class JutsuManager {
    final JavaPlugin plugin;
    private final NamespacedKey jikukanKey;
    private final UUID kcmSpeedUuid = UUID.fromString("7f8bb2b5-9c2f-4a25-a2b4-2d7f7e2a4101");
    private final Set<UUID> activeJikukan = new HashSet<>();
    private final Set<UUID> activeKcm = new HashSet<>();

    public JutsuManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.jikukanKey = new NamespacedKey(plugin, "jikukan_field");
    }

    public void start() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                refreshKcm(player);
                if (activeJikukan.contains(player.getUniqueId())) {
                    pulseJikukan(player);
                }
            }
        }, 1L, 1L);
    }

    public void castRasengan(Player player) {
        if (!JutsuItems.isChestplate(plugin, player.getInventory().getChestplate())) return;

        Location start = player.getEyeLocation().clone().add(player.getLocation().getDirection().normalize().multiply(0.7));
        Vector direction = player.getLocation().getDirection().normalize();
        World world = player.getWorld();

        player.swingMainHand();
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_STRONG, 1.0f, 1.6f);
        player.getWorld().spawnParticle(Particle.END_ROD, start, 18, .18, .18, .18, .03);

        final int maxTicks = 28;
        final double speed = 0.95;
        final double[] travelled = {0.0};
        final Location[] current = {start};

        Bukkit.getScheduler().runTaskTimer(plugin, task -> {
            if (!player.isOnline() || travelled[0] > 26) {
                task.cancel();
                return;
            }

            Location loc = current[0];
            drawRasengan(loc);
            RayTraceResult hit = world.rayTraceEntities(
                    loc, direction, 1.05,
                    entity -> entity != player && entity.isValid() && !(entity instanceof Item)
            );

            if (hit != null && hit.getHitEntity() != null) {
                Entity target = hit.getHitEntity();
                if (target instanceof LivingEntity living) {
                    living.damage(12.0, player);
                    Vector knock = direction.clone().multiply(1.35);
                    knock.setY(Math.max(0.25, knock.getY() + 0.25));
                    living.setVelocity(knock);
                } else {
                    target.setVelocity(direction.clone().multiply(1.2));
                }
                world.spawnParticle(Particle.EXPLOSION, loc, 1);
                world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 0.65f, 1.5f);
                task.cancel();
                return;
            }

            RayTraceResult blockHit = world.rayTraceBlocks(loc, direction, speed);
            if (blockHit != null) {
                world.spawnParticle(Particle.EXPLOSION, loc, 1);
                world.playSound(loc, Sound.BLOCK_GLASS_BREAK, .7f, 1.8f);
                task.cancel();
                return;
            }

            current[0] = loc.add(direction.clone().multiply(speed));
            travelled[0] += speed;
        }, 0L, 1L);
    }

    private void drawRasengan(Location center) {
        World world = center.getWorld();
        double t = System.nanoTime() / 350_000_000.0;
        for (int i = 0; i < 48; i++) {
            double u = (i + 0.5) / 48.0;
            double phi = Math.acos(1 - 2 * u);
            double theta = Math.PI * (3.0 - Math.sqrt(5.0)) * i + t * 2.0;
            double r = 0.42;
            double x = r * Math.sin(phi) * Math.cos(theta);
            double y = r * Math.cos(phi);
            double z = r * Math.sin(phi) * Math.sin(theta);
            world.spawnParticle(Particle.END_ROD, center.clone().add(x, y, z), 1, 0, 0, 0, 0);
        }
        world.spawnParticle(Particle.GLOW, center, 5, .12, .12, .12, .01);
    }

    public void activateJikukan(Player player) {
        if (!JutsuItems.isPattern(plugin, player.getInventory().getItemInMainHand())) return;
        if (activeJikukan.contains(player.getUniqueId())) return;

        activeJikukan.add(player.getUniqueId());
        Location loc = player.getLocation().clone();
        player.getWorld().playSound(loc, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, .8f, 1.4f);
        player.getWorld().spawnParticle(Particle.PORTAL, loc, 80, 3.0, 1.0, 3.0, .8);
        player.sendActionBar("§5Jikūkan Kekkai §7— kết giới đã mở");

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            activeJikukan.remove(player.getUniqueId());
            if (player.isOnline()) player.sendActionBar("§7Jikūkan Kekkai kết thúc");
        }, 100L);
    }

    private void pulseJikukan(Player owner) {
        World world = owner.getWorld();
        Location center = owner.getLocation();
        double radius = 7.0;

        for (Entity entity : world.getNearbyEntities(center, radius, radius, radius)) {
            if (!(entity instanceof Projectile projectile)) continue;
            if (projectile.getShooter() == owner) continue;

            Vector velocity = projectile.getVelocity();
            if (velocity.lengthSquared() < 0.01) continue;

            Vector radial = projectile.getLocation().toVector().subtract(center.toVector());
            if (radial.lengthSquared() < 0.01) radial = owner.getLocation().getDirection().clone();
            radial.normalize();

            Vector tangent = new Vector(-radial.getZ(), 0, radial.getX()).normalize();
            Vector redirected = velocity.clone().normalize().multiply(Math.max(0.65, velocity.length()));
            redirected.multiply(0.45).add(tangent.multiply(redirected.length() * 0.9));
            projectile.setVelocity(redirected);

            Location moved = projectile.getLocation().clone().add(tangent.multiply(0.12));
            projectile.teleport(moved);
        }

        world.spawnParticle(Particle.ENCHANT, center, 12, radius, .15, radius, .01);
    }

    public void onProjectileHit(Projectile projectile) {
        if (projectile == null || projectile.isDead()) return;
        // Paper will call this when a projectile hits. The field itself continuously
        // redirects projectiles, so no persistent state is required here.
    }

    public void refreshKcm(Player player) {
        boolean wearing = JutsuItems.isChestplate(plugin, player.getInventory().getChestplate());
        if (wearing) {
            activeKcm.add(player.getUniqueId());
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 1, true, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 40, 1, true, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 40, 1, true, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 40, 0, true, false, false));

            AttributeInstance speed = player.getAttribute(Attribute.MOVEMENT_SPEED);
            if (speed != null && speed.getModifier(kcmSpeedUuid) == null) {
                speed.addModifier(new AttributeModifier(
                        kcmSpeedUuid, "shadowcore.kcm.speed", 0.18,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                ));
            }

            Location aura = player.getLocation().add(0, 1.0, 0);
            player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, aura, 8, .55, .9, .55, .015);
            player.getWorld().spawnParticle(Particle.ENCHANT, aura, 14, .8, 1.0, .8, .1);
        } else {
            activeKcm.remove(player.getUniqueId());
            removeKcmModifier(player);
        }
    }

    public void removeKcmModifier(Player player) {
        activeKcm.remove(player.getUniqueId());
        AttributeInstance speed = player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (speed != null && speed.getModifier(kcmSpeedUuid) != null) {
            speed.removeModifier(kcmSpeedUuid);
        }
        for (PotionEffectType type : List.of(
                PotionEffectType.SPEED, PotionEffectType.STRENGTH,
                PotionEffectType.JUMP_BOOST, PotionEffectType.RESISTANCE
        )) {
            PotionEffect effect = player.getPotionEffect(type);
            if (effect != null && effect.getAmplifier() <= 1) player.removePotionEffect(type);
        }
    }
}
