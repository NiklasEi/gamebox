package me.nikl.gamebox.external;

import me.nikl.calendarevents.CalendarEvents;
import me.nikl.calendarevents.CalendarEventsApi;
import me.nikl.gamebox.GameBox;
import org.bukkit.Bukkit;

import java.util.UUID;

/**
 * @author Niklas Eicker
 */
public class CalendarEventsHook {
    private String randomEventSuffix = UUID.randomUUID().toString().substring(0, 5);
    private GameBox gameBox;
    private CalendarEventsApi apiCalendarEvents;

    public CalendarEventsHook(GameBox gameBox) {
        this.gameBox = gameBox;
        apiCalendarEvents = ((CalendarEvents) Bukkit.getPluginManager().getPlugin("CalendarEvents")).getApi();
    }

    public boolean addEvent(String label, String occasions, String timings) {
        return apiCalendarEvents.addEvent(GameBox.MODULE_GAMEBOX + label + randomEventSuffix, occasions, timings);
    }

    public void removeEvent(String label) {
        apiCalendarEvents.removeEvent(GameBox.MODULE_GAMEBOX + label + randomEventSuffix);
    }
}
