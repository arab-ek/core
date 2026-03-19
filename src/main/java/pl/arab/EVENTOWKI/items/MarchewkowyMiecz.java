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
import java.util.UUID;

public class MarchewkowyMiecz extends EventItem {

    // Mapa pamiętająca kogo zamroziliśmy i na jak długo
    private final Map<UUID, Long> frozenPlayers = new HashMap<>();

    public MarchewkowyMiecz() {
        // Zmienione ID na to z Twojego configu!
        super("marchewkowy_miecz");
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;
        if (!(e.getEntity() instanceof Player)) return;

        Player attacker = (Player) e.getDamager();
        Player victim = (Player) e.getEntity();

        ItemStack item = attacker.getInventory().getItemInMainHand();

        // Nasza genialna metoda sprawdzająca cooldowny, regiony i wyrzucająca Title z "X"
        if (!canUse(attacker, item)) return;

        int freezeTime = getItemData().freeze_seconds;
        if (freezeTime <= 0) freezeTime = 3;

        frozenPlayers.put(victim.getUniqueId(), System.currentTimeMillis() + (freezeTime * 1000L));

        victim.sendMessage("§bZostałeś zamrożony na " + freezeTime + " sekund!");
        attacker.sendMessage("§aZamroziłeś gracza " + victim.getName() + "!");

        finishUse(attacker, item);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();

        if (frozenPlayers.containsKey(p.getUniqueId())) {
            if (frozenPlayers.get(p.getUniqueId()) > System.currentTimeMillis()) {

                Location from = e.getFrom();
                Location to = e.getTo();

                if (to != null && (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ())) {
                    Location resetLoc = from.clone();
                    resetLoc.setYaw(to.getYaw());
                    resetLoc.setPitch(to.getPitch());
                    e.setTo(resetLoc);
                }
            } else {
                frozenPlayers.remove(p.getUniqueId());
                p.sendMessage("§aZostałeś rozmrożony!");
            }
        }
    }
}