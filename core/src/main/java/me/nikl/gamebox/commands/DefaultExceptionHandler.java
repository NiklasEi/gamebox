package me.nikl.gamebox.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.ExceptionHandler;
import co.aikar.commands.RegisteredCommand;

import java.util.List;

/**
 * @author Niklas Eicker
 */
public class DefaultExceptionHandler implements ExceptionHandler {
    @Override
    public boolean execute(BaseCommand baseCommand, RegisteredCommand registeredCommand, CommandIssuer commandIssuer, List<String> list, Throwable throwable) {
        commandIssuer.sendMessage("in exception handler");
        return false;
    }
}
