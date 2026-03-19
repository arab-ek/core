package pl.arab.COMMANDSUSEMAP;

import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.SimplePluginManager;
import pl.arab.EVENTOWKI.commands.EventowkiCommand;
import pl.arab.EVENTOWKI.managers.EventowkiManager;
import pl.arab.Main;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Stream;

public class CommandMap {
    private final Main plugin;
    private SimpleCommandMap scm;

    public CommandMap(Main plugin, EventowkiManager eventowkiManager) {
        this.plugin = plugin;
        this.setupSimpleCommandMap();

        Stream.of(
                new EventowkiCommand("eventowki", List.of("eventowka"), eventowkiManager)
        ).forEach(this::registerCommands);
    }

    private void registerCommands(CommandUse cmd) {
        this.scm.register(this.plugin.getDescription().getName(), cmd);
    }

    private void setupSimpleCommandMap() {
        SimplePluginManager spm = (SimplePluginManager) this.plugin.getServer().getPluginManager();
        Field f = null;
        try {
            f = SimplePluginManager.class.getDeclaredField("commandMap");
            f.setAccessible(true);
            this.scm = (SimpleCommandMap) f.get(spm);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}