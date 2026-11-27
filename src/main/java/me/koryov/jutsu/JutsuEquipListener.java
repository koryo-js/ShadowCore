package me.koryov.jutsu;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;

public final class JutsuEquipListener implements Listener {
    private final JutsuManager manager;
    public JutsuEquipListener(JutsuManager manager) { this.manager = manager; }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        manager.refreshKcm(event.getPlayer());
    }
}
