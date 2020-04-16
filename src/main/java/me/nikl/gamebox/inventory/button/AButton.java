package me.nikl.gamebox.inventory.button;

import me.nikl.gamebox.inventory.ClickAction;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Niklas Eicker
 */
public abstract class AButton extends ItemStack {
  protected ClickAction action;
  protected String[] args;
  protected Map<InventoryAction, ButtonAction> conditionalActions = new HashMap<>();


  public AButton(ItemStack item) {
    super(item);
    if (getAmount() < 1) setAmount(1);
  }

  public AButton setActionAndArgs(ClickAction action, String... args) {
    this.action = action;
    this.args = args;
    return this;
  }

  public ClickAction getAction(InventoryAction inventoryAction) {
    ButtonAction buttonAction = conditionalActions.get(inventoryAction);
    if (buttonAction == null) {
      return null;
    }
    return buttonAction.getAction();
  }

  public ClickAction getAction() {
    return this.action;
  }

  public void setAction(ClickAction action) {
    this.action = action;
  }

  public String[] getArgs(InventoryAction inventoryAction) {
    ButtonAction buttonAction = conditionalActions.get(inventoryAction);
    if (buttonAction == null) {
      return null;
    }
    return buttonAction.getArgs();
  }

  public String[] getArgs() {
    return this.args;
  }

  public void setArgs(String... args) {
    this.args = args;
  }

  public void addConditionalAction(InventoryAction inventoryAction, ButtonAction buttonAction) {
    this.conditionalActions.put(inventoryAction, buttonAction);
  }

  public abstract AButton clone();

  public static class ButtonAction {
    private ClickAction action;
    private String[] args;

    public ButtonAction(ClickAction action, String... args) {
      this.action = action;
      this.args = args;
    }

    public String[] getArgs() {
      return this.args;
    }

    public ClickAction getAction() {
      return this.action;
    }
  }
}
