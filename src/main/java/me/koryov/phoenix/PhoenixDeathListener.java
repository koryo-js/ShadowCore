package me.koryov.phoenix;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class PhoenixDeathListener implements Listener {

    private final PhoenixManager manager;
    private final Set<UUID> pendingRevival = new HashSet<>();

    public PhoenixDeathListener(PhoenixManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!manager.hasChamber(player)) {
            return;
        }

        // Preserve the complete inventory and XP exactly as they were at death.
        event.setKeepInventory(true);
        event.setKeepLevel(true);
        event.setDroppedExp(0);
        event.getDrops().clear();
        event.setDeathMessage(null);

        pendingRevival.add(player.getUniqueId());
        manager.prepare(player);

        // Automatically respawn; the respawn event redirects the player into
        // their own registered chamber.
        Bukkit.getScheduler().runTaskLater(manager.getPlugin(), () -> {
            if (player.isOnline() && player.isDead()) {
                player.spigot().respawn();
            }
        }, 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (!pendingRevival.contains(player.getUniqueId())) {
            return;
        }

        Location chamber = manager.getChamber(player);
        if (chamber == null) {
            pendingRevival.remove(player.getUniqueId());
            return;
        }

        event.setRespawnLocation(chamber);

        Bukkit.getScheduler().runTaskLater(manager.getPlugin(), () -> {
            if (!player.isOnline() || player.isDead()) {
                return;
            }

            pendingRevival.remove(player.getUniqueId());
            new PhoenixRevivalTask(manager, player).begin();
        }, 1L);
    }
}
