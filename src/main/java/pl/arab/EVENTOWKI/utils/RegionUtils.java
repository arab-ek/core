package pl.arab.EVENTOWKI.utils;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public class RegionUtils {

    // Nowa metoda: Sprawdza samą lokalizację (przydatne do wybuchów)
    public static boolean isInBlockedRegion(Location loc, List<String> blockedRegions) {
        if (blockedRegions == null || blockedRegions.isEmpty()) return false;

        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            ApplicableRegionSet set = container.createQuery().getApplicableRegions(BukkitAdapter.adapt(loc));

            for (ProtectedRegion region : set) {
                if (blockedRegions.contains(region.getId().toLowerCase())) {
                    return true; // Znaleziono zablokowany region
                }
            }
        } catch (NoClassDefFoundError e) {
            // Ignorujemy brak WorldGuarda
        }
        return false;
    }

    // Stara metoda dla gracza (korzysta z nowej)
    public static boolean isInBlockedRegion(Player p, List<String> blockedRegions) {
        return isInBlockedRegion(p.getLocation(), blockedRegions);
    }
}