package me.nikl.gamebox.commands.admin;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Subcommand;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.commands.GameBoxBaseCommand;
import me.nikl.gamebox.data.database.DataBase;
import me.nikl.gamebox.data.database.MysqlDB;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * @author Niklas Eicker
 */
@CommandAlias("%adminCommand")
public class DatabaseConverter extends GameBoxBaseCommand {
    public DatabaseConverter(GameBox gameBox) {
        super(gameBox);
    }

    @Subcommand("filetomysql")
    public void onConvertFileToMySQL (CommandSender sender) {
        DataBase dataBase = gameBox.getDataBase();
        if (!(dataBase instanceof MysqlDB)) {
            sender.sendMessage(gameBox.lang.PREFIX + ChatColor.RED + " You must have MySQL enabled to do this!");
            return;
        }
        ((MysqlDB) dataBase).convertFromFile(sender);
    }
}
