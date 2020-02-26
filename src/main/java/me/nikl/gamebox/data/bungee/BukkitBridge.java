package me.nikl.gamebox.data.bungee;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.data.toplist.PlayerScore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author Niklas Eicker
 */
public class BukkitBridge implements PluginMessageListener, Listener {
  private GameBox gameBox;

  public BukkitBridge(GameBox gameBox) {
    this.gameBox = gameBox;
    gameBox.getServer().getMessenger().registerOutgoingPluginChannel(gameBox, "BungeeCord");
    gameBox.getServer().getMessenger().registerIncomingPluginChannel(gameBox, "BungeeCord", this);
    GameBox.debug("listening and sending to BungeeCord...");
  }

  @Override
  public void onPluginMessageReceived(String channel, Player player, byte[] bytes) {
    if (!channel.equals("BungeeCord")) {
      return;
    }
    DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));
    try {
      String subChannel = in.readUTF();
      GameBox.debug("received message on Bungeecord:" + subChannel);
      if (!subChannel.equals("GameBox")) return;
      in.readShort();
      String message = in.readUTF();
      handlePluginMessage(message);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void handlePluginMessage(String message) {
    GameBox.debug("  got: " + message);
    String[] msgParts = message.split("#");
    if (msgParts.length != 3) {
      gameBox.warning("Cannot interpret plugin message!");
      return;
    }
    String gameID = msgParts[0];
    String gameTypeID = msgParts[1];
    try {
      PlayerScore playerScore = PlayerScore.fromString(msgParts[2]);
      gameBox.getDataBase().getTopList(gameID, gameTypeID, playerScore.getSaveType()).update(playerScore);
    } catch (IllegalArgumentException exception) {
      gameBox.warning("Cannot interpret plugin message!");
      exception.printStackTrace();
    }
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    if (Bukkit.getOnlinePlayers().size() != 1) return;
    //GameBox.debug("first player joined, loading");
    //gameBox.getDataBase().updateTopLists();
  }

  public void sendTopListUpdate(String gameID, String gameTypeID, PlayerScore playerScore) {
    String message = gameID + "#" + gameTypeID + "#" + playerScore.toString();
    ByteArrayDataOutput out = ByteStreams.newDataOutput();
    try {
      out.writeUTF("Forward");
      out.writeUTF("ALL");
      out.writeUTF("GameBox");
      ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
      DataOutputStream msgout = new DataOutputStream(msgbytes);
      msgout.writeUTF(message);
      GameBox.debug("   sending: " + message);

      out.writeShort(msgbytes.toByteArray().length);
      out.write(msgbytes.toByteArray());
    } catch (IOException e) {
      e.printStackTrace();
    }
    Bukkit.getServer().sendPluginMessage(gameBox, "BungeeCord", out.toByteArray());
  }
}
