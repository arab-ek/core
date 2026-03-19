package pl.arab.EVENTOWKI;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import pl.arab.EVENTOWKI.config.ItemData;
import pl.arab.EVENTOWKI.managers.CooldownManager;
import pl.arab.EVENTOWKI.utils.ChatUtils;
import pl.arab.EVENTOWKI.utils.RegionUtils;
import pl.arab.Main;

import java.util.ArrayList;
import java.util.List;

public abstract class EventItem implements Listener {

    protected final Main plugin = Main.getMain();
    protected final NamespacedKey key;
    private final String id;

    public EventItem(String id) {
        this.id = id.toLowerCase();
        this.key = new NamespacedKey(plugin, this.id);

        // Jeśli przedmiotu nie ma w configu, stwórzmy pustą templatkę, by uniknąć NullPointerException
        if (!plugin.getPluginConfig().meta.containsKey(this.id)) {
            plugin.getPluginConfig().meta.put(this.id, new ItemData());
            plugin.getPluginConfig().save();
        }
    }

    public String getId() {
        return id;
    }

    public NamespacedKey getKey() {
        return key;
    }

    public ItemData getItemData() {
        return plugin.getPluginConfig().meta.get(id);
    }

    // Budowanie przedmiotu na bazie tego, co jest w pliku konfiguracyjnym
    public ItemStack getItem() {
        ItemData data = getItemData();
        ItemStack item = new ItemStack(data.material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', data.name));

            List<String> coloredLore = new ArrayList<>();
            for (String line : data.lore) {
                coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            meta.setLore(coloredLore);

            if (data.custom_model_data > 0) {
                meta.setCustomModelData(data.custom_model_data);
            }

            meta.getPersistentDataContainer().set(this.key, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    // Główna metoda zabezpieczająca dla każdego Listenera
    public boolean canUse(Player p, ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        if (!item.getItemMeta().getPersistentDataContainer().has(this.key, PersistentDataType.BYTE)) return false;

        ItemData data = getItemData();
        String titleX = plugin.getMessages().errorTitle;

        // 1. Zablokowane Światy
        if (data.blocked_worlds.contains(p.getWorld().getName())) {
            ChatUtils.sendTitle(p, titleX, plugin.getMessages().blockedWorld, 10, 40, 10);
            return false;
        }

        // 2. Zablokowane Regiony
        if (RegionUtils.isInBlockedRegion(p, data.blocked_regions)) {
            ChatUtils.sendTitle(p, titleX, plugin.getMessages().blockedRegion, 10, 40, 10);
            return false;
        }

        // 3. Cooldown
        if (CooldownManager.isOnCooldown(p, id)) {
            String cleanName = ChatUtils.clean(data.name);
            String msg = plugin.getMessages().onCooldown
                    .replace("{ITEM}", cleanName)
                    .replace("{TIME}", CooldownManager.getFormattedTimeLeft(p, id));
            ChatUtils.sendTitle(p, titleX, msg, 10, 40, 10);
            return false;
        }

        return true;
    }

    // Wywołaj to PO UDANYM użyciu przedmiotu
    public void finishUse(Player p, ItemStack item) {
        ItemData data = getItemData();

        // Ustawienie cooldownu
        if (data.cooldown > 0) {
            CooldownManager.setCooldown(p, id, data.cooldown);
        }

        // Zużycie przedmiotu (jednorazowego)
        if (!data.infinite) {
            item.setAmount(item.getAmount() - 1);
            org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, p::updateInventory, 1L);
        }

        // --- WYSYŁANIE TITLE PO UŻYCIU ---
        String itemName = data.name; // Pobiera kolorową nazwę z configu
        String subtitle = plugin.getMessages().itemUsedSubtitle; // "Pomyślnie użyto przedmiotu!"
        ChatUtils.sendTitle(p, itemName, subtitle, 10, 40, 10);
    }
}