package pl.arab.EVENTOWKI.items;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;
import pl.arab.EVENTOWKI.EventItem;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BoskiTopor extends EventItem {

    private final Map<UUID, Long> immortalityMap = new HashMap<>();

    public BoskiTopor() { super("boski_topor"); } // ID z Twojego configu!

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player p = e.getPlayer();
        if (!canUse(p, e.getItem())) return;

        e.setCancelled(true);

        double radius = 8.0; // Możesz też dodać do getItemData().shockwave_radius;
        double power = 2.0;

        for (Entity entity : p.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Player && entity != p) {
                Player target = (Player) entity;
                Vector direction = target.getLocation().toVector().subtract(p.getLocation().toVector()).normalize();
                direction.setY(0.5);
                target.setVelocity(direction.multiply(power));
            }
        }

        int immSeconds = getItemData().immortality_duration;
        if(immSeconds == 0) immSeconds = 5; // Domyślnie z configu

        immortalityMap.put(p.getUniqueId(), System.currentTimeMillis() + (immSeconds * 1000L));
        p.sendMessage("§aZyskałeś nieśmiertelność na " + immSeconds + " sekund!");

        finishUse(p, e.getItem()); // Zdejmuje item (jeśli infinite:false) i nakłada Action Bar cooldown!
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (immortalityMap.containsKey(p.getUniqueId())) {
                if (immortalityMap.get(p.getUniqueId()) > System.currentTimeMillis()) {
                    e.setCancelled(true);
                } else {
                    immortalityMap.remove(p.getUniqueId());
                }
            }
        }
    }
}