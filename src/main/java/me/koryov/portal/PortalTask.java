package me.koryov.portal;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Bukkit;
import org.bukkit.util.Vector;

public class PortalTask extends BukkitRunnable {

    private final Player owner;

    private final PortalData first;
    private final PortalData second;

    private double rotation = 0;

    private int ticks = 0;

    public PortalTask(
            Player owner,
            PortalData first,
            PortalData second
    ) {

        this.owner = owner;
        this.first = first;
        this.second = second;

    }

    @Override
    public void run() {

        if (!owner.isOnline()) {

            cancel();

            return;

        }

        PortalPair pair =
                PortalManager.getPortal(
                        owner.getUniqueId()
                );

        if (pair == null) {

            cancel();
            return;

        }

        if (pair.getFirst().isActive()) {

            PortalRenderer.render(

                    pair.getFirst().getLocation(),

                    pair.getFirst().getForward(),

                    rotation,

                    pair.getFirst().getScale()

            );

        }

        if (pair.getSecond().isActive()) {

            PortalRenderer.render(

                    pair.getSecond().getLocation(),

                    pair.getSecond().getForward(),

                    rotation,

                    pair.getSecond().getScale()

            );

        }
        for (Player player : Bukkit.getOnlinePlayers()) {

            if (pair != null) {

                PortalTeleport.check(
                        player,
                        pair
                );

            }

        }

        rotation += 0.18;

        pair.getFirst().setScale(

                PortalAnimation.tick(

                        pair.getFirst().getScale(),

                        pair.getFirst().isActive()?1F:0F,

                        0.08F

                )

        );

        pair.getSecond().setScale(

                PortalAnimation.tick(

                        pair.getSecond().getScale(),

                        pair.getSecond().isActive()?1F:0F,

                        0.08F

                )

        );

        ticks++;

        if (ticks >= 20 * 15) {

            PortalManager.removePortal(

                    owner.getUniqueId()

            );

            cancel();

        }

    }


}