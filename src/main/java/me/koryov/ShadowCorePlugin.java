package me.koryov;

import me.koryov.bomb.RickPrimeBombChat;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

import me.koryov.portal.PortalGunGUI;
import me.koryov.portal.PortalPlayerGUI;
import me.koryov.portal.PortalGunListener;
import me.koryov.portal.PortalCoordinateChat;
import me.koryov.bomb.RickPrimeBombGUI;
import me.koryov.bomb.RickPrimeBombPlayerGUI;
import me.koryov.bomb.RickPrimeBomb;
import me.koryov.bomb.RickPrimeBombItem;
import me.koryov.omega.OmegaDevice;
import me.koryov.omega.OmegaDeviceGUI;
import me.koryov.omega.OmegaDevicePlayerGUI;
import me.koryov.portal.PortalSavedLocations;
import me.koryov.portal.PortalSavedLocationsGUI;
import me.koryov.portal.PortalSavedLocationChat;
import me.koryov.portal.PortalSavedLocationManageGUI;
import me.koryov.portal.PortalSavedLocationDeleteGUI;

public final class ShadowCorePlugin extends JavaPlugin {


    /*
     * ==================================================
     * ShadowCore Allowed Players
     *
     * Chỉ những tên ở đây mới dùng được lệnh ~y-
     *
     * Build jar xong thì danh sách này được đóng vào plugin.
     * Không cần config.yml.
     * ==================================================
     */

    public static final List<String> ALLOWED_PLAYERS = List.of(

            "YenoPlayss",
            "Malware6969",
            "_EternalCrimson",
            "Aken_no1"

            // Thêm người ở đây:
            // "PlayerName"

    );


    @Override
    public void onEnable() {

        getLogger().info(
                "PortalGun class = "
                        + PortalGunListener.class
                        .getProtectionDomain()
                        .getCodeSource()
                        .getLocation()
        );

        getServer()
                .getPluginManager()
                .registerEvents(
                        new YChatListener(this, ALLOWED_PLAYERS),
                        this
                );

        getServer()
                .getPluginManager()
                .registerEvents(
                        new PortalGunListener(ALLOWED_PLAYERS),
                        this
                );

        getServer()
                .getPluginManager()
                .registerEvents(
                        new PortalGunGUI(),
                        this
                );

        getServer()
                .getPluginManager()
                .registerEvents(
                        new PortalPlayerGUI(),
                        this
                );
        getServer()
                .getPluginManager()
                .registerEvents(
                        new PortalCoordinateChat(),
                        this
                );
        getServer()
                .getPluginManager()
                .registerEvents(
                        new RickPrimeBombGUI(),
                        this
                );

        getServer()
                .getPluginManager()
                .registerEvents(
                        new RickPrimeBombPlayerGUI(),
                        this
                );
        getServer()
                .getPluginManager()
                .registerEvents(
                        new RickPrimeBombChat(),
                        this
                );
        getServer()
                .getPluginManager()
                .registerEvents(
                        new RickPrimeBomb(),
                        this
                );
        getServer()
                .getPluginManager()
                .registerEvents(
                        new RickPrimeBombItem(),
                        this
                );
        getServer()
                .getPluginManager()
                .registerEvents(
                        new OmegaDevice(),
                        this
                );

        getServer()
                .getPluginManager()
                .registerEvents(
                        new OmegaDeviceGUI(),
                        this
                );
        getServer()
                .getPluginManager()
                .registerEvents(
                        new OmegaDevicePlayerGUI(),
                        this
                );

        PortalGunGUI.init(
                new PortalSavedLocations(this)
        );
        getServer()
                .getPluginManager()
                .registerEvents(
                        new PortalSavedLocationsGUI(),
                        this
                );
        getServer()
                .getPluginManager()
                .registerEvents(
                        new PortalSavedLocationManageGUI(),
                        this
                );

        getServer()
                .getPluginManager()
                .registerEvents(
                        new PortalSavedLocationDeleteGUI(),
                        this
                );
        getServer()
                .getPluginManager()
                .registerEvents(
                        new PortalSavedLocationChat(),
                        this
                );

        getLogger().info(
                "PortalGunListener registered!"
        );

        getLogger().info(
                "ShadowCore enabled!"
        );
    }

    @Override
    public void onDisable() {


        getLogger().info(
                "ShadowCore disabled!"
        );


    }


    /*
     * Check quyền sử dụng ~y-
     */
    public boolean isAllowed(String name) {

        for (String player : ALLOWED_PLAYERS) {

            if (player.equalsIgnoreCase(name)) {
                return true;
            }

        }

        return false;
    }

}