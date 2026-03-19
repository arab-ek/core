package pl.arab.EVENTOWKI.items;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import pl.arab.EVENTOWKI.EventItem;

public class Rozga extends EventItem {

    public Rozga() {
        super("rozga"); // ID z Twojego configu
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;
        if (!(e.getEntity() instanceof Player)) return;

        Player attacker = (Player) e.getDamager();
        Player victim = (Player) e.getEntity();
        ItemStack item = attacker.getInventory().getItemInMainHand();

        if (!canUse(attacker, item)) return;

        // Pobieramy siłę odrzutu z configu
        double power = getItemData().knockback_power;
        if (power <= 0) power = 4.0;

        // Obliczamy wektor odepchnięcia
        Vector direction = victim.getLocation().toVector().subtract(attacker.getLocation().toVector()).normalize();
        direction.setY(0.4); // Lekko podbijamy gracza do góry dla lepszego efektu

        victim.setVelocity(direction.multiply(power));

        attacker.sendMessage("§aOdrzuciłeś gracza z ogromną siłą!");

        finishUse(attacker, item);
    }
}