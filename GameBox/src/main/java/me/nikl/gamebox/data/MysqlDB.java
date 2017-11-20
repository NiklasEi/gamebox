package me.nikl.gamebox.data;

import com.zaxxer.hikari.HikariDataSource;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.GameBoxSettings;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Niklas
 */
public class MysqlDB extends DataBase {


    private static final String INSERT = "INSERT INTO GBPlayers VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE UUID=?";
    private static final String SELECT = "SELECT token FROM GBPlayers WHERE uuid=?";
    private static final String SAVE = "UPDATE GBPlayers SET token=? WHERE uuid=?";

    private String host, database, username, password;
    private int port;


    private HikariDataSource hikari;


    public MysqlDB(GameBox plugin) {
        super(plugin);

        FileConfiguration config = plugin.getConfig();
        host = config.getString("mysql.host");
        port = config.getInt("mysql.port");
        database = config.getString("mysql.database");
        username = config.getString("mysql.username");
        password = config.getString("mysql.password");
    }

    @Override
    public boolean load(boolean async) {
        hikari = new HikariDataSource();
        hikari.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        hikari.addDataSourceProperty("serverName", host);
        hikari.addDataSourceProperty("port", port);
        hikari.addDataSourceProperty("databaseName", database);
        hikari.addDataSourceProperty("user", username);
        hikari.addDataSourceProperty("password", password);

        try(Connection connection = hikari.getConnection()){
            Statement statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS GBPlayers(UUID varchar(36), name VARCHAR(16), TOKEN int, playSounds BOOL)");
        } catch (SQLException e) {
            e.printStackTrace();
            GameBoxSettings.useMysql = false;
            return false;
        }

        return true;
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
    public void save(boolean async) {

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

}
