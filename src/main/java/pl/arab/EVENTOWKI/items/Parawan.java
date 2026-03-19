package pl.arab.EVENTOWKI.items;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;
import pl.arab.EVENTOWKI.EventItem;

public class Parawan extends EventItem {

    public Parawan() {
        super("parawan");
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player p = e.getPlayer();
        if (!canUse(p, e.getItem())) return;

        e.setCancelled(true);

        double radius = getItemData().knockback_radius;
        double power = getItemData().knockback_power;
        if (radius <= 0) radius = 5.0;
        if (power <= 0) power = 1.5;

        for (Entity entity : p.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Player && entity != p) {
                Player target = (Player) entity;
                Vector direction = target.getLocation().toVector().subtract(p.getLocation().toVector()).normalize();
                direction.setY(0.4); // Lekkie podbicie w górę
                target.setVelocity(direction.multiply(power));
            }
        }

        finishUse(p, e.getItem());
    }
}