package me.koryov.hiraishin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
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
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class HiraishinManager {

    private final JavaPlugin plugin;

    private final NamespacedKey markEntityKey;
    private final NamespacedKey ownerKey;
    private final NamespacedKey markWorldKey;
    private final NamespacedKey markXKey;
    private final NamespacedKey markYKey;
    private final NamespacedKey markZKey;

    public HiraishinManager(JavaPlugin plugin) {
        this.plugin = plugin;
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

    public boolean isKunai(org.bukkit.inventory.ItemStack item) {
        return HiraishinKunai.isKunai(plugin, item);
    }

    public void registerThrownKunai(Player owner, Trident trident) {
        removePreviousKunai(owner);

        PersistentDataContainer projectileData = trident.getPersistentDataContainer();
        projectileData.set(markEntityKey, PersistentDataType.BYTE, (byte) 1);
        projectileData.set(ownerKey, PersistentDataType.STRING, owner.getUniqueId().toString());

        trident.setPersistent(true);
        trident.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
        trident.setLifetimeTicks(Integer.MAX_VALUE);

        saveMark(owner, trident.getUniqueId(), trident.getLocation());
        owner.sendMessage("§6[Phi Lôi Thần] §eKunai đã được đánh dấu.");
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
                saveMark(owner, trident.getUniqueId(), trident.getLocation());
            }
        } catch (IllegalArgumentException ignored) {
            // Ignore malformed owner data.
        }
    }

    public void teleportToKunai(Player player) {
        PersistentDataContainer data = player.getPersistentDataContainer();
        String entityId = data.get(markEntityKey, PersistentDataType.STRING);

        if (entityId == null) {
            player.sendMessage("§6[Phi Lôi Thần] §cBạn chưa ném Phi Lôi Thần Kunai.");
            return;
        }

        Location markLocation = null;

        try {
            Entity entity = Bukkit.getEntity(UUID.fromString(entityId));
            if (entity instanceof Trident trident && trident.isValid()) {
                String ownerId = trident.getPersistentDataContainer().get(
                        ownerKey,
                        PersistentDataType.STRING
                );

                if (player.getUniqueId().toString().equals(ownerId)) {
                    markLocation = trident.getLocation().clone();
                }
            }
        } catch (IllegalArgumentException ignored) {
            // Fall back to the stored location below.
        }

        if (markLocation == null) {
            markLocation = readStoredLocation(data);
        }

        if (markLocation == null || markLocation.getWorld() == null) {
            clearMark(player);
            player.sendMessage("§6[Phi Lôi Thần] §cDấu ấn không còn hợp lệ.");
            return;
        }

        World world = markLocation.getWorld();
        int chunkX = markLocation.getBlockX() >> 4;
        int chunkZ = markLocation.getBlockZ() >> 4;

        // Load only the single destination chunk when the mark is currently unloaded.
        if (!world.isChunkLoaded(chunkX, chunkZ)) {
            world.getChunkAt(chunkX, chunkZ);
        }

        Location destination = findSafeLocation(markLocation, player);
        if (destination == null) {
            player.sendMessage("§6[Phi Lôi Thần] §cKhông tìm thấy vị trí an toàn cạnh kunai.");
            return;
        }

        Location origin = player.getLocation().clone();
        destination.setYaw(player.getLocation().getYaw());
        destination.setPitch(player.getLocation().getPitch());

        playTeleportEffect(origin);
        boolean success = player.teleport(destination, PlayerTeleportEvent.TeleportCause.PLUGIN);

        if (success) {
            player.setFallDistance(0.0F);
            playTeleportEffect(destination);
        }
    }

    public boolean hasMark(Player player) {
        return player.getPersistentDataContainer().has(
                markEntityKey,
                PersistentDataType.STRING
        );
    }

    private void removePreviousKunai(Player player) {
        String oldId = player.getPersistentDataContainer().get(
                markEntityKey,
                PersistentDataType.STRING
        );

        if (oldId != null) {
            try {
                Entity oldEntity = Bukkit.getEntity(UUID.fromString(oldId));
                if (oldEntity instanceof Trident) {
                    oldEntity.remove();
                }
            } catch (IllegalArgumentException ignored) {
                // The previous projectile may already have been unloaded or removed.
            }
        }

        clearMark(player);
    }

    private void saveMark(Player player, UUID entityId, Location location) {
        PersistentDataContainer data = player.getPersistentDataContainer();

        data.set(markEntityKey, PersistentDataType.STRING, entityId.toString());
        data.set(markWorldKey, PersistentDataType.STRING, location.getWorld().getUID().toString());
        data.set(markXKey, PersistentDataType.DOUBLE, location.getX());
        data.set(markYKey, PersistentDataType.DOUBLE, location.getY());
        data.set(markZKey, PersistentDataType.DOUBLE, location.getZ());
    }

    private Location readStoredLocation(PersistentDataContainer data) {
        String worldId = data.get(markWorldKey, PersistentDataType.STRING);
        Double x = data.get(markXKey, PersistentDataType.DOUBLE);
        Double y = data.get(markYKey, PersistentDataType.DOUBLE);
        Double z = data.get(markZKey, PersistentDataType.DOUBLE);

        if (worldId == null || x == null || y == null || z == null) {
            return null;
        }

        try {
            World world = Bukkit.getWorld(UUID.fromString(worldId));
            return world == null ? null : new Location(world, x, y, z);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private void clearMark(Player player) {
        PersistentDataContainer data = player.getPersistentDataContainer();
        data.remove(markEntityKey);
        data.remove(markWorldKey);
        data.remove(markXKey);
        data.remove(markYKey);
        data.remove(markZKey);
    }

    private Location findSafeLocation(Location target, Player player) {
        World world = target.getWorld();
        if (world == null) {
            return null;
        }

        int baseX = target.getBlockX();
        int baseY = target.getBlockY();
        int baseZ = target.getBlockZ();

        // Prefer a solid floor with two passable blocks for the player's body.
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

        // A kunai can still be mid-air. Allow an air destination if the player's body fits.
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
        world.spawnParticle(Particle.PORTAL, center, 45, 0.35, 0.65, 0.35, 0.12);
        world.spawnParticle(Particle.END_ROD, center, 12, 0.25, 0.45, 0.25, 0.015);
        world.playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 0.8F, 1.35F);
    }
}
