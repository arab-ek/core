package pl.arab.EVENTOWKI.items;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;
import pl.arab.EVENTOWKI.EventItem;

public class Dynamit extends EventItem {

    public Dynamit() {
        super("dynamit");
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getItem() == null || !e.getItem().hasItemMeta()) return;

        // Sprawdzamy czy gracz w ogóle trzyma Dynamit, zanim zablokujemy event
        if (!e.getItem().getItemMeta().getPersistentDataContainer().has(this.getKey(), PersistentDataType.BYTE)) return;

        // BEZWZGLĘDNIE BLOKUJEMY POSTAWIENIE PRZEDMIOTU JAKO BLOKU! (cofa do eq)
        e.setCancelled(true);

        Player p = e.getPlayer();
        if (!canUse(p, e.getItem())) return;

        Block clickedBlock = e.getClickedBlock();

        if (clickedBlock != null && clickedBlock.getType() == Material.BEDROCK) {

            // Ochrona przed zrobieniem dziury w podłodze świata (Y <= -64 dla 1.18+, Y <= 0 dla starszych)
            if (clickedBlock.getY() <= clickedBlock.getWorld().getMinHeight()) {
                p.sendMessage("§cNie możesz zniszczyć ostatecznej warstwy bedrocka chroniącej przed pustką!");
                return;
            }

            clickedBlock.setType(Material.AIR);
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().bedrockDestroyed));

            finishUse(p, e.getItem()); // Zabierze dynamit z łapki
        } else {
            p.sendMessage("§cDynamit można postawić tylko na skale macierzystej (bedrock)!");
        }
    }
}