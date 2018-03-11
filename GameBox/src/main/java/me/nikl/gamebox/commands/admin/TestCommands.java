package me.nikl.gamebox.commands.admin;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.commands.ConsoleBaseCommand;
import me.nikl.gamebox.commands.GameBoxBaseCommand;
import me.nikl.gamebox.data.GBPlayer;
import me.nikl.gamebox.data.database.DataBase;
import me.nikl.gamebox.data.database.MysqlDB;
import me.nikl.gamebox.data.toplist.SaveType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * @author Niklas Eicker
 */
@CommandAlias("%adminCommand")
public class TestCommands extends ConsoleBaseCommand {
    public TestCommands(GameBox gameBox) {
        super(gameBox);
    }

    @Subcommand("findColumns")
    public void findColumns(CommandSender sender, @Single String columnNameBeginning) {
        DataBase dataBase = gameBox.getDataBase();
        if (!(dataBase instanceof MysqlDB)) return;
        List<String> columns = ((MysqlDB) dataBase).getHighScoreColumnsBeginningWith(columnNameBeginning);
        sender.sendMessage(String.join(", ", columns));
    }

    @Subcommand("hundredrandom")
    public void addHundredRandomStats(CommandSender sender) {
        DataBase dataBase = gameBox.getDataBase();
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            UUID uuid = UUID.randomUUID();
            dataBase.savePlayer(new GBPlayer(gameBox, uuid, 0, true, true), true);
            dataBase.addStatistics(uuid, GameBox.MODULE_COOKIECLICKER, "weekly", random.nextInt(5000), SaveType.HIGH_NUMBER_SCORE);
        }
    }
}
