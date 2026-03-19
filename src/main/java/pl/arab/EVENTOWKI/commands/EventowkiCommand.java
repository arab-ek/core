package pl.arab.EVENTOWKI.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.arab.COMMANDSUSEMAP.CommandUse;
import pl.arab.EVENTOWKI.EventItem;
import pl.arab.EVENTOWKI.gui.EventowkiGui;
import pl.arab.EVENTOWKI.managers.EventowkiManager;
import pl.arab.EVENTOWKI.utils.ChatUtils;

import java.util.ArrayList;
import java.util.List;

public class EventowkiCommand extends CommandUse {

    private final EventowkiManager eventowkiManager;

    public EventowkiCommand(String name, List<String> aliases, EventowkiManager manager) {
        super(name, aliases);
        this.eventowkiManager = manager;
    }

    @Override
    public void run(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return;
        Player p = (Player) sender;

        if (!p.hasPermission("arab.admin")) {
            ChatUtils.sendMessage(p, plugin.getMessages().noPermission);
            return;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("gui")) {
            EventowkiGui gui = new EventowkiGui(plugin);
            gui.open(p);
            return;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            String arg = args[1].toLowerCase();
            EventItem item = eventowkiManager.getItem(arg);

            if (item != null) {
                p.getInventory().addItem(item.getItem());
                ChatUtils.sendMessage(p, plugin.getMessages().itemGiven.replace("{ITEM}", arg));
            } else {
                ChatUtils.sendMessage(p, plugin.getMessages().itemNotFound.replace("{ITEM}", arg));
            }
            return;
        }

        // Pomoc komendy
        ChatUtils.sendMessage(p, "&7Poprawne użycie komendy:");
        ChatUtils.sendMessage(p, "&e/eventowki gui &8- &7Otwiera menu eventówek");
        ChatUtils.sendMessage(p, "&e/eventowki give <id> &8- &7Daje wybraną eventówkę");
    }

    @Override
    public List<String> tab(Player p, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            if ("gui".startsWith(partial)) completions.add("gui");
            if ("give".startsWith(partial)) completions.add("give");
        }
        else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            String partial = args[1].toLowerCase();
            for (String id : eventowkiManager.getRegisteredIds()) {
                if (id.startsWith(partial)) {
                    completions.add(id);
                }
            }
        }
        return completions;
    }
}