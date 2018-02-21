package me.nikl.gamebox.inventory.menu;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.data.GBPlayer;
import me.nikl.gamebox.inventory.button.Button;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by nikl on 21.02.18.
 */
public abstract class PerPlayerMenu extends StaticMenu {
    protected Map<UUID, Inventory> playerInventories = new HashMap<>();
    protected Map<UUID, Button[]> playerButtons = new HashMap<>();

    public PerPlayerMenu(GameBox gameBox) {
        super(gameBox);
    }

    protected void setUpperPlayerButton(Button button, int slot, UUID uuid){
        Button[] buttons = playerButtons.get(uuid);
        if (buttons == null) buttons = new Button[lowerGrid.length];
        buttons[slot] = button;
        playerButtons.put(uuid, buttons);
    }

    public abstract void updatePlayer(GBPlayer gbPlayer);
}
