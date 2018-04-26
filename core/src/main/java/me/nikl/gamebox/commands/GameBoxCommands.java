package me.nikl.gamebox.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandManager;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.GameBoxSettings;
import me.nikl.gamebox.commands.admin.DatabaseConverter;
import me.nikl.gamebox.commands.admin.LanguageCommands;
import me.nikl.gamebox.commands.admin.Reload;
import me.nikl.gamebox.commands.admin.ResetHighScores;
import me.nikl.gamebox.commands.admin.SettingsCommand;
import me.nikl.gamebox.commands.admin.TestCommands;
import me.nikl.gamebox.commands.admin.ToggleDebug;
import me.nikl.gamebox.commands.admin.TokenCommands;
import me.nikl.gamebox.commands.general.HelpCommand;
import me.nikl.gamebox.commands.general.InfoCommand;
import me.nikl.gamebox.commands.player.GetTokenCount;
import me.nikl.gamebox.commands.player.InvitationClickCommand;
import me.nikl.gamebox.commands.player.OpenGameBox;

import java.util.UUID;

/**
 * @author Niklas Eicker
 */
public class GameBoxCommands extends BukkitCommandManager {
    public static final String INVITE_CLICK_COMMAND = "click-" + UUID.randomUUID().toString().substring(0, 13);
    private GameBox gameBox;
    private DefaultExceptionHandler defaultExceptionHandler;

    public GameBoxCommands(GameBox gameBox) {
        super(gameBox);
        this.gameBox = gameBox;
        getCommandReplacements().addReplacement("INVITE_CLICK_COMMAND", INVITE_CLICK_COMMAND);
        getCommandReplacements().addReplacement("mainCommand", GameBoxSettings.mainCommand);
        getCommandReplacements().addReplacement("adminCommand", GameBoxSettings.adminCommand);
        defaultExceptionHandler = new DefaultExceptionHandler();
        registerCommands();
    }

    private void registerCommands() {
        registerCommand(new OpenGameBox(gameBox));
        registerCommand(new InvitationClickCommand(gameBox));
        registerCommand(new InfoCommand(gameBox));
        registerCommand(new HelpCommand(gameBox));
        if (GameBoxSettings.tokensEnabled) registerCommand(new GetTokenCount(gameBox));
        // admin commands
        registerCommand(new ResetHighScores(gameBox));
        registerCommand(new DatabaseConverter(gameBox));
        registerCommand(new Reload(gameBox));
        registerCommand(new LanguageCommands(gameBox));
        registerCommand(new ToggleDebug(gameBox));
        registerCommand(new TokenCommands(gameBox));
        registerCommand(new SettingsCommand(gameBox));
        if(GameBox.debug) registerCommand(new TestCommands(gameBox));
    }

    @Override
    public void registerCommand(BaseCommand baseCommand) {
        super.registerCommand(baseCommand.setExceptionHandler(defaultExceptionHandler), true);
    }
}
