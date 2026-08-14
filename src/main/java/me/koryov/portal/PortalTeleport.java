package me.koryov.portal;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PortalTeleport {

    private static final Map<UUID, Long> cooldown =
            new HashMap<>();

    public static void check(
            Player player,
            PortalPair pair
    ) {
        PortalData enter = null;
        PortalData exit = null;

        PortalData first = pair.getFirst();
        PortalData second = pair.getSecond();

        if (
                player.getWorld().equals(first.getLocation().getWorld())
                        &&
                        player.getLocation().distanceSquared(first.getLocation()) < 1.7 * 1.7
        ) {

            enter = first;
            exit = second;

        }
        else if (
                player.getWorld().equals(second.getLocation().getWorld())
                        &&
                        player.getLocation().distanceSquared(second.getLocation()) < 1.7 * 1.7
        ) {

            enter = second;
            exit = first;

        }
        else {

            return;

        }

        long now = System.currentTimeMillis();

        if (cooldown.containsKey(player.getUniqueId())) {

            if (now - cooldown.get(player.getUniqueId()) < 2000) {
                return;
            }

        }

        cooldown.put(player.getUniqueId(), now);

        Location destination =
                exit.getLocation().clone();

        destination.add(

                exit.getForward()
                        .clone()
                        .multiply(1.5)

        );

        destination.setDirection(
                exit.getForward()
        );

        player.teleport(destination);

        player.teleport(destination);
        destination.setDirection(
                exit.getForward()
                );

player.teleport(destination);

    }

}