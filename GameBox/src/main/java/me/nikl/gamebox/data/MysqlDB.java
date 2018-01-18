package me.nikl.gamebox.data;

import com.zaxxer.hikari.HikariDataSource;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.GameBoxSettings;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Niklas
 */
public class MysqlDB extends DataBase {


    private static final String INSERT = "INSERT INTO " + PLAYER_TABLE + " VALUES(?,?,?,?,?) ON DUPLICATE KEY UPDATE " + PLAYER_NAME + "=?";
    private static final String SELECT = "SELECT * FROM " + PLAYER_TABLE + " WHERE " + PLAYER_UUID + "=?";
    private static final String SELECT_TOKEN = "SELECT " + PLAYER_TOKEN_PATH + " FROM " + PLAYER_TABLE + " WHERE " + PLAYER_UUID + "=?";
    private static final String SAVE = "UPDATE " + PLAYER_TABLE + " SET "
            + PLAYER_TOKEN_PATH + "=?, "
            + PLAYER_PLAY_SOUNDS + "=?, "
            + PLAYER_ALLOW_INVITATIONS
            + "=? WHERE " + PLAYER_UUID + "=?";
    private static final String SET_TOKEN = "UPDATE " + PLAYER_TABLE + " SET "
            + PLAYER_TOKEN_PATH
            + "=? WHERE " + PLAYER_UUID + "=?";

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
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + PLAYER_TABLE + "(" +
                    PLAYER_UUID + " varchar(36), " +
                    PLAYER_NAME + " VARCHAR(16), " +
                    PLAYER_TOKEN_PATH + " int, " +
                    PLAYER_PLAY_SOUNDS + " BOOL, " +
                    PLAYER_ALLOW_INVITATIONS + " BOOL, " +
                    "PRIMARY KEY (`" + PLAYER_UUID + "`))");
        } catch (SQLException e) {
            e.printStackTrace();
            GameBoxSettings.useMysql = false;
            return false;
        }

        return true;
    }

    @Override
    public void save(boolean async) {
        // nothing to do here
    }

    @Override
    public void addStatistics(UUID uuid, String gameID, String gameTypeID, double value, SaveType saveType) {
        // Todo!
    }

    @Override
    public ArrayList<Stat> getTopList(String gameID, String gameTypeID, SaveType saveType, int maxNumber) {
        // Todo!
        return new ArrayList<>();
    }

    @Override
    public void loadPlayer(GBPlayer player, boolean async) {
        // i am going to ignore the async bool here, since I don't want any sync database calls...
        if(!async) plugin.warning(" plugin tried to load player from MySQL sync...");

        // load player from database and set the results in the player class
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection connection = hikari.getConnection();
                     PreparedStatement insert = connection.prepareStatement(INSERT);
                     PreparedStatement select = connection.prepareStatement(SELECT)) {
                    Player p = player.getPlayer();
                    insert.setString(1, p.getUniqueId().toString());
                    insert.setString(2, p.getName());
                    insert.setInt(3, 0);
                    insert.setBoolean(4, true);
                    insert.setBoolean(5, true);
                    insert.setString(6, p.getName());
                    insert.execute();

                    select.setString(1, p.getUniqueId().toString());
                    ResultSet result = select.executeQuery();
                    if (result.next()) {
                        final boolean sound = result.getBoolean(PLAYER_PLAY_SOUNDS);
                        final boolean invites = result.getBoolean(PLAYER_ALLOW_INVITATIONS);
                        final int token = result.getInt(PLAYER_TOKEN_PATH);

                        // back to main thread and set player
                        Bukkit.getScheduler().runTask(plugin, () ->
                                player.setPlayerData(token, sound, invites));
                        try {
                            result.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    } else {
                        plugin.warning( " empty result set when loading player " + p.getName());
                        try {
                            result.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        };
        task.runTaskAsynchronously(plugin);
    }

    @Override
    public void savePlayer(final GBPlayer player, boolean async) {
        // must work async and sync since sync is needed on server shutdown
        if(async){
            new BukkitRunnable(){
                @Override
                public void run(){
                    savePlayer(player);
                }
            }.runTaskAsynchronously(plugin);
        } else {
            savePlayer(player);
        }
    }

    private void savePlayer(final GBPlayer player){
        try (Connection connection = hikari.getConnection();
             PreparedStatement statement = connection.prepareStatement(SAVE)){
            statement.setInt(1, player.getTokens());
            statement.setBoolean(2, player.isPlaySounds());
            statement.setBoolean(3, player.allowsInvites());
            statement.setString(4, player.getUuid().toString());
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getToken(UUID uuid, Callback<Integer> callback) {

        // load token for a player
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection connection = hikari.getConnection();
                     PreparedStatement select = connection.prepareStatement(SELECT_TOKEN)) {

                    select.setString(1, uuid.toString());
                    ResultSet result = select.executeQuery();
                    if (result.next()) {
                        new BukkitRunnable() {

                            @Override
                            public void run() {
                                // call callable back on main thread
                                Bukkit.getScheduler().runTask(plugin, () -> {
                                    try {
                                        callback.onSuccess(result.getInt(PLAYER_TOKEN_PATH));
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    } finally {
                                        try {
                                            result.close();
                                        } catch (SQLException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                        }.runTask(plugin);
                    } else {
                        plugin.warning( " empty result set trying to get token for " +uuid.toString());
                        try {
                            result.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        };
        task.runTaskAsynchronously(plugin);
    }

    @Override
    public void setToken(UUID uuid, int token) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = hikari.getConnection();
                 PreparedStatement statement = connection.prepareStatement(SET_TOKEN)){
                statement.setInt(1, token);
                statement.setString(2, uuid.toString());
                statement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onShutDown(){
        super.onShutDown();
        hikari.close();
    }

}
