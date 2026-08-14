package me.koryov.portal;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class PortalSavedLocation {

    private final String name;
    private final String world;
    private final double x;
    private final double y;
    private final double z;

    public PortalSavedLocation(
            String name,
            String world,
            double x,
            double y,
            double z
    ) {
        this.name = name;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public String getName() {
        return name;
    }

    public String getWorld() {
        return world;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public Location toLocation() {

        World bukkitWorld =
                Bukkit.getWorld(world);

        if (bukkitWorld == null) {
            return null;
        }

        return new Location(
                bukkitWorld,
                x,
                y,
                z
        );
    }
}