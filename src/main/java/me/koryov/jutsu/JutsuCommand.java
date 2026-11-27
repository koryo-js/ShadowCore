package me.koryov.jutsu;

import me.koryov.ShadowCorePlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class JutsuCommand implements CommandExecutor {
    private final ShadowCorePlugin plugin;

    public JutsuCommand(ShadowCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        if (!plugin.isAllowed(player.getName())) {
            player.sendMessage("§cBạn không có quyền dùng ShadowCore Jutsu.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§7/shadowjutsu chestplate §f- nhận Shinobi Chestplate");
            player.sendMessage("§7/shadowjutsu pattern §f- nhận Jikūkan Pattern");
            return true;
        }

        if (args[0].equalsIgnoreCase("chestplate")) {
            player.getInventory().addItem(JutsuItems.createChestplate(plugin));
            player.sendMessage("§9Đã nhận Shinobi Chestplate.");
        } else if (args[0].equalsIgnoreCase("pattern")) {
            player.getInventory().addItem(JutsuItems.createPattern(plugin));
            player.sendMessage("§5Đã nhận Jikūkan Pattern.");
        } else {
            player.sendMessage("§cDùng: /shadowjutsu <chestplate|pattern>");
        }
        return true;
    }
}
