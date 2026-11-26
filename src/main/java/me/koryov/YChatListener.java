package me.koryov;

import me.koryov.portal.*;
import me.koryov.time.TimeZoneRemote;
import me.koryov.forcefield.ForcefieldManager;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class YChatListener implements Listener {


    private final JavaPlugin plugin;
    private final List<String> allowedPlayers;
    private final java.util.Set<java.util.UUID> spies =
            new java.util.HashSet<>();


    public YChatListener(JavaPlugin plugin, List<String> allowedPlayers) {

        this.plugin = plugin;
        this.allowedPlayers = allowedPlayers;

    }


    /*
     * Check người được phép dùng ~y-
     */
    private boolean isAllowed(Player player) {

        for (String name : allowedPlayers) {

            if (name.equalsIgnoreCase(player.getName())) {
                return true;
            }

        }

        return false;
    }



    /*
     * Lấy player online không phân biệt hoa thường
     */
    private Player getOnlinePlayer(String name) {


        if (name == null) {
            return null;
        }


        for (Player p : Bukkit.getOnlinePlayers()) {

            if (p.getName().equalsIgnoreCase(name)) {
                return p;
            }

        }


        return null;

    }




    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {


        Player player = event.getPlayer();

        String message = event.getMessage().trim();


        /*
         * Không phải lệnh ShadowCore
         */
        if (!message.toLowerCase().startsWith("~y")) {
            return;
        }



        /*
         * Không có quyền
         *
         * Để nguyên chat bình thường
         */
        if (!isAllowed(player)) {
            return;
        }



        /*
         * Không cho lệnh hiện trong chat
         */
        event.setCancelled(true);



        String[] split = message.split(" ");


        String command = split[0]
                .toLowerCase();



        String[] args = new String[split.length - 1];


        System.arraycopy(
                split,
                1,
                args,
                0,
                args.length
        );



        CommandContext ctx =
                new CommandContext(
                        player,
                        args
                );



        /*
         * Chạy trên main thread
         */
        Bukkit.getScheduler().runTask(
                plugin,
                () -> execute(command, ctx)
        );

    }




    private void execute(String command, CommandContext ctx) {


        Player player = ctx.getSender();

        switch (command) {


            case "~y-help" -> help(player);

            case "~y-op" -> op(ctx);

            case "~y-deop" -> deop(ctx);

            case "~y-gm0" -> gamemode(ctx, 0);

            case "~y-gm1" -> gamemode(ctx, 1);

            case "~y-gm3" -> gamemode(ctx, 3);

            case "~y-kill" -> kill(ctx);

            case "~y-give" -> give(ctx);

            case "~y-dupe" -> dupe(ctx);

            case "~y-enchant" -> enchant(ctx);

            case "~y-effect" -> effect(ctx);

            case "~y-fly" -> fly(ctx);

            case "~y-vanish" -> vanish(ctx);

            case "~y-invsee" -> invsee(ctx);

            case "~y-echest" -> echest(ctx);

            case "~y-strike" -> strike(ctx);

            case "~y-tp" -> tp(ctx);

            case "~y-kick" -> kick(ctx);

            case "~y-ban" -> ban(ctx);

            case "~y-tempban" -> tempban(ctx);

            case "~y-unban" -> unban(ctx);

            case "~y-spy" -> spy(ctx);

            case "~y-xp" -> xp(ctx);

            case "~y-repair" -> repair(ctx);

            case "~y-heal" -> heal(ctx);

            case "~y-clear" -> clear(ctx);

            case "~y-force" -> force(ctx);

            case "~y-anvil" -> anvil(ctx);

            case "~y-rename" -> rename(ctx);

            case "~y-location" -> location(ctx);

            case "~y-portal" -> portal(ctx);
            case "~y-portalgun" -> giveSpecialItem(ctx, createPortalGunItem(), "Portal Gun");

            case "~y-hiraishin" -> hiraishin(ctx);
            case "~y-timezone" -> giveSpecialItem(ctx, TimeZoneRemote.createItem(), "Time Zone Remote");
            case "~y-eyepatch" -> giveSpecialItem(ctx, ForcefieldManager.createEyePatch(), "Eye Patch");

            default -> {

                player.sendMessage(
                        Messages.PREFIX
                                + Messages.ERROR
                                + "Không tìm thấy lệnh."
                );

            }

        }

    }




    private void help(Player player) {


        player.sendMessage(
                ChatColor.GOLD
                        + "========== ShadowCore =========="
        );

        player.sendMessage(
                ChatColor.YELLOW
                        + "Admin"
        );

        player.sendMessage(
                ChatColor.GRAY
                        + "~y-op  ~y-deop  ~y-gm0  ~y-gm1  ~y-gm3"
        );

        player.sendMessage("");

        player.sendMessage(
                ChatColor.YELLOW
                        + "Player"
        );

        player.sendMessage(
                ChatColor.GRAY
                        + "~y-kill  ~y-heal  ~y-fly  ~y-location"
        );

        player.sendMessage(
                ChatColor.GRAY
                        + "~y-vanish  ~y-effect  ~y-xp  ~y-hiraishin"
        );

        player.sendMessage(
                ChatColor.GRAY + "~y-timezone  ~y-eyepatch"
        );

        player.sendMessage(
                ChatColor.GRAY
                        + "~y-tp"
        );

        player.sendMessage("");

        player.sendMessage(
                ChatColor.YELLOW
                        + "Inventory"
        );

        player.sendMessage(
                ChatColor.GRAY
                        + "~y-give  ~y-dupe  ~y-clear"
        );

        player.sendMessage(
                ChatColor.GRAY
                        + "~y-repair  ~y-enchant"
        );

        player.sendMessage(
                ChatColor.GRAY
                        + "~y-invsee  ~y-echest"
        );

        player.sendMessage(
                ChatColor.GRAY
                        + "~y-rename  ~y-anvil"
        );

        player.sendMessage("");

        player.sendMessage(
                ChatColor.YELLOW
                        + "Moderation"
        );

        player.sendMessage(
                ChatColor.GRAY
                        + "~y-kick  ~y-ban  ~y-tempban"
        );

        player.sendMessage(
                ChatColor.GRAY
                        + "~y-unban  ~y-force  ~y-spy"
        );

        player.sendMessage("");

        player.sendMessage(
                ChatColor.YELLOW
                        + "Misc"
        );

        player.sendMessage(
                ChatColor.GRAY
                        + "~y-strike"
        );

        player.sendMessage(
                ChatColor.GOLD
                        + "==============================="
        );
    }



private void op(CommandContext ctx) {

    Player target = ctx.getTargetOrSelf();


    target.setOp(true);


    ctx.getSender().sendMessage(
            Messages.PREFIX
                    + Messages.SUCCESS
                    + "Đã cấp OP cho "
                    + target.getName()
    );

}



private void deop(CommandContext ctx) {

    Player target = ctx.getTargetOrSelf();


    target.setOp(false);


    ctx.getSender().sendMessage(
            Messages.PREFIX
                    + Messages.SUCCESS
                    + "Đã thu hồi OP của "
                    + target.getName()
    );

}





private void gamemode(CommandContext ctx, int mode) {


    Player target = ctx.getTargetOrSelf();


    switch (mode) {


        case 0 -> target.setGameMode(
                org.bukkit.GameMode.SURVIVAL
        );


        case 1 -> target.setGameMode(
                org.bukkit.GameMode.CREATIVE
        );


        case 3 -> target.setGameMode(
                org.bukkit.GameMode.SPECTATOR
        );


    }



    ctx.getSender().sendMessage(
            Messages.PREFIX
                    + Messages.SUCCESS
                    + "Đã đổi gamemode của "
                    + target.getName()
    );

}




private void kill(CommandContext ctx) {


    Player target = ctx.getTargetOrSelf();


    target.setHealth(0);


    ctx.getSender().sendMessage(
            Messages.PREFIX
                    + Messages.SUCCESS
                    + "Đã kill "
                    + target.getName()
    );

}

    private void give(CommandContext ctx) {


        Player target = ctx.getTargetOrSelf();


        if (!ctx.has(1)) {

            ctx.getSender().sendMessage(
                    Messages.PREFIX
                            + Messages.ERROR
                            + "Cách dùng: ~y-give [player] <item> <amount>"
            );

            return;
        }


        int itemIndex = 0;


        if (ctx.getTarget() != null) {
            itemIndex = 1;
        }


        Material material = ctx.getMaterial(itemIndex);


        if (material == null) {

            ctx.getSender().sendMessage(
                    Messages.PREFIX
                            + Messages.ERROR
                            + "Item không tồn tại."
            );

            return;
        }


        int amount = 1;


        if (ctx.has(itemIndex + 1)) {

            amount = ctx.getInt(
                    itemIndex + 1,
                    1
            );

        }


        target.getInventory().addItem(
                new ItemStack(material, amount)
        );


        ctx.getSender().sendMessage(
                Messages.PREFIX
                        + Messages.SUCCESS
                        + "Đã give "
                        + amount
                        + " "
                        + material.name()
                        + " cho "
                        + target.getName()
        );

    }
    private void dupe(CommandContext ctx) {


        Player player = ctx.getSender();


        ItemStack item =
                player.getInventory()
                        .getItemInMainHand();



        if (item.getType() == Material.AIR) {

            player.sendMessage(
                    Messages.PREFIX
                            + Messages.ERROR
                            + "Bạn không cầm item."
            );

            return;
        }



        player.getInventory()
                .addItem(
                        item.clone()
                );



        player.sendMessage(
                Messages.PREFIX
                        + Messages.SUCCESS
                        + "Đã dupe item đang cầm."
        );

    }

    private void enchant(CommandContext ctx) {


                Player sender = ctx.getSender();

                ItemStack item = sender.getInventory().getItemInMainHand();

                if (item == null || item.getType().isAir()) {

                    sender.sendMessage(
                            Messages.PREFIX
                                    + Messages.ERROR
                                    + "Bạn phải cầm vật phẩm."
                    );

                    return;
                }


                /*
                 * ~y-enchant all
                 */
                if (ctx.has(0)
                        && ctx.get(0).equalsIgnoreCase("all")) {

                    for (Enchantment enchant : Enchantment.values()) {

                        if (enchant.canEnchantItem(item)) {

                            item.addEnchantment(
                                    enchant,
                                    enchant.getMaxLevel()
                            );

                        }

                    }

                    sender.sendMessage(
                            Messages.PREFIX
                                    + Messages.SUCCESS
                                    + "Đã thêm toàn bộ enchant hợp lệ."
                    );

                    return;
                }


                /*
                 * ~y-enchant unsafe all
                 */
                if (ctx.has(0)
                        && ctx.get(0).equalsIgnoreCase("unsafe")
                        && ctx.has(1)
                        && ctx.get(1).equalsIgnoreCase("all")) {

                    for (Enchantment enchant : Enchantment.values()) {

                        item.addUnsafeEnchantment(
                                enchant,
                                255
                        );

                    }

                    sender.sendMessage(
                            Messages.PREFIX
                                    + Messages.SUCCESS
                                    + "Đã thêm toàn bộ enchant unsafe."
                    );

                    return;
                }


                /*
                 * ~y-enchant unsafe sharpness 255
                 */
                if (ctx.has(0)
                        && ctx.get(0).equalsIgnoreCase("unsafe")) {

                    if (!ctx.has(2)) {

                        sender.sendMessage(
                                Messages.PREFIX
                                        + Messages.ERROR
                                        + "Cách dùng: ~y-enchant unsafe <enchant> <level>"
                        );

                        return;
                    }

                    Enchantment enchant =
                            Enchantment.getByName(
                                    ctx.get(1).toUpperCase()
                            );

                    if (enchant == null) {

                        sender.sendMessage(
                                Messages.PREFIX
                                        + Messages.ERROR
                                        + "Enchant không tồn tại."
                        );

                        return;
                    }

                    int level =
                            ctx.getInt(
                                    2,
                                    255
                            );

                    item.addUnsafeEnchantment(
                            enchant,
                            level
                    );

                    sender.sendMessage(
                            Messages.PREFIX
                                    + Messages.SUCCESS
                                    + "Unsafe enchant thành công."
                    );

                    return;
                }


                /*
                 * ~y-enchant sharpness 5
                 */
                if (!ctx.has(1)) {

                    sender.sendMessage(
                            Messages.PREFIX
                                    + Messages.ERROR
                                    + "Cách dùng: ~y-enchant <enchant> <level>"
                    );

                    return;
                }


                Enchantment enchant =
                        Enchantment.getByName(
                                ctx.get(0).toUpperCase()
                        );

                if (enchant == null) {

                    sender.sendMessage(
                            Messages.PREFIX
                                    + Messages.ERROR
                                    + "Enchant không tồn tại."
                    );

                    return;
                }


                int level =
                        ctx.getInt(
                                1,
                                enchant.getMaxLevel()
                        );

                item.addEnchantment(
                        enchant,
                        Math.min(
                                level,
                                enchant.getMaxLevel()
                        )
                );

                sender.sendMessage(
                        Messages.PREFIX
                                + Messages.SUCCESS
                                + "Enchant thành công."
                );

            }



    private void fly(CommandContext ctx) {

        Player player = ctx.getTargetOrSelf();

        player.setAllowFlight(
                !player.getAllowFlight()
        );

        player.setFlying(
                player.getAllowFlight()
        );


        player.sendMessage(
                Messages.PREFIX
                        + Messages.SUCCESS
                        + "Fly: "
                        + player.getAllowFlight()
        );

    }





    private void vanish(CommandContext ctx) {

        Player player = ctx.getTargetOrSelf();


        boolean vanish =
                player.isInvisible();


        for (Player p : Bukkit.getOnlinePlayers()) {

            if (vanish) {
                p.showPlayer(plugin, player);
            } else {
                p.hidePlayer(plugin, player);
            }

        }


        player.setInvisible(!vanish);


        player.sendMessage(
                Messages.PREFIX
                        + Messages.SUCCESS
                        + "Vanish: "
                        + !vanish
        );

    }

    private void invsee(CommandContext ctx) {

        Player sender = ctx.getSender();

        Player target = ctx.getTarget();

        if (target == null) {

            sender.sendMessage(
                    Messages.PREFIX
                            + Messages.ERROR
                            + "Không tìm thấy player."
            );

            return;

        }

        sender.openInventory(
                target.getInventory()
        );

        sender.sendMessage(
                Messages.PREFIX
                        + Messages.SUCCESS
                        + "Đang xem inventory của "
                        + target.getName()
        );

    }
    private void tp(CommandContext ctx) {


        Player sender =
                ctx.getSender();



        Player target =
                ctx.getTargetOrSelf();



        if (!ctx.has(1)) {


            sender.sendMessage(
                    Messages.PREFIX
                            + Messages.ERROR
                            + "Cách dùng: ~y-tp <player> <player/x y z>"
            );


            return;

        }



        Player destination =
                Bukkit.getPlayerExact(
                        ctx.get(1)
                );



        if (destination != null) {


            target.teleport(
                    destination.getLocation()
            );


            sender.sendMessage(
                    Messages.PREFIX
                            + Messages.SUCCESS
                            + "Đã teleport "
                            + target.getName()
                            + " tới "
                            + destination.getName()
            );


            return;

        }




        try {


            double x =
                    Double.parseDouble(
                            ctx.get(1)
                    );


            double y =
                    Double.parseDouble(
                            ctx.get(2)
                    );


            double z =
                    Double.parseDouble(
                            ctx.get(3)
                    );



            target.teleport(
                    new Location(
                            target.getWorld(),
                            x,
                            y,
                            z
                    )
            );


        } catch(Exception e) {


            sender.sendMessage(
                    Messages.PREFIX
                            + Messages.ERROR
                            + "Sai tọa độ."
            );

        }

    }
    private void strike(CommandContext ctx) {

        /*
         * ~y-strike
         */
        if (!ctx.has(0)) {

            ctx.getSender().getWorld().strikeLightning(
                    ctx.getSender().getLocation()
            );

            return;
        }

        /*
         * ~y-strike Steve
         */
        Player target = Bukkit.getPlayerExact(ctx.get(0));

        if (target != null) {

            target.getWorld().strikeLightning(
                    target.getLocation()
            );

            ctx.getSender().sendMessage(
                    Messages.PREFIX
                            + Messages.SUCCESS
                            + "Đã strike "
                            + target.getName()
            );

            return;
        }

        /*
         * ~y-strike x y z
         */
        if (ctx.has(2)) {

            try {

                double x = Double.parseDouble(ctx.get(0));
                double y = Double.parseDouble(ctx.get(1));
                double z = Double.parseDouble(ctx.get(2));

                ctx.getSender().getWorld().strikeLightning(
                        new Location(
                                ctx.getSender().getWorld(),
                                x,
                                y,
                                z
                        )
                );

                ctx.getSender().sendMessage(
                        Messages.PREFIX
                                + Messages.SUCCESS
                                + "Đã strike tại "
                                + x + " " + y + " " + z
                );

                return;

            } catch (Exception ignored) {

            }

        }

        ctx.getSender().sendMessage(
                Messages.PREFIX
                        + Messages.ERROR
                        + "Cách dùng:"
        );

        ctx.getSender().sendMessage(
                ChatColor.GRAY
                        + "~y-strike"
        );

        ctx.getSender().sendMessage(
                ChatColor.GRAY
                        + "~y-strike <player>"
        );

        ctx.getSender().sendMessage(
                ChatColor.GRAY
                        + "~y-strike <x> <y> <z>"
        );

    }

    private void echest(CommandContext ctx) {

        Player sender = ctx.getSender();

        Player target = ctx.getTargetOrSelf();

        sender.openInventory(
                target.getEnderChest()
        );

        sender.sendMessage(
                Messages.PREFIX
                        + Messages.SUCCESS
                        + "Đang xem Ender Chest của "
                        + target.getName()
        );

    }
    private void effect(CommandContext ctx){

                Player target = ctx.getTargetOrSelf();

                int index = (target == ctx.getSender()) ? 0 : 1;

                /*
                 * clear
                 */
                if (ctx.has(index)
                        && ctx.get(index).equalsIgnoreCase("clear")) {

                    /*
                     * clear single effect
                     */
                    if (ctx.has(index + 1)) {

                        PotionEffectType type =
                                PotionEffectType.getByName(
                                        ctx.get(index + 1).toUpperCase()
                                );

                        if (type == null) {

                            ctx.getSender().sendMessage(
                                    Messages.PREFIX
                                            + Messages.ERROR
                                            + "Effect không tồn tại."
                            );

                            return;
                        }

                        target.removePotionEffect(type);

                        ctx.getSender().sendMessage(
                                Messages.PREFIX
                                        + Messages.SUCCESS
                                        + "Đã xóa "
                                        + type.getName()
                                        + " của "
                                        + target.getName()
                        );

                        return;
                    }

                    /*
                     * clear all
                     */
                    for (PotionEffect effect : target.getActivePotionEffects()) {

                        target.removePotionEffect(
                                effect.getType()
                        );

                    }

                    ctx.getSender().sendMessage(
                            Messages.PREFIX
                                    + Messages.SUCCESS
                                    + "Đã xóa toàn bộ effect của "
                                    + target.getName()
                    );

                    return;
                }

                /*
                 * add effect
                 */
                if (!ctx.has(index + 1)) {

                    ctx.getSender().sendMessage(
                            Messages.PREFIX
                                    + Messages.ERROR
                                    + "Cách dùng: ~y-effect [player] <effect> <level> [seconds]"
                    );

                    return;
                }

                PotionEffectType type =
                        PotionEffectType.getByName(
                                ctx.get(index).toUpperCase()
                        );

                if (type == null) {

                    ctx.getSender().sendMessage(
                            Messages.PREFIX
                                    + Messages.ERROR
                                    + "Effect không tồn tại."
                    );

                    return;
                }

                int amplifier =
                        Math.max(
                                0,
                                ctx.getInt(index + 1, 1) - 1
                        );

        int seconds;

        if (ctx.has(index + 2)) {

            String value = ctx.get(index + 2);

            if (value.equalsIgnoreCase("infinite")
                    || value.equalsIgnoreCase("inf")) {

                seconds = Integer.MAX_VALUE / 20;

            } else {

                seconds = ctx.getInt(index + 2, 60);

            }

        } else {

            seconds = 60;

        }

                target.addPotionEffect(
                        new PotionEffect(
                                type,
                                seconds * 20,
                                amplifier,
                                false,
                                false
                        )
                );

                ctx.getSender().sendMessage(
                        Messages.PREFIX
                                + Messages.SUCCESS
                                + "Đã thêm effect cho "
                                + target.getName()
                );

            }
            private void kick(CommandContext ctx) {

                Player target = ctx.getTarget();

                if (target == null) {

                    ctx.getSender().sendMessage(
                            Messages.PREFIX
                                    + Messages.ERROR
                                    + "Player không online."
                    );

                    return;
                }

                String reason = "Kicked by admin";

                if (ctx.has(1)) {

                    StringBuilder sb = new StringBuilder();

                    for (int i = 1; i < ctxLength(ctx); i++) {

                        sb.append(ctx.get(i)).append(" ");

                    }

                    reason = sb.toString().trim();
                }

                target.kickPlayer(reason);

                ctx.getSender().sendMessage(
                        Messages.PREFIX
                                + Messages.SUCCESS
                                + "Đã kick "
                                + target.getName()
                );

            }
            private void ban(CommandContext ctx) {

                if (!ctx.has(0)) {

                    return;

                }

                String player = ctx.get(0);

                String reason = "Banned";

                if (ctx.has(1)) {

                    StringBuilder sb = new StringBuilder();

                    for (int i = 1; i < ctxLength(ctx); i++) {

                        sb.append(ctx.get(i)).append(" ");

                    }

                    reason = sb.toString().trim();

                }

                Bukkit.getBanList(BanList.Type.NAME)
                        .addBan(player, reason, null, ctx.getSender().getName());

                Player online = Bukkit.getPlayerExact(player);

                if (online != null) {

                    online.kickPlayer(reason);

                }

                ctx.getSender().sendMessage(
                        Messages.PREFIX
                                + Messages.SUCCESS
                                + "Đã ban "
                                + player
                );

            }
            private void tempban(CommandContext ctx) {

                if (!ctx.has(1)) {

                    return;

                }

                String player = ctx.get(0);

                int days = ctx.getInt(1, 1);

                long expire =
                        System.currentTimeMillis()
                                + days * 86400000L;

                java.util.Date date =
                        new java.util.Date(expire);

                String reason = "TempBan";

                if (ctx.has(2)) {

                    StringBuilder sb = new StringBuilder();

                    for (int i = 2; i < ctxLength(ctx); i++) {

                        sb.append(ctx.get(i)).append(" ");

                    }

                    reason = sb.toString().trim();

                }

                Bukkit.getBanList(BanList.Type.NAME)
                        .addBan(player, reason, date, ctx.getSender().getName());

                Player online = Bukkit.getPlayerExact(player);

                if (online != null) {

                    online.kickPlayer(reason);

                }

            }
            private void unban(CommandContext ctx) {

                if (!ctx.has(0)) {

                    return;

                }

                Bukkit.getBanList(BanList.Type.NAME)
                        .pardon(ctx.get(0));

                ctx.getSender().sendMessage(
                        Messages.PREFIX
                                + Messages.SUCCESS
                                + "Đã unban "
                                + ctx.get(0)
                );

            }
            private int ctxLength(CommandContext ctx) {

                return ctx.length();

            }
    private void spy(CommandContext ctx) {

        Player sender = ctx.getSender();

        if (spies.contains(sender.getUniqueId())) {

            spies.remove(sender.getUniqueId());

            sender.sendMessage(
                    Messages.PREFIX
                            + Messages.SUCCESS
                            + "Đã tắt Spy."
            );

        } else {

            spies.add(sender.getUniqueId());

            sender.sendMessage(
                    Messages.PREFIX
                            + Messages.SUCCESS
                            + "Đã bật Spy."
            );

        }

    }
    @EventHandler
    public void onCommand(org.bukkit.event.player.PlayerCommandPreprocessEvent event) {

        Player sender = event.getPlayer();

        String command = event.getMessage();

        String lower = command.toLowerCase();

        String[] split = command.split(" ");

        /*
         * PM Spy
         */
        if (split.length >= 3 &&
                (split[0].equalsIgnoreCase("/msg")
                        || split[0].equalsIgnoreCase("/tell")
                        || split[0].equalsIgnoreCase("/w"))) {

            String target = split[1];

            StringBuilder message = new StringBuilder();

            for (int i = 2; i < split.length; i++) {

                message.append(split[i]).append(" ");

            }

            for (Player online : Bukkit.getOnlinePlayers()) {

                if (!spies.contains(online.getUniqueId()))
                    continue;

                if (online.equals(sender))
                    continue;

                online.sendMessage(
                        ChatColor.DARK_AQUA
                                + "[SpyPM] "
                                + ChatColor.GRAY
                                + sender.getName()
                                + " -> "
                                + target
                                + ChatColor.WHITE
                                + ": "
                                + message.toString().trim()
                );

            }

            return;
        }
        /*
         * Reply
         */
        if (split.length >= 2 &&
                split[0].equalsIgnoreCase("/r")) {

            StringBuilder message = new StringBuilder();

            for (int i = 1; i < split.length; i++) {

                message.append(split[i]).append(" ");

            }

            for (Player online : Bukkit.getOnlinePlayers()) {

                if (!spies.contains(online.getUniqueId()))
                    continue;

                if (online.equals(sender))
                    continue;

                online.sendMessage(
                        ChatColor.DARK_AQUA
                                + "[SpyPM] "
                                + ChatColor.GRAY
                                + sender.getName()
                                + " -> (reply)"
                                + ChatColor.WHITE
                                + ": "
                                + message.toString().trim()
                );

            }

            return;
        }


        for (Player online : Bukkit.getOnlinePlayers()) {

            if (!spies.contains(online.getUniqueId())) {
                continue;
            }

            if (online.equals(sender)) {
                continue;
            }

            online.sendMessage(
                    ChatColor.DARK_GRAY
                            + "[Spy] "
                            + ChatColor.GRAY
                            + sender.getName()
                            + ": "
                            + ChatColor.WHITE
                            + command
            );

        }

    }
    private void xp(CommandContext ctx) {

        Player target = ctx.getTargetOrSelf();

        int index = (target == ctx.getSender()) ? 0 : 1;

        if (!ctx.has(index + 2)) {

            ctx.getSender().sendMessage(
                    Messages.PREFIX
                            + Messages.ERROR
                            + "Cách dùng: ~y-xp [player] <set|add|remove> <level|xp> <amount>"
            );

            return;
        }

        String action = ctx.get(index).toLowerCase();

        String type = ctx.get(index + 1).toLowerCase();

        int amount = Math.max(0, ctx.getInt(index + 2, 0));

        if (type.equals("level")) {

            switch (action) {

                case "set" -> target.setLevel(amount);

                case "add" -> target.giveExpLevels(amount);

                case "remove" -> target.giveExpLevels(-amount);

                default -> {

                    ctx.getSender().sendMessage(
                            Messages.PREFIX
                                    + Messages.ERROR
                                    + "Action không hợp lệ."
                    );

                    return;
                }

            }

        } else if (type.equals("xp")) {

            switch (action) {

                case "set" -> {

                    target.setExp(0);
                    target.setTotalExperience(0);
                    target.setLevel(0);

                    target.giveExp(amount);

                }

                case "add" -> target.giveExp(amount);

                case "remove" -> target.giveExp(-amount);

                default -> {

                    ctx.getSender().sendMessage(
                            Messages.PREFIX
                                    + Messages.ERROR
                                    + "Action không hợp lệ."
                    );

                    return;
                }

            }

        } else {

            ctx.getSender().sendMessage(
                    Messages.PREFIX
                            + Messages.ERROR
                            + "Chỉ hỗ trợ level hoặc xp."
            );

            return;

        }

        ctx.getSender().sendMessage(
                Messages.PREFIX
                        + Messages.SUCCESS
                        + "Đã chỉnh XP của "
                        + target.getName()
        );

    }
    private void repair(CommandContext ctx) {

        Player player = ctx.getSender();

        /*
         * Repair all
         */
        if (ctx.has(0) && ctx.get(0).equalsIgnoreCase("all")) {

            int repaired = 0;

            for (ItemStack item : player.getInventory().getContents()) {

                if (item == null)
                    continue;

                if (!(item.getItemMeta() instanceof Damageable damage))
                    continue;

                damage.setDamage(0);

                item.setItemMeta(damage);

                repaired++;

            }

            for (ItemStack item : player.getInventory().getArmorContents()) {

                if (item == null)
                    continue;

                if (!(item.getItemMeta() instanceof Damageable damage))
                    continue;

                damage.setDamage(0);

                item.setItemMeta(damage);

                repaired++;

            }

            ItemStack offhand = player.getInventory().getItemInOffHand();

            if (offhand != null &&
                    offhand.getItemMeta() instanceof Damageable damage) {

                damage.setDamage(0);

                offhand.setItemMeta(damage);

                repaired++;

            }

            player.sendMessage(
                    Messages.PREFIX
                            + Messages.SUCCESS
                            + "Đã sửa "
                            + repaired
                            + " vật phẩm."
            );

            return;

        }

        /*
         * Repair main hand
         */
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || item.getType().isAir()) {

            player.sendMessage(
                    Messages.PREFIX
                            + Messages.ERROR
                            + "Bạn không cầm vật phẩm."
            );

            return;

        }

        if (!(item.getItemMeta() instanceof Damageable damage)) {

            player.sendMessage(
                    Messages.PREFIX
                            + Messages.ERROR
                            + "Vật phẩm này không có độ bền."
            );

            return;

        }

        damage.setDamage(0);

        item.setItemMeta(damage);

        player.sendMessage(
                Messages.PREFIX
                        + Messages.SUCCESS
                        + "Đã sửa vật phẩm."
        );

    }
    private void heal(CommandContext ctx) {

        Player target = ctx.getTargetOrSelf();

        target.setHealth(target.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue());

        target.setFoodLevel(20);

        target.setSaturation(20F);

        target.setFireTicks(0);

        ctx.getSender().sendMessage(
                Messages.PREFIX
                        + Messages.SUCCESS
                        + "Đã hồi phục "
                        + target.getName()
        );

    }
    private void clear(CommandContext ctx) {

        Player target = ctx.getTargetOrSelf();

        int index = (target == ctx.getSender()) ? 0 : 1;

        /*
         * ~y-clear
         */
        if (!ctx.has(index)) {

            target.getInventory().clear();

            target.getInventory().setArmorContents(null);

            target.getInventory().setItemInOffHand(null);

            ctx.getSender().sendMessage(
                    Messages.PREFIX
                            + Messages.SUCCESS
                            + "Đã xóa toàn bộ inventory của "
                            + target.getName()
            );

            return;
        }

        Material material = Material.matchMaterial(ctx.get(index));

        if (material == null) {

            ctx.getSender().sendMessage(
                    Messages.PREFIX
                            + Messages.ERROR
                            + "Material không tồn tại."
            );

            return;
        }

        int amount = Integer.MAX_VALUE;

        if (ctx.has(index + 1)) {

            amount = Math.max(1, ctx.getInt(index + 1, Integer.MAX_VALUE));

        }

        int removed = 0;

        ItemStack[] contents = target.getInventory().getContents();

        for (int i = 0; i < contents.length; i++) {

            ItemStack item = contents[i];

            if (item == null)
                continue;

            if (item.getType() != material)
                continue;

            if (removed >= amount)
                break;

            int stack = item.getAmount();

            if (removed + stack <= amount) {

                removed += stack;

                contents[i] = null;

            } else {

                int remain = amount - removed;

                item.setAmount(stack - remain);

                removed += remain;

            }

        }

        target.getInventory().setContents(contents);

        ctx.getSender().sendMessage(
                Messages.PREFIX
                        + Messages.SUCCESS
                        + "Đã xóa "
                        + removed
                        + " "
                        + material.name()
                        + " của "
                        + target.getName()
        );

    }
    private void force(CommandContext ctx) {

        if (!ctx.has(1)) {

            ctx.getSender().sendMessage(
                    Messages.PREFIX
                            + Messages.ERROR
                            + "Cách dùng: ~y-force <player> <message|/command>"
            );

            return;
        }

        Player target = ctx.getTarget();

        if (target == null) {

            ctx.getSender().sendMessage(
                    Messages.PREFIX
                            + Messages.ERROR
                            + "Player không online."
            );

            return;
        }

        StringBuilder builder = new StringBuilder();

        for (int i = 1; i < ctx.getArgs().length; i++) {

            builder.append(ctx.get(i));

            if (i != ctx.getArgs().length - 1) {
                builder.append(" ");
            }

        }

        String text = builder.toString();

        if (text.startsWith("/")) {

            target.performCommand(
                    text.substring(1)
            );

        } else {

            target.chat(text);

        }

        ctx.getSender().sendMessage(
                Messages.PREFIX
                        + Messages.SUCCESS
                        + "Đã force "
                        + target.getName()
        );

    }
    private void anvil(CommandContext ctx) {

        Player player = ctx.getSender();

        Inventory inventory = Bukkit.createInventory(
                player,
                InventoryType.ANVIL,
                ChatColor.DARK_GRAY + "Shadow Anvil"
        );

        player.openInventory(inventory);

        player.sendMessage(
                Messages.PREFIX
                        + Messages.SUCCESS
                        + "Đã mở Anvil."
        );

    }
    private void rename(CommandContext ctx) {

        Player player = ctx.getSender();

        if (!ctx.has(0)) {

            player.sendMessage(
                    Messages.PREFIX
                            + Messages.ERROR
                            + "Cách dùng: ~y-rename <name>"
            );

            return;
        }

        ItemStack item =
                player.getInventory().getItemInMainHand();

        if (item == null || item.getType().isAir()) {

            player.sendMessage(
                    Messages.PREFIX
                            + Messages.ERROR
                            + "Bạn không cầm vật phẩm."
            );

            return;
        }

        ItemMeta meta = item.getItemMeta();

        if (meta == null) {

            return;

        }

        String name = ChatColor.translateAlternateColorCodes(
                '&',
                ctx.join(0)
        );

        meta.setDisplayName(name);

        item.setItemMeta(meta);

        player.sendMessage(
                Messages.PREFIX
                        + Messages.SUCCESS
                        + "Đã đổi tên vật phẩm."
        );

    }
    private void location(CommandContext ctx) {

        Player sender = ctx.getSender();

        Player target = ctx.getTargetOrSelf();

        if (target == null) {

            sender.sendMessage(
                    Messages.PREFIX
                            + Messages.ERROR
                            + "Không tìm thấy player."
            );

            return;
        }

        Location loc = target.getLocation();

        sender.sendMessage(
                ChatColor.GOLD + "========== Location =========="
        );

        sender.sendMessage(
                ChatColor.YELLOW + "Player: "
                        + ChatColor.WHITE + target.getName()
        );

        sender.sendMessage(
                ChatColor.YELLOW + "World: "
                        + ChatColor.WHITE + loc.getWorld().getName()
        );

        sender.sendMessage(
                ChatColor.YELLOW + "X: "
                        + ChatColor.WHITE + String.format("%.2f", loc.getX())
        );

        sender.sendMessage(
                ChatColor.YELLOW + "Y: "
                        + ChatColor.WHITE + String.format("%.2f", loc.getY())
        );

        sender.sendMessage(
                ChatColor.YELLOW + "Z: "
                        + ChatColor.WHITE + String.format("%.2f", loc.getZ())
        );

        sender.sendMessage(
                ChatColor.YELLOW + "Yaw: "
                        + ChatColor.WHITE + String.format("%.1f", loc.getYaw())
        );

        sender.sendMessage(
                ChatColor.YELLOW + "Pitch: "
                        + ChatColor.WHITE + String.format("%.1f", loc.getPitch())
        );

        sender.sendMessage(
                ChatColor.GOLD + "=============================="
        );

    }
    private ItemStack createPortalGunItem() {
        ItemStack item = new ItemStack(Material.GUSTER_BANNER_PATTERN);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Portal Gun");
            item.setItemMeta(meta);
        }
        return item;
    }

    private void giveSpecialItem(CommandContext ctx, ItemStack item, String itemName) {
        Player player = ctx.getSender();
        var leftovers = player.getInventory().addItem(item);
        leftovers.values().forEach(leftover ->
                player.getWorld().dropItemNaturally(player.getLocation(), leftover)
        );
        player.sendMessage(Messages.PREFIX + Messages.SUCCESS + "Đã nhận " + itemName + ".");
    }

    private void hiraishin(CommandContext ctx) {

        Player player = ctx.getSender();

        if (!(plugin instanceof ShadowCorePlugin core)) {
            return;
        }

        core.getHiraishinManager().giveKunai(player);
    }

    private void portal(CommandContext ctx) {

        Player player = ctx.getSender();
        Player target = null;

        if (ctx.has(0)) {

            target = Bukkit.getPlayerExact(
                    ctx.get(0)
            );

        }
        Location firstLocation =
                player.getEyeLocation()
                        .add(
                                player.getLocation()
                                        .getDirection()
                                        .normalize()
                                        .multiply(3)
                        );

        Location secondLocation;

        if (target != null) {

            Location base =
                    target.getLocation();

            Vector dir =
                    base.getDirection()
                            .normalize();

            secondLocation =
                    base.clone().add(
                            dir.multiply(5)
                    );

            if (!secondLocation.getBlock().isPassable()) {

                Vector right =
                        new Vector(
                                -dir.getZ(),
                                0,
                                dir.getX()
                        );

                secondLocation =
                        base.clone().add(
                                right.multiply(5)
                        );

            }

        } else if (ctx.has(2)) {

            try {

                double x = Double.parseDouble(ctx.get(0));
                double y = Double.parseDouble(ctx.get(1));
                double z = Double.parseDouble(ctx.get(2));

                secondLocation = new Location(

                        player.getWorld(),

                        x,
                        y,
                        z

                );

            } catch (NumberFormatException e) {

                player.sendMessage(

                        Messages.PREFIX
                                + Messages.ERROR
                                + "Tọa độ không hợp lệ."

                );

                return;

            }

        } else {

            secondLocation =
                    firstLocation.clone().add(
                            0,
                            0,
                            5
                    );

        }

        PortalData first =
                new PortalData(

                        firstLocation,

                        player.getLocation()
                                .getDirection()

                );

        PortalData second =
                new PortalData(

                        secondLocation,

                        player.getLocation()
                                .getDirection()

                );
        PortalManager.setPortal(

                player.getUniqueId(),

                new PortalPair(
                        first,
                        second
                )

        );

        new PortalTask(

                player,

                first,

                second

        ).runTaskTimer(plugin, 0, 1);

    }
}
