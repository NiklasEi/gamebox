package me.nikl.gamebox.inventory.button;

import me.nikl.gamebox.GameBoxLanguage;
import me.nikl.gamebox.inventory.ClickAction;
import me.nikl.gamebox.nms.NmsFactory;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;

/**
 * @author Niklas Eicker
 */
public class ButtonFactory {
    public static DisplayButton createTokenButton(GameBoxLanguage language, int token) {
        ItemStack tokensItem = new MaterialData(Material.GOLD_NUGGET).toItemStack(1);
        tokensItem = NmsFactory.getNmsUtility().addGlow(tokensItem);
        DisplayButton tokenButton = new DisplayButton(tokensItem, language.BUTTON_TOKENS, new ArrayList<>());
        return tokenButton.update("%tokens%", token);
    }

    public static ToggleButton createToggleButton(GameBoxLanguage language){
        ItemStack toggle = new MaterialData(Material.RECORD_6).toItemStack(1);
        ToggleButton soundToggle = new ToggleButton(toggle, new MaterialData(Material.RECORD_4));
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
}
