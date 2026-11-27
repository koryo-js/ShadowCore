package me.koryov.jutsu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public final class JutsuListener implements Listener {
    private final JutsuManager manager;

    public JutsuListener(JutsuManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND || event.getAction() != Action.RIGHT_CLICK_AIR) return;
        Player player = event.getPlayer();
        ItemStack mainHand = player.getInventory().getItemInMainHand();

        if (JutsuItems.isPattern(manager.plugin, mainHand)) {
            event.setCancelled(true);
            if (player.isSneaking()) {
                manager.teleportJikukan(player);
            } else {
                manager.activateJikukan(player);
            }
            return;
        }

        if (mainHand.getType().isAir() && player.isSneaking()
                && JutsuItems.isChestplate(manager.plugin, player.getInventory().getChestplate())) {
            event.setCancelled(true);
            manager.castRasengan(player);
        }
    }

    @EventHandler
    public void onAttackEntitySpawn(EntitySpawnEvent event) {
        manager.trackAttackEntity(event.getEntity());
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        manager.onProjectileHit(event.getEntity());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            Bukkit.getScheduler().runTask(manager.plugin, () -> manager.refreshKcm(player));
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            Bukkit.getScheduler().runTask(manager.plugin, () -> manager.refreshKcm(player));
        }
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        manager.refreshKcm(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        manager.removeKcmModifier(event.getPlayer());
    }
}
