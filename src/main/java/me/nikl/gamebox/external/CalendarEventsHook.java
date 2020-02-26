package me.nikl.gamebox.external;

import me.nikl.calendarevents.CalendarEvent;
import me.nikl.calendarevents.CalendarEvents;
import me.nikl.calendarevents.CalendarEventsApi;
import me.nikl.gamebox.GameBox;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Niklas Eicker
 */
public class CalendarEventsHook implements Listener {
  private String randomEventSuffix = UUID.randomUUID().toString().substring(0, 5);
  private GameBox gameBox;
  private CalendarEventsApi apiCalendarEvents;
  private Map<String, CalendarEventCallBack> registeredEventCallbacks = new HashMap<>();

  public CalendarEventsHook(GameBox gameBox) {
    this.gameBox = gameBox;
    Bukkit.getPluginManager().registerEvents(this, gameBox);
    apiCalendarEvents = ((CalendarEvents) Bukkit.getPluginManager().getPlugin("CalendarEvents")).getApi();
  }

  public boolean addEvent(String label, String occasions, String timings, CalendarEventCallBack callBack) {
    String fullLabel = GameBox.MODULE_GAMEBOX + label + randomEventSuffix;
    if (apiCalendarEvents.addEvent(fullLabel, occasions, timings)) {
      registeredEventCallbacks.put(fullLabel, callBack);
      return true;
    }
    return false;
  }

  public void removeEvent(String label) {
    String fullLabel = GameBox.MODULE_GAMEBOX + label + randomEventSuffix;
    apiCalendarEvents.removeEvent(fullLabel);
    registeredEventCallbacks.remove(fullLabel);
  }

  @EventHandler
  public void onCalendarEvent(CalendarEvent event) {
    for (String label : registeredEventCallbacks.keySet()) {
      if (event.getLabels().contains(label)) {
        String originalLabel = label.replace(randomEventSuffix, "").substring(GameBox.MODULE_GAMEBOX.length());
        registeredEventCallbacks.get(label).onCalendarEvent(originalLabel);
      }
    }
  }
}
