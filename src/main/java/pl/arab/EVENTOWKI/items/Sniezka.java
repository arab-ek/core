package pl.arab.EVENTOWKI.items;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;
import pl.arab.EVENTOWKI.EventItem;

public class Sniezka extends EventItem {

    public Sniezka() {
        super("sniezka");
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();

        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        if (!canUse(p, e.getItem())) return;

        // Zatrzymujemy domyślny rzut (żeby nie zżerało itemu z ręki, zrobimy to sami)
        e.setCancelled(true);

        // Wystrzeliwujemy naszą customową śnieżkę
        Snowball snowball = p.launchProjectile(Snowball.class);

        // Oznaczamy śnieżkę naszym tagiem, żeby zwykłe śnieżki nie teleportowały graczy
        snowball.getPersistentDataContainer().set(this.getKey(), PersistentDataType.BYTE, (byte) 1);

        finishUse(p, e.getItem()); // To zabierze 1 śnieżkę (bo masz infinite: false) i da cooldown
    }

    @EventHandler
    public void onHit(ProjectileHitEvent e) {
        // Sprawdzamy czy to nasza śnieżka
        if (e.getEntity() instanceof Snowball && e.getEntity().getPersistentDataContainer().has(this.getKey(), PersistentDataType.BYTE)) {

            // Sprawdzamy czy trafiliśmy w gracza i czy strzelcem jest gracz
            if (e.getHitEntity() instanceof Player && e.getEntity().getShooter() instanceof Player) {

                Player victim = (Player) e.getHitEntity();
                Player shooter = (Player) e.getEntity().getShooter();

                // Pobieramy lokacje z zachowaniem kątów patrzenia kamery (Pitch, Yaw)
                Location victimLoc = victim.getLocation();
                Location shooterLoc = shooter.getLocation();

                // ZAMIANA
                shooter.teleport(victimLoc);
                victim.teleport(shooterLoc);

                // Trochę efektów dźwiękowych dla pikanterii
                shooter.playSound(shooter.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                victim.playSound(victim.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);

                shooter.sendMessage("§aZamieniłeś się miejscami z " + victim.getName() + "!");
                victim.sendMessage("§c" + shooter.getName() + " zamienił się z Tobą miejscami!");
            }
        }
    }
}