package pl.arab.EVENTOWKI.managers;

import pl.arab.EVENTOWKI.EventItem;
import pl.arab.EVENTOWKI.items.*;
import pl.arab.Main;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class EventowkiManager {

    private final Main plugin;
    private final Map<String, EventItem> registeredItems = new HashMap<>();

    public EventowkiManager(Main plugin) {
        this.plugin = plugin;
        this.loadItems();
    }

    private void loadItems() {
        // Tutaj dodajesz każdą nową eventówkę!
        registerItem(new BoskiTopor());
        registerItem(new BombardaMaxima());
        registerItem(new Dynamit());
        registerItem(new CiepleMleko());
        registerItem(new MarchewkowyMiecz());
        registerItem(new SmoczyMiecz());
        registerItem(new Sniezka());
        registerItem(new Kosa());
        registerItem(new KoronaAnarchii());
        registerItem(new Rozga());
        registerItem(new KrewWampira());
        registerItem(new SiekieraGrincha());
        registerItem(new ZajeczyMiecz());
        registerItem(new SplesnialaKanapka());
        registerItem(new Parawan());
        registerItem(new WedkaSurfera());
        registerItem(new Piernik());
        registerItem(new LopataGrincha());
        registerItem(new KostkaRubika());
        registerItem(new WataCukrowa());
        registerItem(new BlokWidmo());
        registerItem(new CudownaLatarnia());
        // registerItem(new MagicznyLuk()); <- tak to będzie wyglądać w przyszłości
    }

    private void registerItem(EventItem item) {
        // Zapisujemy do mapy (po ID)
        registeredItems.put(item.getId().toLowerCase(), item);

        // Automatycznie rejestrujemy eventy z danej klasy przedmiotu
        plugin.getServer().getPluginManager().registerEvents(item, plugin);
    }

    // Pobieranie eventówki po ID (użyjemy tego w komendzie)
    public EventItem getItem(String id) {
        return registeredItems.get(id.toLowerCase());
    }

    // Zwraca listę wszystkich zarejestrowanych przedmiotów (np. do tab-complete)
    public Collection<String> getRegisteredIds() {
        return registeredItems.keySet();
    }
}