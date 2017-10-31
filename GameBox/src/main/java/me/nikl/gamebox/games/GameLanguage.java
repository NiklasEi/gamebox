package me.nikl.gamebox.games;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.Language;
import me.nikl.gamebox.util.Module;

import java.util.List;

/**
 * Created by nikl on 31.10.17.
 */
public abstract class GameLanguage extends Language {

    public List<String> GAME_HELP;

    public GameLanguage(GameBox plugin, Module module) {
        super(plugin, module);
    }
}
