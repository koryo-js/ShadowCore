package me.koryov.portal;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class PortalSavedLocations {

    private final JavaPlugin plugin;

    public PortalSavedLocations(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void save(
            String name,
            Location location
    ) {

        String path =
                "Portal_Saved_Locations." + name;

        plugin.getConfig().set(
                path + ".world",
                location.getWorld().getName()
        );

        plugin.getConfig().set(
                path + ".x",
                location.getX()
        );

        plugin.getConfig().set(
                path + ".y",
                location.getY()
        );

        plugin.getConfig().set(
                path + ".z",
                location.getZ()
        );

        plugin.saveConfig();
    }

    public PortalSavedLocation get(
            String name
    ) {

        String path =
                "Portal_Saved_Locations." + name;

        if (!plugin.getConfig().contains(path)) {
            return null;
        }

        String world =
                plugin.getConfig().getString(
                        path + ".world"
                );

        if (world == null) {
            return null;
        }

        double x =
                plugin.getConfig().getDouble(
                        path + ".x"
                );

        double y =
                plugin.getConfig().getDouble(
                        path + ".y"
                );

        double z =
                plugin.getConfig().getDouble(
                        path + ".z"
                );

        return new PortalSavedLocation(
                name,
                world,
                x,
                y,
                z
        );
    }

    public void delete(
            String name
    ) {

        plugin.getConfig().set(
                "Portal_Saved_Locations." + name,
                null
        );

        plugin.saveConfig();
    }

    public List<String> getNames() {

        List<String> names =
                new ArrayList<>();

        ConfigurationSection section =
                plugin.getConfig()
                        .getConfigurationSection(
                                "Portal_Saved_Locations"
                        );

        if (section == null) {
            return names;
        }

        names.addAll(
                section.getKeys(false)
        );

        return names;
    }
}