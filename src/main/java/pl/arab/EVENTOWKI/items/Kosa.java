package pl.arab.EVENTOWKI.items;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import pl.arab.EVENTOWKI.EventItem;

public class Kosa extends EventItem {

    public Kosa() {
        super("kosa"); // Zgodne z configiem
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;
        if (!(e.getEntity() instanceof Player)) return;

        Player attacker = (Player) e.getDamager();
        Player victim = (Player) e.getEntity();
        ItemStack item = attacker.getInventory().getItemInMainHand();

        if (!canUse(attacker, item)) return;

        // Pobieramy czas trwania z configu
        int durationSeconds = getItemData().effects_duration;
        if (durationSeconds <= 0) durationSeconds = 10;

        // Nakładamy Blindness (Ślepota)
        victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, durationSeconds * 20, 0));

        attacker.sendMessage("§aPrzestraszyłeś i oślepiłeś gracza!");
        victim.sendMessage("§cZostałeś uderzony Kosą! Tracisz widoczność!");

        finishUse(attacker, item);
    }
}