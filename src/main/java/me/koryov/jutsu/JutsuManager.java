package me.koryov.jutsu;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.persistence.PersistentDataType;
import me.koryov.hiraishin.HiraishinManager;
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
    private final Map<UUID, Location> jikukanTargets = new HashMap<>();
    private final HiraishinManager hiraishinManager;
    private final Set<UUID> activeKcm = new HashSet<>();
    private final Map<UUID, EnumSet<PotionEffectType>> kcmOwnedEffects = new HashMap<>();

    public JutsuManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.jikukanKey = new NamespacedKey(plugin, "jikukan_field");
        this.hiraishinManager = new HiraishinManager(plugin);
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

        Location target = findHiraishinTarget(player);
        if (target == null) {
            player.sendActionBar("§5Jikūkan Kekkai §7— cần một Hiraishin Kunai đã đánh dấu");
            return;
        }

        activeJikukan.add(player.getUniqueId());
        jikukanTargets.put(player.getUniqueId(), target.clone());

        Location loc = player.getLocation().clone();
        player.getWorld().playSound(loc, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, .8f, 1.4f);
        player.getWorld().spawnParticle(Particle.PORTAL, loc, 80, 3.0, 1.0, 3.0, .8);
        drawJikukanPortal(loc, target);
        player.sendActionBar("§5Jikūkan Kekkai §7— cổng liên kết với Hiraishin Kunai");

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            activeJikukan.remove(player.getUniqueId());
            jikukanTargets.remove(player.getUniqueId());
            if (player.isOnline()) player.sendActionBar("§7Jikūkan Kekkai kết thúc");
        }, 100L);
    }

    /**
     * Jikūkan Kekkai absorbs an incoming projectile's current vector,
     * moves the projectile to the linked Hiraishin mark, then restores
     * that exact vector. The Hiraishin mark itself is never consumed.
     */
    private void pulseJikukan(Player owner) {
        Location center = owner.getLocation();
        Location target = findHiraishinTarget(owner);
        if (target == null) {
            activeJikukan.remove(owner.getUniqueId());
            jikukanTargets.remove(owner.getUniqueId());
            return;
        }

        jikukanTargets.put(owner.getUniqueId(), target.clone());

        World world = center.getWorld();
        if (world == null) return;
        double radius = 7.0;

        for (Entity entity : world.getNearbyEntities(center, radius, radius, radius)) {
            if (!(entity instanceof Projectile projectile)) continue;
            if (projectile.getShooter() == owner) continue;
            if (projectile instanceof Trident trident && isHiraishinKunai(trident)) continue;

            Vector velocity = projectile.getVelocity();
            if (velocity.lengthSquared() < 0.01) continue;

            Vector absorbedVector = velocity.clone();
            if (!projectile.teleport(target)) continue;

            projectile.setVelocity(absorbedVector);
            spawnPortalArrival(target);
        }

        world.spawnParticle(Particle.ENCHANT, center, 12, radius, .15, radius, .01);
        drawJikukanPortal(center, target);
    }

    private void drawJikukanPortal(Location origin, Location target) {
        if (origin.getWorld() != null) {
            origin.getWorld().spawnParticle(Particle.PORTAL, origin.clone().add(0, 1.0, 0),
                    18, .45, .8, .45, .08);
        }
        spawnPortalArrival(target);
    }

    private void spawnPortalArrival(Location target) {
        if (target == null || target.getWorld() == null) return;
        Location center = target.clone().add(0, 1.0, 0);
        target.getWorld().spawnParticle(Particle.PORTAL, center, 18, .45, .8, .45, .08);
        target.getWorld().spawnParticle(Particle.END_ROD, center, 5, .18, .35, .18, .01);
    }

    private boolean isHiraishinKunai(Trident trident) {
        return trident.getPersistentDataContainer().has(
                new NamespacedKey(plugin, "hiraishin_mark_entity"),
                PersistentDataType.BYTE
        );
    }

    /**
     * Reads the existing Hiraishin mark data without changing the Hiraishin module.
     * The newest valid mark is used as the Jikūkan destination.
     */
    private Location findHiraishinTarget(Player player) {
        String raw = player.getPersistentDataContainer().get(
                new NamespacedKey(plugin, "hiraishin_marks"),
                PersistentDataType.STRING
        );
        if (raw == null || raw.isBlank()) return null;

        String[] marks = raw.split(";");
        for (int i = marks.length - 1; i >= 0; i--) {
            String[] fields = marks[i].split(",");
            if (fields.length != 5) continue;

            try {
                UUID entityId = UUID.fromString(fields[0]);
                UUID worldId = UUID.fromString(fields[1]);
                double x = Double.parseDouble(fields[2]);
                double y = Double.parseDouble(fields[3]);
                double z = Double.parseDouble(fields[4]);

                Entity entity = Bukkit.getEntity(entityId);
                if (entity instanceof Trident trident && trident.isValid() && isHiraishinKunai(trident)) {
                    return trident.getLocation().clone();
                }

                World world = Bukkit.getWorld(worldId);
                if (world != null) return new Location(world, x, y, z);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return null;
    }

    public void teleportJikukan(Player player) {
        if (!JutsuItems.isPattern(plugin, player.getInventory().getItemInMainHand())) return;
        if (!hiraishinManager.hasMark(player)) {
            player.sendActionBar("§5Jikūkan §7— không có Hiraishin Kunai");
            return;
        }
        hiraishinManager.teleportToKunai(player);
    }

    public void onProjectileHit(Projectile projectile) {
        if (projectile == null || projectile.isDead()) return;
        // Paper will call this when a projectile hits. The field itself continuously
        // redirects projectiles, so no persistent state is required here.
    }

    public void refreshKcm(Player player) {
        boolean wearing = JutsuItems.isChestplate(plugin, player.getInventory().getChestplate())
                && JutsuItems.isHokageHat(plugin, player.getInventory().getHelmet());
        if (wearing) {
            activeKcm.add(player.getUniqueId());
            EnumSet<PotionEffectType> owned = kcmOwnedEffects.computeIfAbsent(
                    player.getUniqueId(), ignored -> EnumSet.noneOf(PotionEffectType.class)
            );

            addKcmEffect(player, PotionEffectType.SPEED, 1, owned);
            addKcmEffect(player, PotionEffectType.STRENGTH, 1, owned);
            addKcmEffect(player, PotionEffectType.JUMP_BOOST, 1, owned);
            addKcmEffect(player, PotionEffectType.RESISTANCE, 0, owned);

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

    private void addKcmEffect(Player player, PotionEffectType type, int amplifier,
                               EnumSet<PotionEffectType> owned) {
        PotionEffect current = player.getPotionEffect(type);
        if (current == null || owned.contains(type)) {
            player.addPotionEffect(new PotionEffect(type, 40, amplifier, true, false, false));
            owned.add(type);
        }
    }

    public boolean isKcmActive(Player player) {
        return activeKcm.contains(player.getUniqueId());
    }

    public void removeKcmModifier(Player player) {
        activeKcm.remove(player.getUniqueId());
        AttributeInstance speed = player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (speed != null && speed.getModifier(kcmSpeedUuid) != null) {
            speed.removeModifier(kcmSpeedUuid);
        }
        EnumSet<PotionEffectType> owned = kcmOwnedEffects.remove(player.getUniqueId());
        if (owned != null) {
            for (PotionEffectType type : owned) {
                player.removePotionEffect(type);
            }
        }
    }
}
