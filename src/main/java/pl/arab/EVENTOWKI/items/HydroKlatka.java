package pl.arab.EVENTOWKI.items;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import pl.arab.EVENTOWKI.EventItem;

import java.util.ArrayList;
import java.util.List;

public class HydroKlatka extends EventItem {

    // Lista chroniąca bloki klatki przed zniszczeniem (NIGDZIE indziej)
    private static final List<Location> activeCageBlocks = new ArrayList<>();

    public HydroKlatka() {
        super("hydro_klatka");
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player p = e.getPlayer();
        if (!canUse(p, e.getItem())) return;

        e.setCancelled(true);

        // Wystrzeliwujemy pocisk i dodajemy mu nasz tag
        Fireball fireball = p.launchProjectile(Fireball.class);
        fireball.getPersistentDataContainer().set(this.getKey(), PersistentDataType.BYTE, (byte) 1);
        fireball.setYield(0.0F); // Wyłączamy vanilla wybuch
        fireball.setIsIncendiary(false);

        finishUse(p, e.getItem());
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        if (e.getEntity() instanceof Fireball && e.getEntity().getPersistentDataContainer().has(this.getKey(), PersistentDataType.BYTE)) {

            Location hitLoc = e.getHitEntity() != null ? e.getHitEntity().getLocation() : (e.getHitBlock() != null ? e.getHitBlock().getLocation() : null);
            if (hitLoc == null) return;

            int radius = getItemData().radius;
            if (radius <= 0) radius = 10;

            int duration = getItemData().duration;
            if (duration <= 0) duration = 15;

            spawnCage(hitLoc, radius, duration);
        }
    }

    // Blokujemy BEZWZGLĘDNIE niszczenie bloków klatki przez graczy
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (activeCageBlocks.contains(e.getBlock().getLocation())) {
            e.setCancelled(true);
            e.getPlayer().sendMessage("§cTo klatka z eventówki! Nie możesz tego zniszczyć.");
        }
    }

    private void spawnCage(Location center, int radius, int duration) {
        List<Location> currentCageBlocks = new ArrayList<>();
        List<Material> oldMaterials = new ArrayList<>();

        // 1. Budujemy Klatkę (Sphere)
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        int rSq = radius * radius;
        int innerRSq = (radius - 1) * (radius - 1);

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    int distSq = x * x + y * y + z * z;

                    if (distSq <= rSq && distSq > innerRSq) { // Tylko zewnetrzna powloka
                        Block b = center.getWorld().getBlockAt(cx + x, cy + y, cz + z);

                        // Nie zamieniamy Bedrocka i innych skrzynek
                        if (b.getType() != Material.BEDROCK && !b.getType().name().contains("CHEST")) {
                            currentCageBlocks.add(b.getLocation());
                            oldMaterials.add(b.getType()); // Zapisujemy, co tu było

                            // 50/50 szkło albo terakota dla efektu hydro
                            if (Math.random() > 0.5) {
                                b.setType(Material.BLUE_GLAZED_TERRACOTTA);
                            } else {
                                b.setType(Material.LIGHT_BLUE_STAINED_GLASS);
                            }
                        }
                    }
                }
            }
        }

        // Dodajemy bloki do globalnej blokady
        activeCageBlocks.addAll(currentCageBlocks);
        center.getWorld().playSound(center, Sound.BLOCK_WATER_AMBIENT, 2.0F, 1.0F);

        // 2. Po odczekaniu czasu - przywracamy stare bloki!
        new BukkitRunnable() {
            @Override
            public void run() {
                for (int i = 0; i < currentCageBlocks.size(); i++) {
                    Block b = currentCageBlocks.get(i).getBlock();
                    b.setType(oldMaterials.get(i));

                    if (Math.random() > 0.9) {
                        b.getWorld().spawnParticle(Particle.DRIPPING_WATER, b.getLocation(), 5);
                    }
                }

                // Usuwamy z blokady
                activeCageBlocks.removeAll(currentCageBlocks);
                center.getWorld().playSound(center, Sound.ENTITY_BOAT_PADDLE_WATER, 1.0F, 1.0F);
            }
        }.runTaskLater(plugin, duration * 20L); // duration w sekundach * 20 ticków
    }
}