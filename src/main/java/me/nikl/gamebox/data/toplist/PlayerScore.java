package me.nikl.gamebox.data.toplist;

import java.util.UUID;

/**
 * Created by nikl on 22.01.18.
 */
public class PlayerScore {
  private double value;
  private UUID uuid;

  private SaveType saveType;

  public PlayerScore(UUID uuid, double value, SaveType saveType) {
    this.uuid = uuid;
    this.value = value;
    this.saveType = saveType;
  }

  public static PlayerScore fromString(String string) {
    String[] params = string.split(":");
    if (params.length != 3) {
      throw new IllegalArgumentException("Unknown number of parameters");
    }
    UUID uuid;
    double value;
    SaveType saveType;
    try {
      uuid = UUID.fromString(params[0]);
    } catch (IllegalArgumentException exception) {
      throw new IllegalArgumentException("Illegal uuid in player score", exception);
    }
    try {
      value = Double.valueOf(params[1]);
      saveType = SaveType.valueOf(params[2]);
    } catch (NumberFormatException exception) {
      throw new IllegalArgumentException("Illegal score in player score", exception);
    } catch (IllegalArgumentException exception) {
      throw new IllegalArgumentException("Illegal saveType in player score", exception);
    }
    return new PlayerScore(uuid, value, saveType);
  }

  public double getValue() {
    return value;
  }

  public UUID getUuid() {
    return uuid;
  }

  public SaveType getSaveType() {
    return saveType;
  }

  public boolean isBetterThen(PlayerScore score) {
    if (saveType.isHigherScore()) return value > score.getValue();
    return value < score.getValue();
  }

  public void updateValue(double newValue) {
    this.value = newValue;
  }

  @Override
  public String toString() {
    return uuid.toString() + ":" + String.valueOf(value) + ":" + saveType.toString();
  }
}
