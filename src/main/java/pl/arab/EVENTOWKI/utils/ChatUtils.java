package pl.arab.EVENTOWKI.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public final class ChatUtils {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    private ChatUtils() {
    }

    public static String fixColor(String text) {
        if (text == null) return "";
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', text);
    }

    public static Component colorize(String text) {
        if (text == null || text.isEmpty()) return Component.empty();

        // --- NAPRAWA BŁĘDU Z OBRAZKA ---
        // Najpierw zamieniamy stare kody & (lub §) na format MiniMessage
        // Robimy to przed właściwą deserializacją, aby MiniMessage je zrozumiał
        text = text.replaceAll("(?<!&|<)#([a-fA-F0-9]{6})", "<#$1>");
        text = text.replaceAll("&#([a-fA-F0-9]{6})", "<#$1>");

        // Poniższa linijka naprawia problem ze starymi kodami, takimi jak &7, zamieniając je na tagi MiniMessage
        text = text.replace("&0", "<black>").replace("&1", "<dark_blue>").replace("&2", "<dark_green>")
                .replace("&3", "<dark_aqua>").replace("&4", "<dark_red>").replace("&5", "<dark_purple>")
                .replace("&6", "<gold>").replace("&7", "<gray>").replace("&8", "<dark_gray>")
                .replace("&9", "<blue>").replace("&a", "<green>").replace("&b", "<aqua>")
                .replace("&c", "<red>").replace("&d", "<light_purple>").replace("&e", "<yellow>")
                .replace("&f", "<white>").replace("&l", "<b>").replace("&m", "<st>")
                .replace("&n", "<u>").replace("&o", "<i>").replace("&r", "<reset>");

        // Deserializujemy, wyłączając domyślną kursywę i pogrubienie, żeby zachować czysty styl z configu
        return MINI_MESSAGE.deserialize("<!i><!b>" + text);
    }

    public static List<Component> colorizeLore(List<String> lore) {
        if (lore == null || lore.isEmpty()) return new ArrayList<>();
        return lore.stream().map(ChatUtils::colorize).collect(Collectors.toList());
    }

    public static void sendMessage(CommandSender sender, String message) {
        if (sender != null) {
            sender.sendMessage(colorize(message));
        }
    }

    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        if (player == null) return;
        Title.Times times = Title.Times.times(Duration.ofMillis(fadeIn * 50L), Duration.ofMillis(stay * 50L), Duration.ofMillis(fadeOut * 50L));
        player.showTitle(Title.title(colorize(title), colorize(subtitle), times));
    }

    public static void sendActionBar(Player player, String message) {
        if (player != null) {
            player.sendActionBar(colorize(message));
        }
    }

    public static String toPlain(Component component) {
        if (component == null) return "";
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    @SuppressWarnings("deprecation")
    public static String color(String text) {
        if (text == null) return "";
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static List<String> color(List<String> text) {
        if (text == null) return new ArrayList<>();
        return text.stream().map(ChatUtils::color).collect(Collectors.toList());
    }

    @SuppressWarnings("deprecation")
    public static String clean(String text) {
        if (text == null) return "";
        return ChatColor.stripColor(text);
    }
}