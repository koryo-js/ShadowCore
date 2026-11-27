package me.koryov.phoenix;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class PhoenixDeathListener implements Listener {

    private final PhoenixManager manager;
    private final Set<UUID> pendingRevival = new HashSet<>();
    private final Map<UUID, SavedInventory> savedInventories = new HashMap<>();

    public PhoenixDeathListener(PhoenixManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        // Nếu người chơi KHÔNG có lồng -> giữ nguyên luật rớt đồ của world
        if (!manager.hasChamber(player)) {
            return;
        }

        // 1. Ép bật KeepInventory riêng cho sự kiện chết này
        event.setKeepInventory(true);
        event.setKeepLevel(true);
        event.setDroppedExp(0);
        event.getDrops().clear();
        event.setDeathMessage(null);

        // 2. Sao lưu trực tiếp toàn bộ túi đồ + giáp + tay phụ + cấp độ đề phòng plugin khác ghi đè
        ItemStack[] contents = cloneItems(player.getInventory().getContents());
        savedInventories.put(player.getUniqueId(), new SavedInventory(
                contents,
                player.getLevel(),
                player.getExp()
        ));

        pendingRevival.add(player.getUniqueId());
        manager.prepare(player);

        Bukkit.getScheduler().runTaskLater(manager.getPlugin(), () -> {
            if (player.isOnline() && player.isDead()) {
                player.spigot().respawn();
            }
        }, 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!pendingRevival.contains(uuid)) {
            return;
        }

        Location chamber = manager.getChamber(player);
        if (chamber == null) {
            pendingRevival.remove(uuid);
            savedInventories.remove(uuid);
            return;
        }

        event.setRespawnLocation(chamber);

        Bukkit.getScheduler().runTaskLater(manager.getPlugin(), () -> {
            if (!player.isOnline() || player.isDead()) {
                return;
            }

            // Khôi phục lại chính xác túi đồ và kinh nghiệm nếu world/plugin khác làm mất
            SavedInventory backup = savedInventories.remove(uuid);
            if (backup != null) {
                player.getInventory().setContents(backup.contents);
                player.setLevel(backup.level);
                player.setExp(backup.exp);
                player.updateInventory();
            }

            pendingRevival.remove(uuid);
            new PhoenixRevivalTask(manager, player).begin();
        }, 1L);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
            player.setInvulnerable(false);
            player.setGravity(true);
            if (player.getWalkSpeed() == 0.0F) {
                player.setWalkSpeed(0.2F);
            }
        }
    }

    private ItemStack[] cloneItems(ItemStack[] source) {
        ItemStack[] copy = new ItemStack[source.length];
        for (int i = 0; i < source.length; i++) {
            if (source[i] != null) {
                copy[i] = source[i].clone();
            }
        }
        return copy;
    }

    private record SavedInventory(ItemStack[] contents, int level, float exp) {
    }
}
