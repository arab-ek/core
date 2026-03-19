package pl.arab.EVENTOWKI.items;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import pl.arab.EVENTOWKI.EventItem;

public class ZajeczyMiecz extends EventItem {

    public ZajeczyMiecz() {
        super("zajeczy_miecz");
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;
        if (!(e.getEntity() instanceof Player)) return;

        Player attacker = (Player) e.getDamager();
        Player victim = (Player) e.getEntity();
        ItemStack item = attacker.getInventory().getItemInMainHand();

        if (!canUse(attacker, item)) return;

        int duration = getItemData().jump_block_duration;
        if (duration <= 0) duration = 4;

        // Poziom 250 blokuje całkowicie możliwość skoku (Spigot mechanics)
        victim.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, duration * 20, 250, false, false, true));

        attacker.sendMessage("§aZablokowałeś skakanie przeciwnikowi!");
        victim.sendMessage("§c" + attacker.getName() + " zablokował Ci skakanie!");

        finishUse(attacker, item);
    }
}