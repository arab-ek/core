package pl.arab.EVENTOWKI.items;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import pl.arab.EVENTOWKI.EventItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KostkaRubika extends EventItem {

    public KostkaRubika() {
        super("kostka_rubika");
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;
        if (!(e.getEntity() instanceof Player)) return;

        Player attacker = (Player) e.getDamager();
        Player victim = (Player) e.getEntity();
        ItemStack item = attacker.getInventory().getItemInMainHand();

        if (!canUse(attacker, item)) return;

        // Zbieramy przedmioty z pasków 0-8 (Hotbar)
        List<ItemStack> hotbarItems = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            hotbarItems.add(victim.getInventory().getItem(i));
        }

        // Mieszamy je jak Kostkę Rubika!
        Collections.shuffle(hotbarItems);

        // Zwracamy wymieszane przedmioty na miejsce
        for (int i = 0; i < 9; i++) {
            victim.getInventory().setItem(i, hotbarItems.get(i));
        }

        attacker.sendMessage("§eWymieszałeś przedmioty w pasku gracza!");
        victim.sendMessage("§cTwoje przedmioty na pasku zostały przetasowane!");

        finishUse(attacker, item);
    }
}