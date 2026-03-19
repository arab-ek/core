package pl.arab.EVENTOWKI.items;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import pl.arab.EVENTOWKI.EventItem;

public class SplesnialaKanapka extends EventItem {

    public SplesnialaKanapka() {
        super("splesniala_kanapka");
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;
        if (!(e.getEntity() instanceof Player)) return;

        Player attacker = (Player) e.getDamager();
        Player victim = (Player) e.getEntity();
        ItemStack item = attacker.getInventory().getItemInMainHand();

        if (!canUse(attacker, item)) return;

        int duration = getItemData().disease_duration;
        int level = getItemData().disease_level;

        if (duration <= 0) duration = 10;
        if (level <= 0) level = 2;

        // Nakładamy zatrucie (level - 1, bo 0 to poziom I w kodzie)
        victim.addPotionEffect(new PotionEffect(PotionEffectType.POISON, duration * 20, level - 1));

        attacker.sendMessage("§aZatrułeś gracza Spleśniałą Kanapką!");
        finishUse(attacker, item);
    }
}