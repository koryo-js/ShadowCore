package me.koryov.portal;

import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PortalManager {

    private static final Map<UUID, PortalPair> portals =
            new HashMap<>();

    public static void setPortal(
            UUID uuid,
            PortalPair portal
    ) {

        portals.put(
                uuid,
                portal
        );

    }

    public static PortalPair getPortal(UUID uuid) {

        return portals.get(uuid);

    }

    public static void removePortal(UUID uuid) {

        portals.remove(uuid);

    }

    public static Map<UUID,PortalPair> getPortals() {

        return portals;

    }

}