package pl.arab.EVENTOWKI.items;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import pl.arab.EVENTOWKI.EventItem;

public class WedkaSurfera extends EventItem {

    public WedkaSurfera() {
        super("wedka_surferka");
    }

    @EventHandler
    public void onFish(PlayerFishEvent e) {
        Player p = e.getPlayer();
        ItemStack item = p.getInventory().getItemInMainHand();

        // Jeśli to nie nasza wędka, ignorujemy
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().getPersistentDataContainer().has(this.getKey(), org.bukkit.persistence.PersistentDataType.BYTE)) {
            return;
        }

        // Interesuje nas tylko moment zwijania wędki (REEL_IN, IN_GROUND, CAUGHT_ENTITY)
        if (e.getState() == PlayerFishEvent.State.REEL_IN ||
                e.getState() == PlayerFishEvent.State.IN_GROUND ||
                e.getState() == PlayerFishEvent.State.CAUGHT_ENTITY) {

            if (!canUse(p, item)) {
                e.setCancelled(true);
                return;
            }

            // Obliczamy wektor przyciągania do spławika
            Location hookLoc = e.getHook().getLocation();
            Vector direction = hookLoc.toVector().subtract(p.getLocation().toVector()).normalize();

            double pull = getItemData().pull_velocity;
            if (pull <= 0) pull = 2.3;

            // Wystrzelenie gracza w stronę spławika
            p.setVelocity(direction.multiply(pull));
            p.sendMessage("§bZostałeś przyciągnięty!");

            finishUse(p, item);
        }
    }
}