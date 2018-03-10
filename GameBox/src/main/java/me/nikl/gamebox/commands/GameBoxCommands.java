package me.nikl.gamebox.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandManager;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.commands.admin.DatabaseConverter;
import me.nikl.gamebox.commands.admin.LanguageCommands;
import me.nikl.gamebox.commands.admin.Reload;
import me.nikl.gamebox.commands.admin.ResetHighScores;
import me.nikl.gamebox.commands.admin.TestCommands;
import me.nikl.gamebox.commands.admin.ToggleDebug;
import me.nikl.gamebox.commands.admin.TokenCommands;
import me.nikl.gamebox.commands.player.HelpCommand;
import me.nikl.gamebox.commands.player.InfoCommand;
import me.nikl.gamebox.commands.player.InvitationClickCommand;
import me.nikl.gamebox.commands.player.OpenGameBox;
import me.nikl.gamebox.utility.Permission;

import java.util.UUID;

/**
 * @author Niklas Eicker
 */
public class GameBoxCommands extends BukkitCommandManager {
    public static final String INVITE_CLICK_COMMAND = UUID.randomUUID().toString();
    private GameBox gameBox;
    private DefaultExceptionHandler defaultExceptionHandler;

    public GameBoxCommands(GameBox gameBox) {
        super(gameBox);
        this.gameBox = gameBox;
        getCommandReplacements().addReplacement("INVITE_CLICK_COMMAND", INVITE_CLICK_COMMAND);
        getCommandReplacements().addReplacement("mainCommand", "gb|games|gamebox");
        getCommandReplacements().addReplacement("adminCommand", "gba|gameboxadmin|gamesadmin");
        getCommandReplacements().addReplacement("infoPermission", Permission.CMD_INFO.getPermission());
        getCommandReplacements().addReplacement("helpPermission", Permission.CMD_HELP.getPermission());
        defaultExceptionHandler = new DefaultExceptionHandler();
        registerCommands();
    }

    private void registerCommands() {
        registerCommand(new OpenGameBox(gameBox));
        registerCommand(new InvitationClickCommand(gameBox));
        registerCommand(new InfoCommand(gameBox));
        registerCommand(new HelpCommand(gameBox));
        // admin commands
        registerCommand(new ResetHighScores(gameBox));
        registerCommand(new DatabaseConverter(gameBox));
        registerCommand(new Reload(gameBox));
        registerCommand(new LanguageCommands(gameBox));
        registerCommand(new TestCommands(gameBox));
        registerCommand(new ToggleDebug(gameBox));
        registerCommand(new TokenCommands(gameBox));
        if(GameBox.debug) registerCommand(new TestCommands(gameBox));
    }

    @Override
    public void registerCommand(BaseCommand baseCommand) {
        super.registerCommand(baseCommand.setExceptionHandler(defaultExceptionHandler));
    }
}
