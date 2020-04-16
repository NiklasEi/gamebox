package me.nikl.gamebox.events.modules;

import me.nikl.gamebox.module.local.VersionedModule;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ModuleInstalledEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private VersionedModule module;

    public ModuleInstalledEvent(VersionedModule module) {
        super(true);
        this.module = module;
        Bukkit.getPluginManager().callEvent(this);
    }

    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return handlers;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public VersionedModule getModule() {
        return module;
    }
}
