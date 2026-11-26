package me.koryov.hiraishin;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import io.papermc.paper.event.player.PlayerArmSwingEvent;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class HiraishinListener implements Listener {

    private final HiraishinManager manager;

    public HiraishinListener(HiraishinManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onKunaiLaunch(PlayerLaunchProjectileEvent event) {
        if (!(event.getProjectile() instanceof Trident trident)) {
            return;
        }

        if (!manager.isKunai(event.getItemStack())) {
            return;
        }

        Player player = event.getPlayer();

        // We handle the consumption ourselves so every thrown kunai costs exactly one item.
        event.setShouldConsume(false);
        consumeOneKunai(player, event.getHand());

        manager.registerThrownKunai(player, trident);
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

        Player player = event.getPlayer();

        if (!player.isSneaking()) {
            return;
        }

        if (!manager.hasMark(player)) {
            return;
        }

        manager.teleportToKunai(player);
    }

    private void consumeOneKunai(Player player, EquipmentSlot hand) {
        ItemStack item;

        if (hand == EquipmentSlot.OFF_HAND) {
            item = player.getInventory().getItemInOffHand();

            if (manager.isKunai(item)) {
                if (item.getAmount() <= 1) {
                    player.getInventory().setItemInOffHand(null);
                } else {
                    item.setAmount(item.getAmount() - 1);
                }
            }

            return;
        }

        item = player.getInventory().getItemInMainHand();

        if (manager.isKunai(item)) {
            if (item.getAmount() <= 1) {
                player.getInventory().setItemInMainHand(null);
            } else {
                item.setAmount(item.getAmount() - 1);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // Marks are stored in the player's PersistentDataContainer,
        // so they survive logout/restart. Nothing needs to be cleared here.
    }
}
