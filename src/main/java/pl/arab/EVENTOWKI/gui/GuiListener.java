package pl.arab.EVENTOWKI.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class GuiListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        // Sprawdzamy czy to nasze GUI (używając InventoryHolder)
        if (e.getInventory().getHolder() instanceof EventowkiGui) {

            // Bezwzględnie blokujemy przesuwanie/wyciąganie itemów
            e.setCancelled(true);

            if (e.getCurrentItem() == null) return;

            // Jeśli ktoś uderzył w przedmiot - dajemy mu jego kopię
            Player p = (Player) e.getWhoClicked();
            ItemStack clickedItem = e.getCurrentItem().clone();

            p.getInventory().addItem(clickedItem);
            p.sendMessage("§aPomyślnie wyciągnięto przedmiot z GUI!");
        }
    }
}