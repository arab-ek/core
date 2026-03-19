package pl.arab.EVENTOWKI.config;

import eu.okaeri.configs.OkaeriConfig;
import org.bukkit.Material;
import java.util.ArrayList;
import java.util.List;

// Ta klasa odwzorowuje strukturę pojedynczej eventówki z Twojego pliku YML
public class ItemData extends OkaeriConfig {
    public Material material = Material.STONE;
    public String name = "&cZmień nazwę";
    public List<String> lore = new ArrayList<>();
    public int cooldown = 0;
    public boolean infinite = true;
    public int custom_model_data = 0;
    public List<String> blocked_worlds = new ArrayList<>();
    public List<String> blocked_regions = new ArrayList<>();

    public double explosion_power = 5.0; // dla bombardy
    public int immortality_duration = 3; // dla topora
    public int freeze_seconds = 3;
    public int effects_duration = 10;
    public double knockback_power = 4.0;
    public int disease_duration = 10;
    public int disease_level = 2;
    public int jump_block_duration = 4;
    public double knockback_radius = 5.0;
    public double pull_velocity = 2.3;
    public int haste_level = 10;
    public int haste_duration = 30;
    public double health_removal_hp = 20.0; // Domyślnie 10 serc
    public int lantern_regen_duration = 20;
    public int lantern_absorption_duration = 10;
    public int lantern_strength_duration = 10;
    public int radius = 10;
    public int duration = 60; // w sekundach
    public double proximity_radius = 5.0; // zasięg działania
    public double health_limit = 10.0; // do ilu serduszek obniżamy (10.0 = 5 serc)
}