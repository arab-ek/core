package pl.arab.EVENTOWKI.items;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;
import pl.arab.EVENTOWKI.EventItem;
import pl.arab.EVENTOWKI.utils.RegionUtils;

public class BombardaMaxima extends EventItem {

    public BombardaMaxima() {
        super("bombarda_maxina");
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();

        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!canUse(p, e.getItem())) return;

        e.setCancelled(true);

        Fireball projectile = p.launchProjectile(Fireball.class);
        projectile.setVelocity(p.getLocation().getDirection().multiply(2.0));
        projectile.getPersistentDataContainer().set(this.getKey(), PersistentDataType.BYTE, (byte) 1);

        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.0f);
        finishUse(p, e.getItem());
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        if (e.getEntity().getPersistentDataContainer().has(this.getKey(), PersistentDataType.BYTE)) {

            Location hitLoc = e.getEntity().getLocation();
            int radius = (int) getItemData().explosion_power;

            hitLoc.getWorld().spawnParticle(Particle.EXPLOSION, hitLoc, 3);
            hitLoc.getWorld().playSound(hitLoc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 1.0f);

            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {

                        Location blockLoc = hitLoc.clone().add(x, y, z);

                        if (hitLoc.distance(blockLoc) <= radius) {
                            Block block = blockLoc.getBlock();

                            // SPRAWDZENIE 1: Czy blok nie leży na zablokowanym regionie?
                            if (RegionUtils.isInBlockedRegion(blockLoc, getItemData().blocked_regions)) {
                                continue; // Jeśli tak, pomijamy ten blok!
                            }

                            // SPRAWDZENIE 2: Czy to nie jest powietrze albo bedrock?
                            if (block.getType() != Material.AIR && block.getType() != Material.BEDROCK) {
                                block.setType(Material.AIR);
                            }
                        }
                    }
                }
            }
        }
    }
}