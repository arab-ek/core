package pl.arab.EVENTOWKI.items;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import pl.arab.EVENTOWKI.EventItem;

public class KoronaAnarchii extends EventItem {

    public KoronaAnarchii() {
        super("korona_anarchii");
        startPassiveEffectsTask();
    }

    // Nadpisujemy budowanie przedmiotu, aby nałożyć Unbreakable (Niezniszczalność)
    @Override
    public ItemStack getItem() {
        ItemStack item = super.getItem(); // Pobieramy wygenerowany item z klasy bazowej
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setUnbreakable(true); // Ustawiamy na stałe niezniszczalny
            item.setItemMeta(meta);
        }
        return item;
    }

    private void startPassiveEffectsTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    ItemStack helmet = p.getInventory().getHelmet();

                    if (helmet != null && helmet.hasItemMeta() &&
                            helmet.getItemMeta().getPersistentDataContainer().has(getKey(), PersistentDataType.BYTE)) {

                        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 1, false, false, true));
                        p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 40, 0, false, false, true));
                        p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 40, 1, false, false, true));
                        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 40, 2, false, false, true));
                        p.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 40, 0, false, false, true));
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }
}