package me.koryov.time;

import me.koryov.ShadowCorePlugin;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public final class TimeZoneListener implements Listener {

    private final TimeZoneManager manager;

    public TimeZoneListener(TimeZoneManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onRemoteUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();
        if (!TimeZoneRemote.isRemote(item)) {
            return;
        }

        event.setCancelled(true);
        Player player = event.getPlayer();

        if (ShadowCorePlugin.ALLOWED_PLAYERS.stream().noneMatch(name -> name.equalsIgnoreCase(player.getName()))) {
            player.sendMessage(ChatColor.RED + "You are not allowed to use the Time Zone Remote.");
            return;
        }

        boolean activated = manager.toggle(player);
        player.playSound(player.getLocation(),
                activated ? Sound.BLOCK_BEACON_ACTIVATE : Sound.BLOCK_BEACON_DEACTIVATE,
                0.7f, activated ? 1.4f : 0.8f);
        player.sendMessage(activated
                ? ChatColor.AQUA + "Time Zone activated."
                : ChatColor.GRAY + "Time Zone deactivated.");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFrozenPlayerMove(PlayerMoveEvent event) {
        if (!manager.isFrozen(event.getPlayer()) || event.getTo() == null) {
            return;
        }

        if (event.getFrom().getX() != event.getTo().getX()
                || event.getFrom().getY() != event.getTo().getY()
                || event.getFrom().getZ() != event.getTo().getZ()) {
            event.setTo(event.getFrom());
        }
    }
}
