package pl.arab.EVENTOWKI.items;

import org.bukkit.Sound;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import pl.arab.EVENTOWKI.EventItem;

public class SmoczyMiecz extends EventItem {

    public SmoczyMiecz() {
        super("smoczy_miecz");
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();

        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        if (!canUse(p, e.getItem())) return;
        e.setCancelled(true);

        // Wystrzelenie ender perły z gracza
        p.launchProjectile(EnderPearl.class);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ENDER_PEARL_THROW, 1.0f, 1.0f);

        // Nakładamy cooldown!
        finishUse(p, e.getItem());
    }
}