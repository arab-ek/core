package pl.arab.EVENTOWKI.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import pl.arab.EVENTOWKI.config.ItemData;

import java.util.HashMap;
import java.util.Map;

public class PluginConfig extends OkaeriConfig {

    @Comment("Kolor samego czasu na Action Barze (reszta jest na sztywno biała)")
    public String actionBarTimeColor = "&e";

    @Comment("Konfiguracja poszczególnych eventówek")
    public Map<String, ItemData> meta = new HashMap<>();
}