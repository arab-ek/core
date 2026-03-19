package pl.arab.EVENTOWKI.items;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
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
import pl.arab.EVENTOWKI.EventItem;
import pl.arab.EVENTOWKI.config.ItemData;
import pl.arab.EVENTOWKI.managers.CooldownManager;
import pl.arab.EVENTOWKI.utils.ChatUtils;
import pl.arab.EVENTOWKI.utils.RegionUtils;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BlokWidmo extends EventItem implements Listener {

    // Trzymamy aktywne bloki: Lokacja -> Czas wygaśnięcia w ms
    private static final Map<Location, Long> activeBlocks = new ConcurrentHashMap<>();

    // Trzymamy graczy będących pod wpływem bloku: UUID -> BossBar
    private static final Map<UUID, BossBar> activeVictims = new ConcurrentHashMap<>();

    // Unikalne UUID dla naszego modyfikatora życia (aby móc go potem usunąć)
    private static final UUID HEALTH_REDUCTION_UUID = UUID.fromString("6a117d91-bc1e-450f-a789-982348567abc");

    public BlokWidmo() {
        super("blok_widmo");
        startAuraTask();
    }

    // Aby ta klasa nie wyskakiwała w canUse(PlayerInteractEvent), upewnijmy się, że używamy BlockPlaceEvent
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

        // --- UDAŁO SIĘ POSTAWIĆ BLOK ---
        Location loc = e.getBlock().getLocation();
        int durSeconds = data.duration > 0 ? data.duration : 60;

        // Zapisujemy blok
        activeBlocks.put(loc, System.currentTimeMillis() + (durSeconds * 1000L));

        loc.getWorld().playSound(loc, Sound.BLOCK_END_PORTAL_SPAWN, 1.0F, 0.5F);
        loc.getWorld().spawnParticle(Particle.LARGE_SMOKE, loc.clone().add(0.5, 0.5, 0.5), 50, 0.5, 0.5, 0.5, 0.1);

        finishUse(p, itemHand);
    }

    // Task, który co pół sekundy sprawdza aurę wokół aktywnych bloków
    private void startAuraTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                double radius = getItemData().proximity_radius > 0 ? getItemData().proximity_radius : 5.0;
                double maxHealthLimit = getItemData().health_limit > 0 ? getItemData().health_limit : 10.0;
                double radiusSq = radius * radius;

                // 1. Czyszczenie wygasłych bloków
                Iterator<Map.Entry<Location, Long>> blockIt = activeBlocks.entrySet().iterator();
                while (blockIt.hasNext()) {
                    Map.Entry<Location, Long> entry = blockIt.next();
                    if (entry.getValue() < now) {
                        entry.getKey().getBlock().setType(Material.AIR); // Usuwamy fizyczny blok
                        entry.getKey().getWorld().playSound(entry.getKey(), Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 1.0f);
                        blockIt.remove();
                    } else {
                        // Efekt wizualny aury
                        if (Math.random() > 0.5) {
                            entry.getKey().getWorld().spawnParticle(Particle.PORTAL, entry.getKey().clone().add(0.5, 1.0, 0.5), 10, 0.5, 0.5, 0.5, 0.5);
                        }
                    }
                }

                // 2. Aplikowanie/Usuwanie modyfikatora u graczy
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getGameMode() == org.bukkit.GameMode.SPECTATOR || p.getGameMode() == org.bukkit.GameMode.CREATIVE) continue;

                    boolean inAura = false;
                    long maxExpirationTime = 0;

                    // Sprawdzamy czy gracz jest w zasięgu KTÓREGOŚ z aktywnych bloków widmo
                    for (Map.Entry<Location, Long> entry : activeBlocks.entrySet()) {
                        if (p.getWorld().equals(entry.getKey().getWorld()) && p.getLocation().distanceSquared(entry.getKey().clone().add(0.5,0.5,0.5)) <= radiusSq) {
                            inAura = true;
                            // Znajdujemy blok, który będzie działał najdłużej (do BossBara)
                            if (entry.getValue() > maxExpirationTime) {
                                maxExpirationTime = entry.getValue();
                            }
                        }
                    }

                    AttributeInstance attr = p.getAttribute(Attribute.MAX_HEALTH);
                    if (attr == null) continue;

                    if (inAura) {
                        // Nadajemy modyfikator (jeśli jeszcze nie ma)
                        if (!hasModifier(attr)) {
                            // Obliczamy ile serc trzeba odjąć, żeby zrównać go do limitu
                            double baseVal = attr.getBaseValue();
                            if (baseVal > maxHealthLimit) {
                                double reduction = maxHealthLimit - baseVal; // wynik ujemny
                                attr.addModifier(new AttributeModifier(HEALTH_REDUCTION_UUID, "blok_widmo", reduction, AttributeModifier.Operation.ADD_NUMBER));

                                // Leczenie w dół (żeby nie miał pustych szarych serduszek poza paskiem)
                                if (p.getHealth() > maxHealthLimit) p.setHealth(maxHealthLimit);

                                ChatUtils.sendTitle(p, plugin.getMessages().errorTitle, "§c§lWkroczyłeś w strefę Bloku Widmo!", 10, 40, 10);
                            }
                        }

                        // Obsługa BossBara
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
                        // Gracz wyszedł poza strefę lub blok zniknął - zdejmujemy modyfikator!
                        if (hasModifier(attr)) {
                            removeModifier(attr);
                            ChatUtils.sendTitle(p, plugin.getMessages().errorTitle, "§a§lOpuściłeś strefę Bloku Widmo!", 10, 40, 10);
                        }

                        // Usuwamy BossBar
                        BossBar bar = activeVictims.remove(p.getUniqueId());
                        if (bar != null) {
                            bar.removeAll();
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 10L, 10L); // Odpalaj co pół sekundy
    }

    // Pomocnicza metoda sprawdzająca czy gracz ma nasz debuff
    private boolean hasModifier(AttributeInstance attr) {
        for (AttributeModifier mod : attr.getModifiers()) {
            if (mod.getUniqueId().equals(HEALTH_REDUCTION_UUID)) return true;
        }
        return false;
    }

    // Pomocnicza metoda usuwająca debuff
    private void removeModifier(AttributeInstance attr) {
        for (AttributeModifier mod : attr.getModifiers()) {
            if (mod.getUniqueId().equals(HEALTH_REDUCTION_UUID)) {
                attr.removeModifier(mod);
                break;
            }
        }
    }
}