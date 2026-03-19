package pl.arab;

import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer;
import eu.okaeri.configs.yaml.bukkit.serdes.SerdesBukkit;
import org.bukkit.plugin.java.JavaPlugin;
import pl.arab.COMMANDSUSEMAP.CommandMap;
import pl.arab.EVENTOWKI.config.MessagesConfig;
import pl.arab.EVENTOWKI.config.PluginConfig;
import pl.arab.EVENTOWKI.managers.CooldownManager;
import pl.arab.EVENTOWKI.managers.EventowkiManager;

import java.io.File;

public final class Main extends JavaPlugin {

    private static Main instance;
    private PluginConfig pluginConfig;
    private MessagesConfig messagesConfig; // DODANE
    private EventowkiManager eventowkiManager; // DODANE

    @Override
    public void onEnable() {
        instance = this;

        // 1. Ładowanie Okaeri Configs (Główny config)
        try {
            this.pluginConfig = ConfigManager.create(PluginConfig.class, (it) -> {
                it.withConfigurer(new YamlBukkitConfigurer(), new SerdesBukkit());
                it.withBindFile(new File(this.getDataFolder(), "config.yml"));
                it.saveDefaults();
                it.load(true);
            });

            // Ładowanie Okaeri Configs (Wiadomości)
            this.messagesConfig = ConfigManager.create(MessagesConfig.class, (it) -> {
                it.withConfigurer(new YamlBukkitConfigurer(), new SerdesBukkit());
                it.withBindFile(new File(this.getDataFolder(), "messages.yml"));
                it.saveDefaults();
                it.load(true);
            });
            getLogger().info("Konfiguracja zaladowana pomyslnie!");
        } catch (Exception e) {
            getLogger().severe("Blad podczas ladowania konfiguracji: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // 2. Inicjalizacja Managera Eventówek
        this.eventowkiManager = new EventowkiManager(this);

        getServer().getPluginManager().registerEvents(new pl.arab.EVENTOWKI.gui.GuiListener(), this);

        // 3. Inicjalizacja komend
        new CommandMap(this, this.eventowkiManager);
        // 4. Start Action Baru
        CooldownManager.startActionBarTask(this);

        getLogger().info("Zaawansowany Core by Arab - Uruchomiony!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Wylaczanie...");
    }

    public static Main getMain() {
        return instance;
    }

    public PluginConfig getPluginConfig() {
        return pluginConfig;
    }

    // --- DODANE GETTERY DO ROZWIĄZANIA BŁĘDÓW ---
    public MessagesConfig getMessages() {
        return messagesConfig;
    }

    public EventowkiManager getEventowkiManager() {
        return eventowkiManager;
    }
}