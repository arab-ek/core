package pl.arab.EVENTOWKI.config;

import eu.okaeri.configs.OkaeriConfig;

public class MessagesConfig extends OkaeriConfig {
    public String errorTitle = "&c&lX";
    public String itemUsedSubtitle = "&7Pomyślnie użyto przedmiotu!"; // <--- DODANE

    public String noPermission = "&cBrak uprawnień.";
    public String onCooldown = "&cPrzedmiotu {ITEM} możesz użyć za &e{TIME}&c!";
    public String itemGiven = "&aOtrzymałeś eventówkę: &e{ITEM}";
    public String itemNotFound = "&cNie znaleziono eventówki o ID: &e{ITEM}";
    public String blockedWorld = "&cNie możesz użyć tego w tym świecie!";
    public String blockedRegion = "&cNie możesz użyć tego na tym terenie!";
    public String bedrockDestroyed = "&aZniszczono skałę macierzystą!";
    public String negativeEffectsRemoved = "&aUsunięto wszystkie negatywne efekty!";
    public String armorNotSetOrFull = "&cNie posiadasz założonego całego setu lub jest on w pełni naprawiony!";
}