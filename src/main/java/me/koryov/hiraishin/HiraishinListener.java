package me.koryov.hiraishin;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import io.papermc.paper.event.player.PlayerArmSwingEvent;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
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

        ItemStack launchedItem = event.getItemStack();
        if (!manager.isKunai(launchedItem)) {
            return;
        }

        Player player = event.getPlayer();

        // Paper's PlayerLaunchProjectileEvent does not expose getHand() here.
        // Identify the source slot by matching the launched stack.
        event.setShouldConsume(false);
        consumeOneKunai(player, launchedItem);

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
        if (event.getHand().name().equals("OFF_HAND")) {
            return;
        }

        Player player = event.getPlayer();

        if (!player.isSneaking() || !manager.hasMark(player)) {
            return;
        }

        manager.teleportToKunai(player);
    }

    private void consumeOneKunai(Player player, ItemStack launchedItem) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();

        if (manager.isKunai(mainHand) && mainHand.isSimilar(launchedItem)) {
            removeOneFromStack(mainHand, true, player);
            return;
        }

        ItemStack offHand = player.getInventory().getItemInOffHand();

        if (manager.isKunai(offHand) && offHand.isSimilar(launchedItem)) {
            removeOneFromStack(offHand, false, player);
        }
    }

    private void removeOneFromStack(ItemStack item, boolean mainHand, Player player) {
        if (item.getAmount() <= 1) {
            if (mainHand) {
                player.getInventory().setItemInMainHand(null);
            } else {
                player.getInventory().setItemInOffHand(null);
            }
        } else {
            item.setAmount(item.getAmount() - 1);
        }
    }
}
