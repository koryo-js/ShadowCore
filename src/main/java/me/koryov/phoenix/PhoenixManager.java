package me.koryov.phoenix;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public final class PhoenixManager {

    private final JavaPlugin plugin;
    private final NamespacedKey worldKey;
    private final NamespacedKey xKey;
    private final NamespacedKey yKey;
    private final NamespacedKey zKey;

    public PhoenixManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.worldKey = new NamespacedKey(plugin, "phoenix_chamber_world");
        this.xKey = new NamespacedKey(plugin, "phoenix_chamber_x");
        this.yKey = new NamespacedKey(plugin, "phoenix_chamber_y");
        this.zKey = new NamespacedKey(plugin, "phoenix_chamber_z");
    }

    /**
     * Registers a personal chamber at the player's current position and builds it.
     */
    public boolean setChamber(Player player) {
        Location feet = player.getLocation();
        World world = feet.getWorld();
        if (world == null) {
            return false;
        }

        Location spawn = new Location(
                world,
                feet.getBlockX() + 0.5,
                feet.getBlockY(),
                feet.getBlockZ() + 0.5,
                0.0F,
                0.0F
        );

        // Load the destination chunk before constructing the chamber.
        world.getChunkAt(spawn);
        PhoenixChamber.build(spawn);

        PersistentDataContainer data = player.getPersistentDataContainer();
        data.set(worldKey, PersistentDataType.STRING, world.getUID().toString());
        data.set(xKey, PersistentDataType.DOUBLE, spawn.getX());
        data.set(yKey, PersistentDataType.DOUBLE, spawn.getY());
        data.set(zKey, PersistentDataType.DOUBLE, spawn.getZ());

        return true;
    }

    public void removeChamber(Player player) {
        PersistentDataContainer data = player.getPersistentDataContainer();
        data.remove(worldKey);
        data.remove(xKey);
        data.remove(yKey);
        data.remove(zKey);
    }

    public Location getChamber(Player player) {
        PersistentDataContainer data = player.getPersistentDataContainer();
        String worldId = data.get(worldKey, PersistentDataType.STRING);
        Double x = data.get(xKey, PersistentDataType.DOUBLE);
        Double y = data.get(yKey, PersistentDataType.DOUBLE);
        Double z = data.get(zKey, PersistentDataType.DOUBLE);

        if (worldId == null || x == null || y == null || z == null) {
            return null;
        }

        try {
            World world = Bukkit.getWorld(UUID.fromString(worldId));
            if (world == null) {
                return null;
            }
            Location location = new Location(world, x, y, z, 0.0F, 0.0F);
            world.getChunkAt(location);
            return location;
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    public boolean hasChamber(Player player) {
        return getChamber(player) != null;
    }

    public void prepare(Player player) {
        Location chamber = getChamber(player);
        if (chamber != null) {
            PhoenixChamber.close(chamber);
        }
    }

    public void finish(Player player) {
        Location chamber = getChamber(player);
        if (chamber == null) {
            return;
        }

        PhoenixChamber.open(chamber);
        Location exit = PhoenixChamber.getExit(chamber);
        player.teleport(exit);
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }
}
