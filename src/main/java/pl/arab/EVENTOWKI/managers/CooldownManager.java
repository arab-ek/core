package pl.arab.EVENTOWKI.managers;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pl.arab.EVENTOWKI.EventItem;
import pl.arab.Main;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    // Mapa: Gracz -> (Mapa: ID Eventówki -> Czas końca w ms)
    private static final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public static void setCooldown(Player p, String itemId, int seconds) {
        cooldowns.computeIfAbsent(p.getUniqueId(), k -> new HashMap<>())
                .put(itemId.toLowerCase(), System.currentTimeMillis() + (seconds * 1000L));
    }

    public static boolean isOnCooldown(Player p, String itemId) {
        if (!cooldowns.containsKey(p.getUniqueId())) return false;
        Long endTime = cooldowns.get(p.getUniqueId()).get(itemId.toLowerCase());
        return endTime != null && endTime > System.currentTimeMillis();
    }

    public static String getFormattedTimeLeft(Player p, String itemId) {
        if (!isOnCooldown(p, itemId)) return "";
        long millisLeft = cooldowns.get(p.getUniqueId()).get(itemId.toLowerCase()) - System.currentTimeMillis();
        long totalSeconds = millisLeft / 1000;

        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        if (minutes > 0) return minutes + "m " + seconds + "s";
        return seconds + "s";
    }

    // Niezawodne wyciąganie koloru bezpośrednio z surowego stringa z configu
    private static String extractItemColor(String nameRaw) {
        if (nameRaw == null) return "§e"; // Zabezpieczenie na nulla

        for (int i = 0; i < nameRaw.length() - 1; i++) {
            char current = nameRaw.charAt(i);
            // Sprawdzamy czy to znak formatowania z configu (&) lub z serwera (§)
            if (current == '&' || current == '§') {
                char next = nameRaw.toLowerCase().charAt(i + 1);
                // Sprawdzamy czy to podstawowy kod koloru MC (ignorujemy pogrubienia 'l' itd.)
                if ("0123456789abcdef".indexOf(next) != -1) {
                    return "§" + next; // Zwracamy kolor w poprawnym formacie
                }
            }
        }
        return "§e"; // Domyślny żółty, jeśli item nie ma kodu koloru
    }

    // Task odświeżający Action Bar
    public static void startActionBarTask(Main plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!cooldowns.containsKey(p.getUniqueId())) continue;

                    Map<String, Long> pCooldowns = cooldowns.get(p.getUniqueId());
                    StringBuilder actionBar = new StringBuilder();
                    boolean first = true;

                    for (Map.Entry<String, Long> entry : pCooldowns.entrySet()) {
                        if (entry.getValue() > System.currentTimeMillis()) {
                            if (!first) actionBar.append(ChatColor.WHITE).append(" §8| ");

                            String id = entry.getKey();
                            EventItem item = plugin.getEventowkiManager().getItem(id);

                            // Pobieranie nazwy i wyciąganie z niej koloru
                            String nameRaw = item != null ? item.getItemData().name : id;
                            String itemColor = extractItemColor(nameRaw);

                            // Czyścimy nazwę ze wszystkich kolorów, żeby na Action Barze była na 100% biała
                            String cleanName = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', nameRaw));

                            String timeString = getFormattedTimeLeft(p, id);

                            // Sklejamy: Biała nazwa + (Kolorowy Czas z itemu)
                            actionBar.append(ChatColor.WHITE).append(cleanName).append(" (")
                                    .append(itemColor).append(timeString).append(ChatColor.WHITE).append(")");
                            first = false;
                        }
                    }

                    if (actionBar.length() > 0) {
                        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(actionBar.toString()));
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // Uruchamiaj co 1 sekundę
    }
}