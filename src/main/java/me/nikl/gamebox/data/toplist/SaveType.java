package me.nikl.gamebox.data.toplist;

/**
 * @author Niklas Eicker
 */
public enum SaveType {
  TIME_LOW, TIME_HIGH, SCORE, WINS, HIGH_NUMBER_SCORE;

  public boolean isHigherScore() {
    return this != TIME_LOW;
  }
}
