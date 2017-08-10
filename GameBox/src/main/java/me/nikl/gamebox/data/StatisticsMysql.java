package me.nikl.gamebox.data;

import java.sql.Connection;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.GameBoxSettings;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Created by Niklas on 03.03.2017.
 */
public class StatisticsMysql extends Statistics {
    private Connection connection;
    private String host, database, username, password;
    private int port;


    public StatisticsMysql(GameBox plugin) {
        super(plugin);

        FileConfiguration config = plugin.getConfig();
        host = config.getString("mysql.host");
        database = config.getString("mysql.database");
        username = config.getString("mysql.username");
        password = config.getString("mysql.password");

        port = config.getInt("mysql.port");


        try {
            openConnection();
            Statement statement = connection.createStatement();
        } catch (ClassNotFoundException | SQLException e) {
            GameBoxSettings.useMysql = false;
            plugin.getLogger().log(Level.SEVERE, " Failed to establish a connection to the MySql database!");
            plugin.getLogger().log(Level.SEVERE, " Falling back to file storage...");
            e.printStackTrace();
            return;
        }
    }

    @Override
    public boolean load() {
        return false;
    }

    @Override
    public boolean getBoolean(UUID uuid, String path) {
        return false;
    }

    @Override
    public boolean getBoolean(UUID uuid, String path, boolean defaultValue) {
        return false;
    }

    @Override
    public void set(String uuid, String path, Object b) {

    }

    @Override
    public void save() {

    }

    @Override
    public int getInt(UUID uuid, String path, int defaultValue) {
        return 0;
    }

    @Override
    public void addStatistics(UUID uuid, String gameID, String gameTypeID, double value, SaveType saveType) {

    }

    @Override
    public ArrayList<Stat> getTopList(String gameID, String gameTypeID, SaveType saveType, int maxNumber) {
        return null;
    }

    @Override
    public boolean isSet(String path) {
        return false;
    }

    public void openConnection() throws SQLException, ClassNotFoundException {
        if (connection != null && !connection.isClosed()) {
            return;
        }

        synchronized (this) {
            if (connection != null && !connection.isClosed()) {
                return;
            }
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + this.host+ ":" + this.port + "/" + this.database, this.username, this.password);
        }
    }
}
