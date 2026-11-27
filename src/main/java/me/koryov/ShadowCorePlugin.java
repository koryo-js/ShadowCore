package me.koryov;

import me.koryov.bomb.RickPrimeBombChat;
import me.koryov.hiraishin.HiraishinListener;
import me.koryov.hiraishin.HiraishinManager;
import me.koryov.phoenix.PhoenixManager;
import me.koryov.phoenix.PhoenixDeathListener;
import me.koryov.time.TimeZoneManager;
import me.koryov.time.TimeZoneListener;
import me.koryov.forcefield.ForcefieldListener;
import me.koryov.jutsu.JutsuManager;
import me.koryov.jutsu.JutsuListener;
import me.koryov.jutsu.JutsuEquipListener;
import me.koryov.jutsu.JutsuCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Set;
import java.util.Locale;
import java.security.SecureRandom;
import java.util.concurrent.ConcurrentHashMap;

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

    private HiraishinManager hiraishinManager;
    private PhoenixManager phoenixManager;
    private TimeZoneManager timeZoneManager;
    private JutsuManager jutsuManager;


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

    public static final List<String> ALLOWED_PLAYERS = new CopyOnWriteArrayList<>(List.of(

            "YenoPlayss",
            "Malware6969",
            "_EternalCrimson",
            "Aken_no1"

            // Thêm người ở đây:
            // "PlayerName"

    ));


    private static final Set<String> DISABLED_Y_ACCESS = ConcurrentHashMap.newKeySet();
    private static volatile String shadowAccessCode = generateAccessCode();

    private static String normalizeName(String name) {
        return name == null ? "" : name.toLowerCase(Locale.ROOT);
    }

    private static String generateAccessCode() {
        final String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder(8);
        for (int i = 0; i < 8; i++) code.append(alphabet.charAt(random.nextInt(alphabet.length())));
        return code.toString();
    }

    public boolean isRawAllowed(String name) {
        for (String allowed : ALLOWED_PLAYERS) if (allowed.equalsIgnoreCase(name)) return true;
        return false;
    }

    public boolean hasYAccess(String name) {
        return isRawAllowed(name) && !DISABLED_Y_ACCESS.contains(normalizeName(name));
    }

    public boolean setYAccess(String name, boolean enabled) {
        if (!isRawAllowed(name)) return false;
        if (enabled) DISABLED_Y_ACCESS.remove(normalizeName(name));
        else DISABLED_Y_ACCESS.add(normalizeName(name));
        return true;
    }

    public boolean redeemShadowCode(String name, String code) {
        if (code == null || !shadowAccessCode.equalsIgnoreCase(code.trim())) return false;
        if (!isRawAllowed(name)) ALLOWED_PLAYERS.add(name);
        DISABLED_Y_ACCESS.remove(normalizeName(name));
        return true;
    }

    public String issueShadowCode() {
        shadowAccessCode = generateAccessCode();
        return shadowAccessCode;
    }

    @Override
    public void onEnable() {

        hiraishinManager = new HiraishinManager(this);
        ShadowCoreUpdater.checkAsync(this);
        phoenixManager = new PhoenixManager(this);
        timeZoneManager = new TimeZoneManager(this);
        jutsuManager = new JutsuManager(this);
        jutsuManager.start();

        getServer()
                .getPluginManager()
                .registerEvents(
                        new YChatListener(this, ALLOWED_PLAYERS),
                        this
                );

        getServer()
                .getPluginManager()
                .registerEvents(
                        new HiraishinListener(hiraishinManager),
                        this
                );

        getServer().getPluginManager().registerEvents(
                new PhoenixDeathListener(phoenixManager), this
        );

        getServer().getPluginManager().registerEvents(
                new TimeZoneListener(timeZoneManager), this
        );
        getServer().getPluginManager().registerEvents(
                new ForcefieldListener(this), this
        );
        getServer().getPluginManager().registerEvents(new JutsuListener(jutsuManager), this);
        getServer().getPluginManager().registerEvents(new JutsuEquipListener(jutsuManager), this);
        if (getCommand("shadowjutsu") != null) {
            getCommand("shadowjutsu").setExecutor(new JutsuCommand(this));
        }

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
                "ShadowCore enabled!"
        );
    }

    @Override
    public void onDisable() {

        if (timeZoneManager != null) {
            timeZoneManager.stop();
        }

        getLogger().info(
                "ShadowCore disabled!"
        );


    }


    public HiraishinManager getHiraishinManager() {
        return hiraishinManager;
    }

    public PhoenixManager getPhoenixManager() {
        return phoenixManager;
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