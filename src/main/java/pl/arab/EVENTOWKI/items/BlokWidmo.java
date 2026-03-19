package pl.arab.EVENTOWKI.items;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import pl.arab.EVENTOWKI.config.ItemData;
import pl.arab.EVENTOWKI.managers.CooldownManager;
import pl.arab.EVENTOWKI.utils.ChatUtils;
import pl.arab.EVENTOWKI.utils.RegionUtils;
import pl.arab.Main;
import pl.arab.EVENTOWKI.EventItem;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BlokWidmo extends EventItem implements Listener {

    private static final Map<Location, Long> activeBlocks = new ConcurrentHashMap<>();
    private static final Map<UUID, BossBar> activeVictims = new ConcurrentHashMap<>();

    // ZMIANA 1.21+: Zamiast UUID używamy NamespacedKey!
    private static final NamespacedKey HEALTH_REDUCTION_KEY = new NamespacedKey(Main.getMain(), "blok_widmo_reduction");

    public BlokWidmo() {
        super("blok_widmo");
        startAuraTask();
    }

    @Override
    public ItemStack getItem() { return super.getItem(); }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        ItemStack itemHand = e.getItemInHand();

        if (!itemHand.hasItemMeta() ||
                !itemHand.getItemMeta().getPersistentDataContainer().has(this.key, PersistentDataType.BYTE)) {
            return;
        }

        ItemData data = getItemData();
        String titleX = plugin.getMessages().errorTitle;

        if (data.blocked_worlds.contains(p.getWorld().getName())) {
            ChatUtils.sendTitle(p, titleX, data.name + " &c- " + plugin.getMessages().blockedWorld, 10, 40, 10);
            e.setCancelled(true);
            return;
        }

        if (RegionUtils.isInBlockedRegion(p, data.blocked_regions)) {
            ChatUtils.sendTitle(p, titleX, data.name + " &c- " + plugin.getMessages().blockedRegion, 10, 40, 10);
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

        Location loc = e.getBlock().getLocation();
        int durSeconds = data.duration > 0 ? data.duration : 60;

        activeBlocks.put(loc, System.currentTimeMillis() + (durSeconds * 1000L));

        loc.getWorld().playSound(loc, Sound.BLOCK_END_PORTAL_SPAWN, 1.0F, 0.5F);
        loc.getWorld().spawnParticle(Particle.LARGE_SMOKE, loc.clone().add(0.5, 0.5, 0.5), 50, 0.5, 0.5, 0.5, 0.1);

        finishUse(p, itemHand);
    }

    private void startAuraTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                double radius = getItemData().proximity_radius > 0 ? getItemData().proximity_radius : 5.0;
                double maxHealthLimit = getItemData().health_limit > 0 ? getItemData().health_limit : 10.0;
                double radiusSq = radius * radius;

                Iterator<Map.Entry<Location, Long>> blockIt = activeBlocks.entrySet().iterator();
                while (blockIt.hasNext()) {
                    Map.Entry<Location, Long> entry = blockIt.next();
                    if (entry.getValue() < now) {
                        entry.getKey().getBlock().setType(Material.AIR);
                        entry.getKey().getWorld().playSound(entry.getKey(), Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 1.0f);
                        blockIt.remove();
                    } else {
                        if (Math.random() > 0.5) {
                            entry.getKey().getWorld().spawnParticle(Particle.PORTAL, entry.getKey().clone().add(0.5, 1.0, 0.5), 10, 0.5, 0.5, 0.5, 0.5);
                        }
                    }
                }

                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getGameMode() == org.bukkit.GameMode.SPECTATOR || p.getGameMode() == org.bukkit.GameMode.CREATIVE) continue;

                    boolean inAura = false;
                    long maxExpirationTime = 0;

                    for (Map.Entry<Location, Long> entry : activeBlocks.entrySet()) {
                        if (p.getWorld().equals(entry.getKey().getWorld()) && p.getLocation().distanceSquared(entry.getKey().clone().add(0.5,0.5,0.5)) <= radiusSq) {
                            inAura = true;
                            if (entry.getValue() > maxExpirationTime) {
                                maxExpirationTime = entry.getValue();
                            }
                        }
                    }

                    AttributeInstance attr = p.getAttribute(Attribute.MAX_HEALTH);
                    if (attr == null) continue;

                    if (inAura) {
                        if (!hasModifier(attr)) {
                            double baseVal = attr.getBaseValue();
                            if (baseVal > maxHealthLimit) {
                                double reduction = maxHealthLimit - baseVal;

                                // ZMIANA 1.21+: Nowy konstruktor modyfikatora
                                attr.addModifier(new AttributeModifier(HEALTH_REDUCTION_KEY, reduction, AttributeModifier.Operation.ADD_NUMBER));

                                if (p.getHealth() > maxHealthLimit) p.setHealth(maxHealthLimit);
                                ChatUtils.sendTitle(p, plugin.getMessages().errorTitle, "§c§lWkroczyłeś w strefę Bloku Widmo!", 10, 40, 10);
                            }
                        }

                        long timeLeft = (maxExpirationTime - now) / 1000L;
                        String timeStr = String.format("%02dm %02ds", timeLeft / 60, timeLeft % 60);
                        String barTitle = ChatUtils.clean(plugin.getMessages().bossbarWidmo.replace("{TIME}", timeStr));

                        BossBar bar = activeVictims.computeIfAbsent(p.getUniqueId(), k -> {
                            BossBar newBar = Bukkit.createBossBar(barTitle, BarColor.PURPLE, BarStyle.SOLID);
                            newBar.addPlayer(p);
                            return newBar;
                        });
                        bar.setTitle(barTitle);

                    } else {
                        if (hasModifier(attr)) {
                            removeModifier(attr);
                            ChatUtils.sendTitle(p, plugin.getMessages().errorTitle, "§a§lOpuściłeś strefę Bloku Widmo!", 10, 40, 10);
                        }

                        BossBar bar = activeVictims.remove(p.getUniqueId());
                        if (bar != null) {
                            bar.removeAll();
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 10L, 10L);
    }

    private boolean hasModifier(AttributeInstance attr) {
        for (AttributeModifier mod : attr.getModifiers()) {
            // ZMIANA 1.21+: getKey() zamiast getUniqueId()
            if (mod.getKey().equals(HEALTH_REDUCTION_KEY)) return true;
        }
        return false;
    }

    private void removeModifier(AttributeInstance attr) {
        for (AttributeModifier mod : attr.getModifiers()) {
            // ZMIANA 1.21+: getKey() zamiast getUniqueId()
            if (mod.getKey().equals(HEALTH_REDUCTION_KEY)) {
                attr.removeModifier(mod);
                break;
            }
        }
    }
}