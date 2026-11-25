package me.koryov.hiraishin;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import io.papermc.paper.event.player.PlayerArmSwingEvent;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.EquipmentSlot;

public class HiraishinListener implements Listener {

    private final HiraishinManager manager;

    public HiraishinListener(HiraishinManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onKunaiLaunch(PlayerLaunchProjectileEvent event) {
        if (!(event.getProjectile() instanceof Trident trident)) {
            return;
        }

        if (!manager.isKunai(event.getItemStack())) {
            return;
        }

        // The kunai is reusable; the thrown trident remains as the spatial mark.
        event.setShouldConsume(false);
        manager.registerThrownKunai(event.getPlayer(), trident);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onKunaiHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Trident trident) {
            manager.updateMarkFromProjectile(trident);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onArmSwing(PlayerArmSwingEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        if (!event.getPlayer().isSneaking()) {
            return;
        }

        if (!manager.hasMark(event.getPlayer())) {
            return;
        }

        manager.teleportToKunai(event.getPlayer());
    }
}
