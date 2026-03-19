package pl.arab.EVENTOWKI.items;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import pl.arab.EVENTOWKI.EventItem;

public class WataCukrowa extends EventItem {

    public WataCukrowa() { super("wata_cukrowa"); }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player p = e.getPlayer();
        if (!canUse(p, e.getItem())) return;

        EntityEquipment equipment = p.getEquipment();
        if (equipment == null) return;

        ItemStack[] armor = equipment.getArmorContents();
        boolean canRepair = true;

        // 1. Sprawdzamy czy ma cały set założony
        for (ItemStack armorItem : armor) {
            if (armorItem == null || armorItem.getType().isAir()) {
                canRepair = false; // Brak jakiejś części zbroi
                break;
            }
        }

        // 2. Jeśli ma cały set, sprawdzamy czy któraś część jest zniszczona
        if (canRepair) {
            boolean foundDamaged = false;
            for (ItemStack armorItem : armor) {
                if (armorItem.getItemMeta() instanceof Damageable) {
                    Damageable damageable = (Damageable) armorItem.getItemMeta();
                    if (damageable.getDamage() > 0) {
                        foundDamaged = true; // Znalazł zniszczoną część
                        break;
                    }
                }
            }
            if (!foundDamaged) canRepair = false; // Cały set jest na fulla
        }

        if (!canRepair) {
            // Wiadomość o błędzie (Title lub chat)
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().armorNotSetOrFull));
            return; // Zatrzymujemy działanie
        }

        // 3. Naprawiamy caly set
        for (ItemStack armorItem : armor) {
            if (armorItem.getItemMeta() instanceof Damageable) {
                Damageable damageable = (Damageable) armorItem.getItemMeta();
                damageable.setDamage(0); // Pełna naprawa
                armorItem.setItemMeta((ItemMeta) damageable);
            }
        }
        equipment.setArmorContents(armor); // Zwracamy zbroję do ekwipunku

        p.sendMessage("§aTwój set zbroi został pomyślnie naprawiony!");

        // Zużywamy przedmiot (jednorazowa, bo w configu infinite: false)
        finishUse(p, e.getItem());
    }
}