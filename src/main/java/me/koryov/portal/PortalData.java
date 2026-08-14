package me.koryov.portal;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class PortalData {

    private final Location location;
    private final Vector forward;

    private boolean active = true;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public PortalData(
            Location location,
            Vector forward
    ) {

        this.location = location.clone();
        this.forward = forward.clone().normalize();

    }

    public Location getLocation() {

        return location.clone();

    }

    public Vector getForward() {

        return forward.clone();

    }
    private float scale = 0F;

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

}