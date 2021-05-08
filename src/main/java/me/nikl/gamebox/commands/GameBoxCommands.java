package me.nikl.gamebox.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.annotation.CommandAlias;
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
import me.nikl.gamebox.commands.admin.modules.Install;
import me.nikl.gamebox.commands.admin.modules.List;
import me.nikl.gamebox.commands.admin.modules.Remove;
import me.nikl.gamebox.commands.admin.modules.Update;
import me.nikl.gamebox.commands.general.HelpCommand;
import me.nikl.gamebox.commands.general.InfoCommand;
import me.nikl.gamebox.commands.player.GetTokenCount;
import me.nikl.gamebox.commands.player.InvitationClickCommand;
import me.nikl.gamebox.commands.player.OpenGameBox;
import me.nikl.gamebox.data.toplist.SaveType;
import me.nikl.gamebox.module.data.CloudModuleData;
import me.nikl.gamebox.module.data.ModuleBasicData;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Niklas Eicker
 */
public class GameBoxCommands extends BukkitCommandManager {
  public static final String INVITE_CLICK_COMMAND = "click-" + UUID.randomUUID().toString().substring(0, 13);
  private GameBox gameBox;

  public GameBoxCommands(GameBox gameBox) {
    super(gameBox);
    this.gameBox = gameBox;
    getCommandReplacements().addReplacement("INVITE_CLICK_COMMAND", INVITE_CLICK_COMMAND);
    getCommandReplacements().addReplacement("mainCommand", GameBoxSettings.mainCommand);
    getCommandReplacements().addReplacement("adminCommand", GameBoxSettings.adminCommand);
    getCommandReplacements().addReplacement("adminGameCommand", GameBoxSettings.adminCommand + " game");
    registerCommandCompletions();
    registerCommands();
  }

  private void registerCommandCompletions() {
    getCommandCompletions().registerCompletion("installedGameIds", c ->
            gameBox.getPluginManager().getGames().keySet()
    );
    getCommandCompletions().registerCompletion("loadedModuleIds", c ->
            gameBox.getModulesManager().getLoadedVersionedModules().stream().map(ModuleBasicData::getId).collect(Collectors.toList())
    );
    getCommandCompletions().registerCompletion("cloudModuleIds", c ->
            gameBox.getModulesManager().getCloudService().getCachedCloudContent().stream().map(CloudModuleData::getId).collect(Collectors.toList())
    );
    getCommandCompletions().registerCompletion("SaveTypes", c ->
            Arrays.asList(Arrays.stream(SaveType.values()).map(Object::toString).toArray(String[]::new))
    );
    getCommandCompletions().registerCompletion("allGameIds", c -> {
              Set<String> gameIds = gameBox.getGameRegistry().getGameIds();
              gameIds.addAll(gameBox.getGameRegistry().getDisabledGameIds());
              return gameIds;
            }
    );
    getCommandCompletions().registerCompletion("SubCommands", c ->
            gameBox.getGameRegistry().getSubCommands()
    );
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
    registerCommand(new Install(gameBox));
    registerCommand(new Update(gameBox));
    registerCommand(new List(gameBox));
    registerCommand(new Remove(gameBox));
    if (GameBox.debug) registerCommand(new TestCommands(gameBox));
  }

  @Override
  public void registerCommand(BaseCommand baseCommand) {
    GameBox.debug("registering " + baseCommand.getClass().getSimpleName());
    GameBox.debug("   annotated -> " + (baseCommand.getClass().getAnnotation(CommandAlias.class) != null ? baseCommand.getClass().getAnnotation(CommandAlias.class).value() : "null"));
    super.registerCommand(baseCommand.setExceptionHandler((command, registeredCommand, sender, args, throwable) -> {
      sender.sendMessage("Caught exception in command " + baseCommand.getName() + ":");
      throwable.printStackTrace();
      return true;
    }), true);
  }
}
