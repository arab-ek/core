package pl.arab.EVENTOWKI.items;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action; // Importuj to!
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import pl.arab.EVENTOWKI.EventItem;

import java.util.Arrays;
import java.util.List;

public class CiepleMleko extends EventItem {

    private final List<PotionEffectType> badEffects = Arrays.asList(
            PotionEffectType.POISON, PotionEffectType.BLINDNESS, PotionEffectType.NAUSEA,
            PotionEffectType.HUNGER, PotionEffectType.WEAKNESS, PotionEffectType.SLOWNESS,
            PotionEffectType.MINING_FATIGUE, PotionEffectType.WITHER
    );

    public CiepleMleko() { super("cieple_mleko"); }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player p = e.getPlayer();
        if (!canUse(p, e.getItem())) return;

        // --- NATYCHMIASTOWE DZIAŁANIE ---
        e.setCancelled(true); // Anulujemy animację picia

        boolean removedAny = false;
        for (PotionEffect effect : p.getActivePotionEffects()) {
            if (badEffects.contains(effect.getType())) {
                p.removePotionEffect(effect.getType());
                removedAny = true;
            }
        }

        if (removedAny) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().negativeEffectsRemoved));
        }

        finishUse(p, e.getItem());
    }
}