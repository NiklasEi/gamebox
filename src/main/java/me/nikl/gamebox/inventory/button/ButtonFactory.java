package me.nikl.gamebox.inventory.button;

import me.nikl.gamebox.GameBoxLanguage;
import me.nikl.gamebox.inventory.ClickAction;
import me.nikl.gamebox.utility.ItemStackUtility;
import me.nikl.nmsutilities.NmsFactory;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

/**
 * @author Niklas Eicker
 */
public class ButtonFactory {
  private static ItemStack nextPageButton;
  private static ItemStack previousPageButton;

  public static DisplayButton createTokenButton(GameBoxLanguage language, int token) {
    ItemStack tokensItem = new ItemStack(Material.GOLD_NUGGET, 1);
    tokensItem = NmsFactory.getNmsUtility().addGlow(tokensItem);
    DisplayButton tokenButton = new DisplayButton(tokensItem, language.BUTTON_TOKENS, new ArrayList<>());
    return tokenButton.update("%tokens%", token);
  }

  public static ToggleButton createSoundToggleButton(GameBoxLanguage language) {
    ItemStack toggle = new ItemStack(ItemStackUtility.MUSIC_DISC_GREEN, 1);
    ToggleButton soundToggle = new ToggleButton(toggle, new ItemStack(ItemStackUtility.MUSIC_DISC_RED, 1));
    ItemMeta meta = soundToggle.getItemMeta();
    meta.addItemFlags(ItemFlag.values());
    meta.setDisplayName(language.BUTTON_SOUND_ON_NAME);
    meta.setLore(language.BUTTON_SOUND_ON_LORE);
    soundToggle.setItemMeta(meta);
    soundToggle.setToggleDisplayName(language.BUTTON_SOUND_OFF_NAME);
    soundToggle.setToggleLore(language.BUTTON_SOUND_OFF_LORE);
    soundToggle.setAction(ClickAction.TOGGLE);
    soundToggle.setArgs("sound");
    return soundToggle;
  }

  public static AButton createShopPageForwardButton(GameBoxLanguage language, String argOne, String argTwo) {
    if (nextPageButton == null) {
      nextPageButton = new ItemStack(Material.ARROW, 1);
      ItemMeta meta = nextPageButton.getItemMeta();
      meta.setDisplayName(language.BUTTON_FORWARD);
      nextPageButton.setItemMeta(meta);
    }
    return new Button(nextPageButton).setActionAndArgs(ClickAction.OPEN_SHOP_PAGE, argOne, argTwo);
  }

  public static AButton createModulesPageForwardButton(GameBoxLanguage language, String argOne) {
    if (nextPageButton == null) {
      nextPageButton = new ItemStack(Material.ARROW, 1);
      ItemMeta meta = nextPageButton.getItemMeta();
      meta.setDisplayName(language.BUTTON_FORWARD);
      nextPageButton.setItemMeta(meta);
    }
    return new Button(nextPageButton).setActionAndArgs(ClickAction.OPEN_MODULES_PAGE, argOne);
  }

  public static AButton createModuleDetailsPageForwardButton(GameBoxLanguage lang, String moduleId, String page) {
    if (nextPageButton == null) {
      nextPageButton = new ItemStack(Material.ARROW, 1);
      ItemMeta meta = nextPageButton.getItemMeta();
      meta.setDisplayName(lang.BUTTON_FORWARD);
      nextPageButton.setItemMeta(meta);
    }
    return new Button(nextPageButton).setActionAndArgs(ClickAction.OPEN_MODULE_DETAILS, moduleId, page);
  }

  public static AButton createShopPageBackButton(GameBoxLanguage language, String argOne, String argTwo) {
    if (previousPageButton == null) {
      previousPageButton = new ItemStack(Material.ARROW, 1);
      ItemMeta meta = previousPageButton.getItemMeta();
      meta.setDisplayName(language.BUTTON_BACK);
      previousPageButton.setItemMeta(meta);
    }
    return new Button(previousPageButton).setActionAndArgs(ClickAction.OPEN_SHOP_PAGE, argOne, argTwo);
  }

  public static AButton createModulesPageBackButton(GameBoxLanguage language, String argOne) {
    if (previousPageButton == null) {
      previousPageButton = new ItemStack(Material.ARROW, 1);
      ItemMeta meta = previousPageButton.getItemMeta();
      meta.setDisplayName(language.BUTTON_BACK);
      previousPageButton.setItemMeta(meta);
    }
    return new Button(previousPageButton).setActionAndArgs(ClickAction.OPEN_MODULES_PAGE, argOne);
  }

  public static AButton createModuleDetailsPageBackButton(GameBoxLanguage lang, String moduleId, String page) {
    if (nextPageButton == null) {
      nextPageButton = new ItemStack(Material.ARROW, 1);
      ItemMeta meta = nextPageButton.getItemMeta();
      meta.setDisplayName(lang.BUTTON_BACK);
      nextPageButton.setItemMeta(meta);
    }
    return new Button(nextPageButton).setActionAndArgs(ClickAction.OPEN_MODULE_DETAILS, moduleId, page);
  }
}
