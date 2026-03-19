package pl.arab.EVENTOWKI.items;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import pl.arab.EVENTOWKI.EventItem;

public class KrewWampira extends EventItem {

    public KrewWampira() {
        super("krew_wampira"); // ID z Twojego configu
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player p = e.getPlayer();
        if (!canUse(p, e.getItem())) return;

        e.setCancelled(true);

        // Zabezpieczenie na wypadek braku atrybutu HP (standardowo wynosi 20.0 = 10 serc)
        AttributeInstance maxHpAttr = p.getAttribute(Attribute.MAX_HEALTH);
        double maxHp = (maxHpAttr != null) ? maxHpAttr.getValue() : 20.0;

        // Sprawdzamy, czy gracz nie ma już pełnego zdrowia
        if (p.getHealth() >= maxHp) {
            p.sendMessage("§cMasz już pełne zdrowie!");
            return; // Zatrzymujemy akcję, NIE zabieramy itemu i NIE dajemy cooldownu
        }

        // Leczenie do maxa
        p.setHealth(maxHp);
        p.sendMessage("§c§lUleczono Cię do pełnego zdrowia!");

        // Zużywamy przedmiot i odpalamy Action Bar
        finishUse(p, e.getItem());
    }
}