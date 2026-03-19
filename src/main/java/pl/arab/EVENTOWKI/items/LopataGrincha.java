package pl.arab.EVENTOWKI.items;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import pl.arab.EVENTOWKI.EventItem;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class LopataGrincha extends EventItem {

    private final Map<UUID, Long> frozenPlayers = new HashMap<>();
    private final Random random = new Random();

    public LopataGrincha() {
        super("lopata_grincha");
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;
        if (!(e.getEntity() instanceof Player)) return;

        Player attacker = (Player) e.getDamager();
        Player victim = (Player) e.getEntity();
        ItemStack item = attacker.getInventory().getItemInMainHand();

        if (!canUse(attacker, item)) return;

        int freezeTime = getItemData().freeze_seconds;
        if (freezeTime <= 0) freezeTime = 3;

        // 1. Zamrażamy gracza
        frozenPlayers.put(victim.getUniqueId(), System.currentTimeMillis() + (freezeTime * 1000L));

        // 2. Obracamy mu kamerę! (Yaw od 0 do 360, Pitch od -90 do 90)
        Location loc = victim.getLocation();
        loc.setYaw(random.nextFloat() * 360.0f);
        loc.setPitch((random.nextFloat() * 180.0f) - 90.0f);
        victim.teleport(loc);

        attacker.sendMessage("§aOgłuszyłeś i obróciłeś przeciwnika!");
        victim.sendMessage("§cZostałeś uderzony Łopatą Grincha!");

        finishUse(attacker, item);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (frozenPlayers.containsKey(p.getUniqueId())) {
            if (frozenPlayers.get(p.getUniqueId()) > System.currentTimeMillis()) {
                Location from = e.getFrom();
                Location to = e.getTo();
                // Blokujemy tylko ruch X, Y, Z. Pozwalamy mu znów ruszać kamerą (ale na start dostał losową)
                if (to != null && (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ())) {
                    Location resetLoc = from.clone();
                    resetLoc.setYaw(to.getYaw());
                    resetLoc.setPitch(to.getPitch());
                    e.setTo(resetLoc);
                }
            } else {
                frozenPlayers.remove(p.getUniqueId());
            }
        }
    }
}