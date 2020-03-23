package me.nikl.gamebox.inventory.gui.game;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.GameBoxSettings;
import me.nikl.gamebox.game.Game;
import me.nikl.gamebox.inventory.ClickAction;
import me.nikl.gamebox.inventory.GUIManager;
import me.nikl.gamebox.inventory.button.Button;
import me.nikl.gamebox.inventory.gui.AGui;
import me.nikl.gamebox.utility.ItemStackUtility;
import me.nikl.nmsutilities.NmsFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

/**
 * @author Niklas Eicker
 */
public class GameGui extends AGui {
  private String gameId;

  /**
   * Constructor for a gamegui
   * <p>
   * Don't forget to register it with the GUIManager
   *
   * @param plugin     plugin instance
   * @param guiManager GUIManager instance
   * @param slots      number of slots in the inventory
   * @param gameID     ID of the game
   * @param key        GUI key
   * @param title      proposed title of the GUI
   */
  public GameGui(GameBox plugin, GUIManager guiManager, int slots, String gameID, String key, String title) {
    super(plugin, guiManager, slots, new String[]{gameID, key}, title);
    this.gameId = gameID;


    Map<Integer, ItemStack> hotBarButtons = plugin.getPluginManager().getHotBarButtons();


    // set lower grid
    if (hotBarButtons.containsKey(GameBoxSettings.exitButtonSlot)) {
      Button exit = new Button(hotBarButtons.get(GameBoxSettings.exitButtonSlot));
      ItemMeta meta = hotBarButtons.get(GameBoxSettings.exitButtonSlot).getItemMeta();
      exit.setItemMeta(meta);
      exit.setAction(ClickAction.CLOSE);
      setLowerButton(exit, GameBoxSettings.exitButtonSlot);
    }


    if (hotBarButtons.containsKey(GameBoxSettings.toMainButtonSlot)) {
      Button main = new Button(hotBarButtons.get(GameBoxSettings.toMainButtonSlot));
      ItemMeta meta = hotBarButtons.get(GameBoxSettings.toMainButtonSlot).getItemMeta();
      main.setItemMeta(meta);
      main.setAction(ClickAction.OPEN_MAIN_GUI);
      setLowerButton(main, GameBoxSettings.toMainButtonSlot);
    }
  }

  /**
   * Only to be used for the main GUI of a game
   * <p>
   * The title is automatically set to the game-title
   * set in the GameBox language file
   *
   * @param plugin GameBox instance
   * @param game   the game belonging to this gui
   * @param slots  slots of the GUI
   */
  public GameGui(GameBox plugin, Game game, int slots) {
    this(plugin, plugin.getPluginManager().getGuiManager(), slots, game.getGameID(), GUIManager.MAIN_GAME_GUI, plugin.lang.TITLE_GAME_GUI);
  }

  /**
   * Place a help button with the given text in the
   * lower right corner of the GUI
   *
   * @param list text that will be displayed on the button
   */
  public void setHelpButton(List<String> list) {
    Button help = new Button(NmsFactory.getNmsUtility().addGlow(ItemStackUtility.createBookWithText(list)));
    help.setAction(ClickAction.NOTHING);

    setButton(help, inventory.getSize() - 1);
  }

  public String getGameId() {
    return this.gameId;
  }
}
