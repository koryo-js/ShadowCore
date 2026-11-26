package me.koryov.forcefield;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class ForcefieldListener implements Listener {

    private final JavaPlugin plugin;

    public ForcefieldListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEyePatchDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (!ForcefieldManager.isEyePatch(player.getInventory().getHelmet())) {
            return;
        }

        double damage = event.getFinalDamage();
        Location attackerLocation = null;
        if (event instanceof org.bukkit.event.entity.EntityDamageByEntityEvent byEntity) {
            Entity damager = byEntity.getDamager();
            attackerLocation = damager.getLocation();
        }

        event.setCancelled(true);
        ForcefieldDisplay.show(plugin, player, damage, attackerLocation);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 0.65f, 1.25f);
    }
}
