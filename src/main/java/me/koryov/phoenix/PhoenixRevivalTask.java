package me.koryov.phoenix;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public final class PhoenixRevivalTask extends BukkitRunnable {

    private static final int DURATION_TICKS = 100; // 5 giây

    private final PhoenixManager manager;
    private final Player player;
    private final Location chamber;
    private final float previousWalkSpeed;
    private final boolean previousGravity;
    private final boolean previousInvulnerable;
    private int ticks;

    public PhoenixRevivalTask(PhoenixManager manager, Player player) {
        this.manager = manager;
        this.player = player;
        this.chamber = manager.getChamber(player);
        this.previousWalkSpeed = player.getWalkSpeed();
        this.previousGravity = player.hasGravity();
        this.previousInvulnerable = player.isInvulnerable();
    }

    public void begin() {
        if (chamber == null) {
            cancel();
            return;
        }

        PhoenixChamber.close(chamber);

        // Đặt người chơi lơ lửng giữa cột nước, nhìn thẳng ra mặt kính trước
        Location suspendedLoc = chamber.clone().add(0.0, 0.6, 0.0);
        suspendedLoc.setYaw(0.0F);
        suspendedLoc.setPitch(0.0F);
        player.teleport(suspendedLoc);

        player.setVelocity(new Vector(0, 0, 0));
        player.setWalkSpeed(0.0F);
        player.setGravity(false);
        player.setInvulnerable(true);
        player.setFallDistance(0.0F);
        player.setRemainingAir(player.getMaximumAir());

        // Âm thanh khởi động buồng + lặn vào nước
        player.getWorld().playSound(chamber, Sound.BLOCK_BEACON_ACTIVATE, 1.0F, 0.6F);
        player.getWorld().playSound(chamber, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 0.9F, 0.7F);
        player.getWorld().playSound(chamber, Sound.AMBIENT_UNDERWATER_ENTER, 1.0F, 1.0F);

        player.sendActionBar(Component.text("§bĐANG TÁI TẠO CƠ THỂ..."));

        runTaskTimer(manager.getPlugin(), 0L, 1L);
    }

    @Override
    public void run() {
        if (!player.isOnline() || player.isDead()) {
            restore();
            cancel();
            return;
        }

        ticks++;

        // Giữ người chơi cố định lơ lửng ở tâm cột nước + hiệu ứng dao động nhẹ
        double bobbing = Math.sin(ticks * 0.15) * 0.08;
        Location holdLoc = chamber.clone().add(0.0, 0.6 + bobbing, 0.0);
        holdLoc.setYaw(player.getLocation().getYaw());
        holdLoc.setPitch(player.getLocation().getPitch());
        player.teleport(holdLoc);
        player.setVelocity(new Vector(0, 0, 0));
        player.setRemainingAir(player.getMaximumAir());

        // Hiệu ứng hạt bọt khí và năng lượng xoay quanh
        if (ticks % 4 == 0) {
            Location center = chamber.clone().add(0.0, 1.2, 0.0);
            player.getWorld().spawnParticle(Particle.BUBBLE_COLUMN_UP, center, 10, 0.25, 0.8, 0.25, 0.05);
            player.getWorld().spawnParticle(Particle.END_ROD, center, 6, 0.3, 0.7, 0.3, 0.01);
        }

        // Tiếng bọt khí sục nhẹ mỗi 0.5 giây
        if (ticks % 10 == 0) {
            player.getWorld().playSound(chamber, Sound.BLOCK_BUBBLE_COLUMN_UPWARDS_AMBIENT, 0.8F, 1.1F);
        }

        // Mỗi giây: âm thanh sạc năng lượng tăng dần cao độ (pitch từ 0.65 -> 1.5)
        if (ticks % 20 == 0) {
            int secondsLeft = Math.max(0, (DURATION_TICKS - ticks) / 20);
            float progress = (float) ticks / DURATION_TICKS;
            float risingPitch = 0.65F + (progress * 0.85F);

            player.sendActionBar(Component.text("§bTÁI TẠO §f" + secondsLeft + "s"));
            player.getWorld().playSound(chamber, Sound.BLOCK_CONDUIT_AMBIENT_SHORT, 1.1F, risingPitch);
            player.getWorld().playSound(chamber, Sound.BLOCK_NOTE_BLOCK_CHIME, 0.8F, risingPitch + 0.2F);
        }

        if (ticks >= DURATION_TICKS) {
            complete();
            cancel();
        }
    }

    private void complete() {
        // Phá vỡ cột kính giữa và làm nước tràn ra
        PhoenixChamber.shatterAndSpill(chamber, manager.getPlugin());

        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setSaturation(20.0F);
        player.setFireTicks(0);
        player.setFallDistance(0.0F);

        restore();

        // Đẩy người chơi văng ra phía trước (+Z) cùng dòng nước tràn
        player.setVelocity(new Vector(0.0, 0.35, 1.15));

        // Âm thanh sóng xung kích + hoàn tất tái tạo
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.6F, 1.6F);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.2F, 1.3F);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 0.5F, 1.35F);

        player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation().add(0.0, 1.0, 0.0),
                40, 0.5, 0.8, 0.5, 0.08);
        player.sendActionBar(Component.text("§aHỒI SINH HOÀN TẤT"));
    }

    private void restore() {
        if (!player.isOnline()) {
            return;
        }
        player.setWalkSpeed(0.2F); // Tốc độ đi bộ chuẩn của Minecraft
        player.setGravity(true);
        player.setInvulnerable(false);
        player.setNoDamageTicks(0);
    }
}
