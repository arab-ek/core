package pl.arab.EVENTOWKI.items;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import pl.arab.EVENTOWKI.EventItem;
import pl.arab.EVENTOWKI.config.ItemData;
import pl.arab.EVENTOWKI.managers.CooldownManager;
import pl.arab.EVENTOWKI.utils.ChatUtils;
import pl.arab.EVENTOWKI.utils.RegionUtils;

// Musimy zaimplementować Listener ręcznie, bo canUse() bazuje na PlayerInteractEvent
public class CudownaLatarnia extends EventItem implements Listener {

    public CudownaLatarnia() { super("cudowna_latarnia"); }

    // Ta klasa nadrzędna wymaga getItem(), po prostu zwróć pusty item
    @Override public ItemStack getItem() { return super.getItem(); }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        ItemStack itemHand = e.getItemInHand();

        // 1. Sprawdzamy PDC tak jak w EventItem, żeby nie reagować na zwykły Beacon
        if (!itemHand.hasItemMeta() ||
                !itemHand.getItemMeta().getPersistentDataContainer().has(this.key, PersistentDataType.BYTE)) {
            return;
        }

        // 2. Ręczne sprawdzenie cooldownu i regionu
        ItemData data = getItemData();
        String titleX = plugin.getMessages().errorTitle;

        if (data.blocked_worlds.contains(p.getWorld().getName())) {
            ChatUtils.sendTitle(p, titleX, plugin.getMessages().blockedWorld, 10, 40, 10);
            e.setCancelled(true); // Anuluj postawienie
            return;
        }

        if (RegionUtils.isInBlockedRegion(p, data.blocked_regions)) {
            ChatUtils.sendTitle(p, titleX, plugin.getMessages().blockedRegion, 10, 40, 10);
            e.setCancelled(true);
            return;
        }

        if (CooldownManager.isOnCooldown(p, getId())) {
            String cleanName = ChatUtils.clean(data.name);
            String msg = plugin.getMessages().onCooldown
                    .replace("{ITEM}", cleanName)
                    .replace("{TIME}", CooldownManager.getFormattedTimeLeft(p, getId()));
            ChatUtils.sendTitle(p, titleX, msg, 10, 40, 10);
            e.setCancelled(true);
            return;
        }

        // --- NAKŁADANIE EFEKTÓW ---
        int regen = getItemData().lantern_regen_duration;
        int absorption = getItemData().lantern_absorption_duration;
        int strength = getItemData().lantern_strength_duration;

        p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, regen * 20, 0));
        p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, absorption * 20, 0));
        p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, strength * 20, 1)); // Siła II

        // Używamy natywnej metody, która ustawi cooldown i wyśle Title z naprawionym ChatUtils
        finishUse(p, itemHand);
    }
}