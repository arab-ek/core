package pl.arab.EVENTOWKI.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import pl.arab.Main;
import pl.arab.EVENTOWKI.EventItem;

public class EventowkiGui implements InventoryHolder {

    private final Inventory inventory;

    public EventowkiGui(Main plugin) {
        // Tworzymy GUI o wielkości 54 slotów (6 rzędów)
        this.inventory = Bukkit.createInventory(this, 54, ChatColor.DARK_AQUA + "Menu Eventówek");
        this.loadItems(plugin);
    }

    private void loadItems(Main plugin) {
        int slot = 0;

        // Przelatujemy przez wszystkie zarejestrowane ID eventówek
        for (String id : plugin.getEventowkiManager().getRegisteredIds()) {
            if (slot >= 54) break; // Zabezpieczenie przed wyjściem poza GUI

            EventItem eventItem = plugin.getEventowkiManager().getItem(id);
            if (eventItem != null) {
                // Generujemy item i wrzucamy do GUI
                inventory.setItem(slot, eventItem.getItem());
                slot++;
            }
        }
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    // Ta metoda jest kluczowa - dzięki niej rozpoznamy w evencie, że to nasze GUI!
    @Override
    public Inventory getInventory() {
        return inventory;
    }
}