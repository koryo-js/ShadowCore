package me.koryov.jutsu;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.*;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;

public final class JutsuManager {
    final JavaPlugin plugin;
    private final NamespacedKey jikukanKey;
    private final NamespacedKey kcmSpeedKey;
    private final Set<UUID> activeJikukan = new HashSet<>();
    private final Map<UUID, Location> lastAttackLocations = new HashMap<>();
    private final Set<UUID> activeKcm = new HashSet<>();
    private final Map<UUID, Set<PotionEffectType>> kcmOwnedEffects = new HashMap<>();

    private static final double PORTAL_RADIUS = 1.35;

    public JutsuManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.jikukanKey = new NamespacedKey(plugin, "jikukan_anchor");
        this.kcmSpeedKey = new NamespacedKey(plugin, "kcm_speed");
    }

    public void start() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                refreshKcm(player);
                if (activeJikukan.contains(player.getUniqueId())) {
                    pulseJikukan(player);
                }
            }

            Set<UUID> seenAttacks = new HashSet<>();
            for (World world : Bukkit.getWorlds()) {
                for (Entity entity : world.getEntities()) {
                    if (isAttackObject(entity) && entity.isValid() && !entity.isDead()) {
                        seenAttacks.add(entity.getUniqueId());
                        lastAttackLocations.put(entity.getUniqueId(), entity.getLocation().clone());
                    }
                }
            }
            lastAttackLocations.keySet().removeIf(id -> !seenAttacks.contains(id));
        }, 1L, 1L);
    }

    public void castRasengan(Player player) {
        if (!JutsuItems.isChestplate(plugin, player.getInventory().getChestplate())) return;

        Location start = player.getEyeLocation().clone()
                .add(player.getLocation().getDirection().normalize().multiply(0.7));
        Vector direction = player.getLocation().getDirection().normalize();
        World world = player.getWorld();

        player.swingMainHand();
        world.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_STRONG, 1.0f, 1.6f);
        world.spawnParticle(Particle.END_ROD, start, 18, .18, .18, .18, .03);

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

    /**
     * Shift + right-click stores the seal at the player's current position.
     * The location and facing direction are persisted on the player.
     */
    public void placeJikukan(Player player) {
        if (!JutsuItems.isPattern(plugin, player.getInventory().getItemInMainHand())) return;

        activeJikukan.remove(player.getUniqueId());
        Location anchor = player.getLocation().clone();
        String encoded = anchor.getWorld().getUID() + ","
                + anchor.getX() + "," + anchor.getY() + "," + anchor.getZ() + ","
                + anchor.getYaw() + "," + anchor.getPitch();

        player.getPersistentDataContainer().set(jikukanKey, PersistentDataType.STRING, encoded);

        Location center = portalCenter(anchor);
        anchor.getWorld().playSound(center, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, .8f, 1.4f);
        anchor.getWorld().spawnParticle(Particle.PORTAL, center, 55, .7, .8, .7, .25);
        drawPortalRing(anchor);

        player.sendActionBar("§5Jikūkan Pattern §7— đã đặt kết ấn tại vị trí này");
    }

    /**
     * Right-click toggles the portal. A Hiraishin Kunai is required as its exit.
     */
    public void activateJikukan(Player player) {
        if (!JutsuItems.isPattern(plugin, player.getInventory().getItemInMainHand())) return;

        if (activeJikukan.remove(player.getUniqueId())) {
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, .8f, 1.2f);
            player.sendActionBar("§5Jikūkan Pattern §7— cổng đã tắt");
            return;
        }

        Location anchor = loadJikukanAnchor(player);
        if (anchor == null) {
            player.sendActionBar("§5Jikūkan Pattern §7— Shift + chuột phải để đặt kết ấn trước");
            return;
        }

        if (findHiraishinTarget(player) == null) {
            player.sendActionBar("§5Jikūkan Pattern §7— cần ném Hiraishin Kunai để tạo lối ra");
            return;
        }


        activeJikukan.add(player.getUniqueId());
        anchor.getWorld().playSound(portalCenter(anchor), Sound.BLOCK_BEACON_ACTIVATE, .9f, 1.5f);
        player.sendActionBar("§5Jikūkan Pattern §7— cổng đã mở, projectile sẽ đi qua Kunai");
    }
    public void teleportJikukan(Player player) {
        org.bukkit.inventory.ItemStack item = player.getInventory().getItemInMainHand();
        if (!JutsuItems.isPattern(plugin, item) || item.getItemMeta() == null) return;

        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        org.bukkit.persistence.PersistentDataContainer pdc = meta.getPersistentDataContainer();

        // Nếu item chưa lưu tọa độ -> Lưu tọa độ hiện tại làm điểm neo không gian
        if (!pdc.has(jikukanKey, org.bukkit.persistence.PersistentDataType.STRING)) {
            Location loc = player.getLocation();
            String serialized = loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch();
            pdc.set(jikukanKey, org.bukkit.persistence.PersistentDataType.STRING, serialized);
            item.setItemMeta(meta);

            player.getWorld().playSound(loc, Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.8f);
            player.getWorld().spawnParticle(Particle.REVERSE_PORTAL, loc.clone().add(0, 1, 0), 30, 0.4, 0.8, 0.4, 0.05);
            player.sendActionBar("§5Jikūkan §7— đã khắc ấn không gian tại vị trí này");
            return;
        }

        // Nếu đã có tọa độ -> Dịch chuyển tức thời tới đó
        String raw = pdc.get(jikukanKey, org.bukkit.persistence.PersistentDataType.STRING);
        if (raw == null) return;

        String[] parts = raw.split(",");
        World targetWorld = Bukkit.getWorld(parts[0]);
        if (targetWorld == null) {
            player.sendActionBar("§cThế giới của ấn không gian không tồn tại!");
            return;
        }

        Location dest = new Location(
                targetWorld,
                Double.parseDouble(parts[1]),
                Double.parseDouble(parts[2]),
                Double.parseDouble(parts[3]),
                Float.parseFloat(parts[4]),
                Float.parseFloat(parts[5])
        );

        // Hiệu ứng tại điểm đi và điểm đến
        player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation().add(0, 1, 0), 40, 0.5, 1.0, 0.5, 0.2);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.3f);

        player.teleport(dest);

        targetWorld.spawnParticle(Particle.PORTAL, dest.clone().add(0, 1, 0), 40, 0.5, 1.0, 0.5, 0.2);
        targetWorld.playSound(dest, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.3f);
        player.sendActionBar("§dJikūkan Idō §7— đã dịch chuyển không gian!");
    }


    /**
     * Detects a projectile crossing the fixed portal plane from its front side.
     * Its velocity is captured before teleport and restored unchanged afterward.
     */
    private void pulseJikukan(Player owner) {
        Location anchor = loadJikukanAnchor(owner);
        Location target = findHiraishinTarget(owner);
        if (anchor == null || target == null) {
            activeJikukan.remove(owner.getUniqueId());
            owner.sendActionBar("§5Jikūkan Pattern §7— mất kết nối với Hiraishin Kunai");
            return;
        }

        Vector normal = anchor.getDirection().normalize();
        Location center = portalCenter(anchor);
        drawPortalRing(anchor);

        World world = anchor.getWorld();
        if (world == null) return;

        for (Entity entity : world.getNearbyEntities(center, 2.6, 2.6, 2.6)) {
            if (!isAttackObject(entity) || !entity.isValid()) continue;
            if (entity instanceof Trident trident && isHiraishinKunai(trident)) continue;

            Location previous = lastAttackLocations.get(entity.getUniqueId());
            Location current = entity.getLocation().clone();
            if (previous == null || previous.getWorld() == null
                    || !previous.getWorld().equals(world)) continue;

            if (!crossesPortalPlane(previous, current, center, normal)) continue;

            Vector velocity = entity.getVelocity().clone();
            if (velocity.lengthSquared() < 0.0001) continue;

            Location exit = target.clone().add(velocity.clone().normalize().multiply(0.8));
            exit.setYaw(current.getYaw());
            exit.setPitch(current.getPitch());

            if (entity.teleport(exit)) {
                entity.setVelocity(velocity);
                World exitWorld = exit.getWorld();
                if (exitWorld != null) {
                    exitWorld.spawnParticle(Particle.PORTAL, exit, 28, .35, .45, .35, .15);
                    exitWorld.spawnParticle(Particle.END_ROD, exit, 8, .2, .25, .2, .02);
                    exitWorld.playSound(exit, Sound.ENTITY_ENDERMAN_TELEPORT, .6f, 1.6f);
                }
                world.spawnParticle(Particle.PORTAL, center, 20, .35, .45, .35, .12);
                world.playSound(center, Sound.BLOCK_BEACON_POWER_SELECT, .45f, 1.8f);
            }
        }
    }

    /**
     * Jikukan catches physical attack entities, not particle-only effects.
     * Projectile covers arrows, tridents, fireballs, potions, snowballs,
     * eggs, pearls, wind charges and similar vanilla projectile entities.
     * Primed TNT, fireworks and moving falling blocks are also physical attacks.
     */
    public void trackAttackEntity(Entity entity) {
        if (isAttackObject(entity)) {
            lastAttackLocations.put(entity.getUniqueId(), entity.getLocation().clone());
        }
    }

    private boolean isAttackObject(Entity entity) {
        return entity instanceof Projectile
                || entity instanceof TNTPrimed
                || entity instanceof Firework
                || entity instanceof FallingBlock;
    }

    private boolean crossesPortalPlane(Location previous, Location current,
                                       Location center, Vector normal) {
        Vector from = previous.toVector().subtract(center.toVector());
        Vector to = current.toVector().subtract(center.toVector());
        double fromSide = from.dot(normal);
        double toSide = to.dot(normal);

        // Only enter from the side the seal faces.
        if (fromSide <= 0.0 || toSide > 0.0) return false;

        double denominator = fromSide - toSide;
        if (denominator <= 0.000001) return false;
        double progress = fromSide / denominator;

        Vector crossing = previous.toVector().add(
                current.toVector().subtract(previous.toVector()).multiply(progress)
        );
        Vector offset = crossing.subtract(center.toVector());
        Vector radial = offset.subtract(normal.clone().multiply(offset.dot(normal)));
        return radial.lengthSquared() <= PORTAL_RADIUS * PORTAL_RADIUS;
    }

    private Location portalCenter(Location anchor) {
        return anchor.clone().add(0, 1.0, 0);
    }

    private void drawPortalRing(Location anchor) {
        World world = anchor.getWorld();
        if (world == null) return;

        Location center = portalCenter(anchor);
        Vector normal = anchor.getDirection().normalize();
        Vector up = new Vector(0, 1, 0);
        Vector right = normal.clone().crossProduct(up);
        if (right.lengthSquared() < 0.0001) right = new Vector(1, 0, 0);
        right.normalize();

        for (int i = 0; i < 28; i++) {
            double angle = (Math.PI * 2.0 * i) / 28.0;
            Vector point = right.clone().multiply(Math.cos(angle) * PORTAL_RADIUS)
                    .add(up.clone().multiply(Math.sin(angle) * PORTAL_RADIUS));
            world.spawnParticle(Particle.PORTAL, center.clone().add(point), 1, 0, 0, 0, 0);
        }
        world.spawnParticle(Particle.END_ROD, center, 3, .25, .35, .25, .01);
    }

    private boolean isHiraishinKunai(Trident trident) {
        return trident.getPersistentDataContainer().has(
                new NamespacedKey(plugin, "hiraishin_mark_entity"),
                PersistentDataType.BYTE
        );
    }

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
                // Skip an invalid mark and continue checking older marks.
            }
        }
        return null;
    }

    private Location loadJikukanAnchor(Player player) {
        String raw = player.getPersistentDataContainer().get(jikukanKey, PersistentDataType.STRING);
        if (raw == null || raw.isBlank()) return null;

        String[] fields = raw.split(",");
        if (fields.length != 6) return null;

        try {
            World world = Bukkit.getWorld(UUID.fromString(fields[0]));
            if (world == null) return null;
            return new Location(world,
                    Double.parseDouble(fields[1]),
                    Double.parseDouble(fields[2]),
                    Double.parseDouble(fields[3]),
                    Float.parseFloat(fields[4]),
                    Float.parseFloat(fields[5]));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    public void onProjectileHit(Projectile projectile) {
        // The fixed portal plane is checked every tick; no extra hit-event state is needed.
    }

    public void refreshKcm(Player player) {
        applyKcmArmorColors(player);
        boolean wearing = JutsuItems.isChestplate(plugin, player.getInventory().getChestplate())
                && JutsuItems.isHokageHat(plugin, player.getInventory().getHelmet());
        if (wearing) {
            activeKcm.add(player.getUniqueId());
            Set<PotionEffectType> owned = kcmOwnedEffects.computeIfAbsent(
                    player.getUniqueId(), ignored -> new HashSet<>()
            );

            addKcmEffect(player, PotionEffectType.SPEED, 1, owned);
            addKcmEffect(player, PotionEffectType.STRENGTH, 1, owned);
            addKcmEffect(player, PotionEffectType.JUMP_BOOST, 1, owned);
            addKcmEffect(player, PotionEffectType.RESISTANCE, 0, owned);

            AttributeInstance speed = player.getAttribute(Attribute.MOVEMENT_SPEED);
            if (speed != null && speed.getModifier(kcmSpeedKey) == null) {
                speed.addModifier(new AttributeModifier(
                        kcmSpeedKey, 0.18,
                        AttributeModifier.Operation.ADD_SCALAR
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

    private void applyKcmArmorColors(Player player) {
        org.bukkit.inventory.ItemStack chest = player.getInventory().getChestplate();
        if (JutsuItems.isChestplate(plugin, chest)
                && chest.getItemMeta() instanceof org.bukkit.inventory.meta.LeatherArmorMeta meta
                && !Color.fromRGB(255, 176, 32).equals(meta.getColor())) {
            meta.setColor(Color.fromRGB(255, 176, 32));
            chest.setItemMeta(meta);
        }

        org.bukkit.inventory.ItemStack helmet = player.getInventory().getHelmet();
        if (JutsuItems.isHokageHat(plugin, helmet)
                && helmet.getItemMeta() instanceof org.bukkit.inventory.meta.LeatherArmorMeta meta
                && !Color.fromRGB(190, 35, 35).equals(meta.getColor())) {
            meta.setColor(Color.fromRGB(190, 35, 35));
            helmet.setItemMeta(meta);
        }
    }

    private void addKcmEffect(Player player, PotionEffectType type, int amplifier,
                              Set<PotionEffectType> owned) {
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
        if (speed != null && speed.getModifier(kcmSpeedKey) != null) {
            speed.removeModifier(kcmSpeedKey);
        }
        Set<PotionEffectType> owned = kcmOwnedEffects.remove(player.getUniqueId());
        if (owned != null) {
            for (PotionEffectType type : owned) {
                player.removePotionEffect(type);
            }
        }
    }
}
