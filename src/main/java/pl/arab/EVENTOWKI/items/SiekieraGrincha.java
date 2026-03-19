package pl.arab.EVENTOWKI.items;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import pl.arab.EVENTOWKI.EventItem;

public class SiekieraGrincha extends EventItem {

    public SiekieraGrincha() {
        super("siekiera_grincha");
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;
        if (!(e.getEntity() instanceof Player)) return;

        Player attacker = (Player) e.getDamager();
        Player victim = (Player) e.getEntity();
        ItemStack item = attacker.getInventory().getItemInMainHand();

        if (!canUse(attacker, item)) return;

        // Pobieramy max HP ofiary
        AttributeInstance maxHpAttr = victim.getAttribute(Attribute.MAX_HEALTH);
        double maxHp = (maxHpAttr != null) ? maxHpAttr.getValue() : 20.0;

        // 30% jej życia
        double damageToDeal = maxHp * 0.30;

        // Uderzenie pioruna (tylko efekt wizualny, żeby nie podwajać obrażeń z Vanilli)
        victim.getWorld().strikeLightningEffect(victim.getLocation());

        // Zadajemy "czyste" obrażenia ignorując zbroję
        if (victim.getHealth() - damageToDeal <= 0) {
            victim.damage(10000.0); // Zabij natywnie (żeby był poprawny komunikat na czacie)
        } else {
            victim.setHealth(victim.getHealth() - damageToDeal);
        }

        attacker.sendMessage("§aPorażono gracza piorunem!");
        finishUse(attacker, item);
    }
}