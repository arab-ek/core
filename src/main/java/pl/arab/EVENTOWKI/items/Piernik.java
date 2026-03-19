package pl.arab.EVENTOWKI.items;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action; // Importuj to!
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import pl.arab.EVENTOWKI.EventItem;

public class Piernik extends EventItem {

    public Piernik() { super("piernik"); }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player p = e.getPlayer();
        if (!canUse(p, e.getItem())) return;

        // --- NATYCHMIASTOWE DZIAŁANIE ---
        e.setCancelled(true); // Anulujemy animację jedzenia

        int level = getItemData().haste_level;
        int duration = getItemData().haste_duration;
        if (level <= 0) level = 10;
        if (duration <= 0) duration = 30;

        p.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, duration * 20, level - 1));

        finishUse(p, e.getItem());
    }
}