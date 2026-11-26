package me.koryov.hiraishin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class HiraishinManager {

    private final JavaPlugin plugin;

    private final NamespacedKey marksKey;
    private final NamespacedKey markEntityKey;
    private final NamespacedKey ownerKey;
    private final NamespacedKey markWorldKey;
    private final NamespacedKey markXKey;
    private final NamespacedKey markYKey;
    private final NamespacedKey markZKey;

    private static final String MARK_SEPARATOR = ";";
    private static final String FIELD_SEPARATOR = ",";

    public HiraishinManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.marksKey = new NamespacedKey(plugin, "hiraishin_marks");
        this.markEntityKey = new NamespacedKey(plugin, "hiraishin_mark_entity");
        this.ownerKey = new NamespacedKey(plugin, "hiraishin_owner");
        this.markWorldKey = new NamespacedKey(plugin, "hiraishin_world");
        this.markXKey = new NamespacedKey(plugin, "hiraishin_x");
        this.markYKey = new NamespacedKey(plugin, "hiraishin_y");
        this.markZKey = new NamespacedKey(plugin, "hiraishin_z");
    }

    public void giveKunai(Player player) {
        var leftovers = player.getInventory().addItem(HiraishinKunai.create(plugin));
        leftovers.values().forEach(item ->
                player.getWorld().dropItemNaturally(player.getLocation(), item)
        );
        player.sendMessage("§6[ShadowCore] §eBạn đã nhận Phi Lôi Thần Kunai.");
    }

    public boolean isKunai(ItemStack item) {
        return HiraishinKunai.isKunai(plugin, item);
    }

    /**
     * Called when a special trident is launched.
     *
     * The inventory item is consumed by the listener. The thrown trident itself
     * becomes one independent Hiraishin mark.
     */
    public void registerThrownKunai(Player owner, Trident trident) {
        PersistentDataContainer projectileData = trident.getPersistentDataContainer();
        projectileData.set(markEntityKey, PersistentDataType.BYTE, (byte) 1);
        projectileData.set(ownerKey, PersistentDataType.STRING, owner.getUniqueId().toString());

        trident.setPersistent(true);
        trident.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
        trident.setLifetimeTicks(Integer.MAX_VALUE);

        HiraishinMark mark = new HiraishinMark(
                trident.getUniqueId(),
                trident.getLocation().clone()
        );

        List<HiraishinMark> marks = loadMarks(owner);
        marks.add(mark);
        saveMarks(owner, marks);

    }

    public void updateMarkFromProjectile(Trident trident) {
        PersistentDataContainer data = trident.getPersistentDataContainer();

        if (!data.has(markEntityKey, PersistentDataType.BYTE)) {
            return;
        }

        String ownerId = data.get(ownerKey, PersistentDataType.STRING);
        if (ownerId == null) {
            return;
        }

        try {
            Player owner = Bukkit.getPlayer(UUID.fromString(ownerId));
            if (owner != null) {
                List<HiraishinMark> marks = loadMarks(owner);
                for (HiraishinMark mark : marks) {
                    if (mark.entityId.equals(trident.getUniqueId())) {
                        mark.location = trident.getLocation().clone();
                        saveMarks(owner, marks);
                        return;
                    }
                }
            }
        } catch (IllegalArgumentException ignored) {
            // Ignore malformed owner data.
        }
    }

    public void teleportToKunai(Player player) {
        List<HiraishinMark> marks = loadMarks(player);

        if (marks.isEmpty()) {
            return;
        }

        HiraishinMark selected = selectMark(player, marks);

        if (selected == null) {
            return;
        }

        Location markLocation = getLiveMarkLocation(selected);

        if (markLocation == null || markLocation.getWorld() == null) {
            marks.remove(selected);
            saveMarks(player, marks);
            return;
        }

        selected.location = markLocation.clone();

        World world = markLocation.getWorld();
        int chunkX = markLocation.getBlockX() >> 4;
        int chunkZ = markLocation.getBlockZ() >> 4;

        if (!world.isChunkLoaded(chunkX, chunkZ)) {
            world.getChunkAt(chunkX, chunkZ);
        }

        Location destination = findSafeLocation(markLocation, player);

        if (destination == null) {
            // Recover a landed kunai if no safe teleport position exists.
            if (isMarkOnGround(selected)) {
                removeProjectile(selected);
                marks.remove(selected);
                saveMarks(player, marks);
                giveKunaiBack(player);
            }
            return;
        }

        Location origin = player.getLocation().clone();
        destination.setYaw(player.getLocation().getYaw());
        destination.setPitch(player.getLocation().getPitch());

        playTeleportEffect(origin);

        boolean success = player.teleport(
                destination,
                PlayerTeleportEvent.TeleportCause.PLUGIN
        );

        if (!success) {
            return;
        }

        player.setFallDistance(0.0F);
        playTeleportEffect(destination);

        // Successful teleport consumes the selected mark and retrieves its kunai.
        removeProjectile(selected);
        marks.remove(selected);
        saveMarks(player, marks);
        giveKunaiBack(player);
    }

    public boolean hasMark(Player player) {
        return !loadMarks(player).isEmpty();
    }

    private HiraishinMark selectMark(Player player, List<HiraishinMark> marks) {
        Location eye = player.getEyeLocation();
        Vector look = eye.getDirection().normalize();

        List<HiraishinMark> sameWorld = new ArrayList<>();

        for (HiraishinMark mark : marks) {
            Location location = getLiveMarkLocation(mark);
            if (location == null || location.getWorld() == null) {
                continue;
            }

            if (location.getWorld().equals(player.getWorld())) {
                sameWorld.add(mark);
            }
        }

        // First choice: the mark closest to the player's crosshair direction.
        HiraishinMark directional = sameWorld.stream()
                .filter(mark -> isInLookingDirection(eye, look, getLiveMarkLocation(mark)))
                .min(Comparator
                        .comparingDouble((HiraishinMark mark) ->
                                look.angle(eye.toVector().subtract(getLiveMarkLocation(mark).toVector()).multiply(-1)))
                        .thenComparingDouble(mark ->
                                eye.distanceSquared(getLiveMarkLocation(mark))))
                .orElse(null);

        if (directional != null) {
            return directional;
        }

        // If there is no mark inside the viewing cone, do not teleport somewhere
        // random in the same world.
        //
        // For another world there is no physical "looking direction", so use the
        // most recently created cross-world mark as the only sensible fallback.
        return marks.get(marks.size() - 1);
    }

    private boolean isInLookingDirection(Location eye, Vector look, Location target) {
        if (target == null || target.getWorld() == null
                || !target.getWorld().equals(eye.getWorld())) {
            return false;
        }

        Vector toTarget = target.toVector().subtract(eye.toVector());

        if (toTarget.lengthSquared() < 0.01) {
            return true;
        }

        double dot = look.dot(toTarget.normalize());

        // About 50 degrees from the crosshair.
        return dot >= 0.6428D;
    }

    private Location getLiveMarkLocation(HiraishinMark mark) {
        try {
            Entity entity = Bukkit.getEntity(mark.entityId);

            if (entity instanceof Trident trident && trident.isValid()) {
                String ownerId = trident.getPersistentDataContainer().get(
                        ownerKey,
                        PersistentDataType.STRING
                );

                if (ownerId != null) {
                    return trident.getLocation().clone();
                }
            }
        } catch (IllegalArgumentException ignored) {
            // Use the stored location.
        }

        return mark.location == null ? null : mark.location.clone();
    }

    private void removeProjectile(HiraishinMark mark) {
        try {
            Entity entity = Bukkit.getEntity(mark.entityId);
            if (entity != null) {
                entity.remove();
            }
        } catch (IllegalArgumentException ignored) {
            // Projectile may already be unloaded.
        }
    }

    private List<HiraishinMark> loadMarks(Player player) {
        String raw = player.getPersistentDataContainer().get(
                marksKey,
                PersistentDataType.STRING
        );

        List<HiraishinMark> marks = new ArrayList<>();

        if (raw == null || raw.isBlank()) {
            return marks;
        }

        for (String encoded : raw.split(MARK_SEPARATOR)) {
            String[] fields = encoded.split(FIELD_SEPARATOR);

            if (fields.length != 5) {
                continue;
            }

            try {
                UUID entityId = UUID.fromString(fields[0]);
                UUID worldId = UUID.fromString(fields[1]);
                double x = Double.parseDouble(fields[2]);
                double y = Double.parseDouble(fields[3]);
                double z = Double.parseDouble(fields[4]);

                World world = Bukkit.getWorld(worldId);
                if (world != null) {
                    marks.add(new HiraishinMark(
                            entityId,
                            new Location(world, x, y, z)
                    ));
                }
            } catch (IllegalArgumentException ignored) {
                // Ignore a malformed individual mark instead of losing all marks.
            }
        }

        return marks;
    }

    private void saveMarks(Player player, List<HiraishinMark> marks) {
        PersistentDataContainer data = player.getPersistentDataContainer();

        if (marks.isEmpty()) {
            data.remove(marksKey);
            return;
        }

        StringBuilder builder = new StringBuilder();

        for (HiraishinMark mark : marks) {
            if (mark.location == null || mark.location.getWorld() == null) {
                continue;
            }

            if (builder.length() > 0) {
                builder.append(MARK_SEPARATOR);
            }

            builder.append(mark.entityId)
                    .append(FIELD_SEPARATOR)
                    .append(mark.location.getWorld().getUID())
                    .append(FIELD_SEPARATOR)
                    .append(mark.location.getX())
                    .append(FIELD_SEPARATOR)
                    .append(mark.location.getY())
                    .append(FIELD_SEPARATOR)
                    .append(mark.location.getZ());
        }

        if (builder.length() == 0) {
            data.remove(marksKey);
        } else {
            data.set(marksKey, PersistentDataType.STRING, builder.toString());
        }
    }

    private void giveKunaiBack(Player player) {
        ItemStack item = HiraishinKunai.create(plugin);
        var leftovers = player.getInventory().addItem(item);

        leftovers.values().forEach(leftover ->
                player.getWorld().dropItemNaturally(player.getLocation(), leftover)
        );
    }

    private int countKunai(Player player) {
        int count = 0;

        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (isKunai(item)) {
                count += item.getAmount();
            }
        }

        if (isKunai(player.getInventory().getItemInOffHand())) {
            count += player.getInventory().getItemInOffHand().getAmount();
        }

        return count;
    }

    private boolean isMarkOnGround(HiraishinMark mark) {
        try {
            Entity entity = Bukkit.getEntity(mark.entityId);
            return entity instanceof Trident trident
                    && trident.isValid()
                    && trident.isOnGround();
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    private Location findSafeLocation(Location target, Player player) {
        World world = target.getWorld();
        if (world == null) {
            return null;
        }

        int baseX = target.getBlockX();
        int baseY = target.getBlockY();
        int baseZ = target.getBlockZ();

        for (int radius = 0; radius <= 2; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) {
                        continue;
                    }

                    for (int dy = -1; dy <= 2; dy++) {
                        Location candidate = new Location(
                                world,
                                baseX + dx + 0.5,
                                baseY + dy,
                                baseZ + dz + 0.5,
                                player.getLocation().getYaw(),
                                player.getLocation().getPitch()
                        );

                        Block feet = candidate.getBlock();
                        Block head = candidate.clone().add(0, 1, 0).getBlock();
                        Block floor = candidate.clone().add(0, -1, 0).getBlock();

                        if (feet.isPassable()
                                && head.isPassable()
                                && !feet.isLiquid()
                                && !head.isLiquid()
                                && floor.getType().isSolid()
                                && !floor.isLiquid()) {
                            return candidate;
                        }
                    }
                }
            }
        }

        Location airCandidate = target.clone().add(0, 0.2, 0);

        if (airCandidate.getBlock().isPassable()
                && airCandidate.clone().add(0, 1, 0).getBlock().isPassable()
                && !airCandidate.getBlock().isLiquid()
                && !airCandidate.clone().add(0, 1, 0).getBlock().isLiquid()) {
            return airCandidate;
        }

        return null;
    }

    private void playTeleportEffect(Location location) {
        World world = location.getWorld();

        if (world == null) {
            return;
        }

        Location center = location.clone().add(0, 0.9, 0);

        world.spawnParticle(
                Particle.PORTAL,
                center,
                45,
                0.35,
                0.65,
                0.35,
                0.12
        );

        world.spawnParticle(
                Particle.END_ROD,
                center,
                12,
                0.25,
                0.45,
                0.25,
                0.015
        );

        world.playSound(
                location,
                Sound.ENTITY_ENDERMAN_TELEPORT,
                0.8F,
                1.35F
        );
    }

    private static final class HiraishinMark {

        private final UUID entityId;
        private Location location;

        private HiraishinMark(UUID entityId, Location location) {
            this.entityId = entityId;
            this.location = location;
        }
    }
}
