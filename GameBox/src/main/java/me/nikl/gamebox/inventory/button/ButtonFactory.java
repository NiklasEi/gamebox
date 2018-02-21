package me.nikl.gamebox.inventory.button;

import me.nikl.gamebox.GameBoxLanguage;
import me.nikl.gamebox.nms.NmsFactory;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
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
        tokenButton.addDisplay("%tokens%", token);
        tokenButton.update();
        return tokenButton;
    }
}
