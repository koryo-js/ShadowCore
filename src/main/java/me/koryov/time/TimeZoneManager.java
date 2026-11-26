package me.koryov.time;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class TimeZoneManager {

    public static final double RADIUS = 10.0;

    private final JavaPlugin plugin;
    private final Set<UUID> activeOwners = new HashSet<>();
    private final Map<UUID, FrozenState> frozen = new HashMap<>();
    private TimeZoneTask task;

    public TimeZoneManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean toggle(Player owner) {
        UUID id = owner.getUniqueId();

        if (activeOwners.remove(id)) {
            if (activeOwners.isEmpty()) {
                restoreAll();
                if (task != null) {
                    task.cancel();
                    task = null;
                }
            }
            return false;
        }

        activeOwners.add(id);
        if (task == null) {
            task = new TimeZoneTask(this);
            task.runTaskTimer(plugin, 1L, 1L);
        }
        return true;
    }

    public boolean isActive(Player owner) {
        return activeOwners.contains(owner.getUniqueId());
    }

    public boolean isFrozen(Player player) {
        FrozenState state = frozen.get(player.getUniqueId());
        return state != null && state.entity == player;
    }

    void tick() {
        Set<UUID> seen = new HashSet<>();

        for (UUID ownerId : Set.copyOf(activeOwners)) {
            Player owner = Bukkit.getPlayer(ownerId);
            if (owner == null || !owner.isOnline()) {
                activeOwners.remove(ownerId);
                continue;
            }

            Location center = owner.getLocation();
            double radiusSquared = RADIUS * RADIUS;

            for (Entity entity : owner.getWorld().getNearbyEntities(center, RADIUS, RADIUS, RADIUS)) {
                if (entity == owner || entity instanceof Player player
                        && activeOwners.contains(player.getUniqueId())) {
                    continue;
                }
                if (entity.getLocation().distanceSquared(center) > radiusSquared) {
                    continue;
                }
                if (!(entity instanceof LivingEntity || entity instanceof Projectile)) {
                    continue;
                }

                UUID entityId = entity.getUniqueId();
                seen.add(entityId);
                freeze(entity);
            }
        }

        for (UUID entityId : Set.copyOf(frozen.keySet())) {
            if (!seen.contains(entityId)) {
                restore(entityId);
            }
        }

        if (activeOwners.isEmpty() && task != null) {
            task.cancel();
            task = null;
        }
    }

    private void freeze(Entity entity) {
        FrozenState state = frozen.computeIfAbsent(entity.getUniqueId(), ignored -> new FrozenState(entity));
        entity.setVelocity(new Vector());

        if (entity instanceof LivingEntity living && !(living instanceof Player)) {
            living.setAI(false);
        }
    }

    private void restore(UUID entityId) {
        FrozenState state = frozen.remove(entityId);
        if (state == null || !state.entity.isValid()) {
            return;
        }

        Entity entity = state.entity;
        entity.setVelocity(state.velocity);

        if (entity instanceof LivingEntity living && !(living instanceof Player)
                && state.ai != null) {
            living.setAI(state.ai);
        }
    }

    private void restoreAll() {
        for (UUID entityId : Set.copyOf(frozen.keySet())) {
            restore(entityId);
        }
    }

    public void stop() {
        activeOwners.clear();
        restoreAll();
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private static final class FrozenState {
        private final Entity entity;
        private final Vector velocity;
        private final Boolean ai;

        private FrozenState(Entity entity) {
            this.entity = entity;
            this.velocity = entity.getVelocity().clone();
            this.ai = entity instanceof LivingEntity living && !(living instanceof Player)
                    ? living.hasAI() : null;
        }
    }
}
