package me.koryov.jutsu;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;

public final class JutsuListener implements Listener {
    private final JutsuManager manager;

    public JutsuListener(JutsuManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND || event.getAction() != Action.RIGHT_CLICK_AIR) return;
        Player player = event.getPlayer();

        if (player.isSneaking() && JutsuItems.isChestplate(manager.plugin, player.getInventory().getChestplate())) {
            event.setCancelled(true);
            manager.castRasengan(player);
            return;
        }

        if (JutsuItems.isPattern(manager.plugin, player.getInventory().getItemInMainHand())) {
            event.setCancelled(true);
            manager.activateJikukan(player);
        }
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
