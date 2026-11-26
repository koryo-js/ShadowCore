package me.koryov.phoenix;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public final class PhoenixRevivalTask extends BukkitRunnable {

    private static final int DURATION_TICKS = 100;

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
        player.setVelocity(player.getVelocity().zero());
        player.setWalkSpeed(0.0F);
        player.setGravity(false);
        player.setInvulnerable(true);
        player.setFallDistance(0.0F);

        player.getWorld().playSound(chamber, Sound.BLOCK_BEACON_ACTIVATE, 1.0F, 0.7F);
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

        // Keep the player centered and suspended inside the chamber.
        player.setVelocity(player.getVelocity().zero());
        if (ticks % 5 == 0) {
            Location center = chamber.clone().add(0.0, 1.0, 0.0);
            player.getWorld().spawnParticle(Particle.END_ROD, center, 18, 0.55, 0.8, 0.55, 0.015);
            player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, center, 5, 0.35, 0.6, 0.35, 0.005);
        }

        if (ticks % 20 == 0) {
            int secondsLeft = Math.max(0, (DURATION_TICKS - ticks) / 20);
            player.sendActionBar(Component.text("§bTÁI TẠO §f" + secondsLeft + "s"));
            player.getWorld().playSound(chamber, Sound.BLOCK_BEACON_AMBIENT, 0.7F, 1.2F);
        }

        if (ticks >= DURATION_TICKS) {
            complete();
            cancel();
        }
    }

    private void complete() {
        manager.finish(player);

        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setSaturation(20.0F);
        player.setFireTicks(0);
        player.setFallDistance(0.0F);

        restore();
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0F, 1.4F);
        player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation().add(0.0, 1.0, 0.0),
                45, 0.6, 0.9, 0.6, 0.08);
        player.sendActionBar(Component.text("§aHỒI SINH HOÀN TẤT"));
    }

    private void restore() {
        if (!player.isOnline()) {
            return;
        }
        player.setWalkSpeed(previousWalkSpeed);
        player.setGravity(previousGravity);
        player.setInvulnerable(previousInvulnerable);
    }
}
