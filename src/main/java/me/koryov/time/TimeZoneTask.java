package me.koryov.time;

import org.bukkit.scheduler.BukkitRunnable;

public final class TimeZoneTask extends BukkitRunnable {

    private final TimeZoneManager manager;

    public TimeZoneTask(TimeZoneManager manager) {
        this.manager = manager;
    }

    @Override
    public void run() {
        manager.tick();
    }
}
