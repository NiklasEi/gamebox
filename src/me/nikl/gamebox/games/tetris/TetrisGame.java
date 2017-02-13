package me.nikl.gamebox.games.tetris;

import me.nikl.gamebox.Main;
import me.nikl.gamebox.games.AGameWithTimer;
import me.nikl.gamebox.games.tetris.elements.AllElements;
import me.nikl.gamebox.games.tetris.elements.IElement;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

/**
 * Created by niklas on 10/31/16.
 *
 */
public class TetrisGame extends AGameWithTimer{
	private Player player;
	private Inventory inv;
	private int moveTicks, pauseTicks;
	private IElement fallingElement;
	private GameState state;
	
	
	public TetrisGame(Player player, TetrisGameManager gameManager) {
		super(player, gameManager);
		this.player = players[0];
		int size = 99;
		this.inv = Bukkit.createInventory(null, size, "Tetris");
		
		this.moveTicks = 5; //ToDo!!!!!!!!!!
		this.pauseTicks = 5; //ToDo!!!!!!!!!!
		
		player.getInventory().clear(); // ToDo save old inventory!!!!
		player.getInventory().setItem(21, new ItemStack(Material.STAINED_CLAY));
		player.getInventory().setItem(22, new ItemStack(Material.ARROW));
		player.getInventory().setItem(23, new ItemStack(Material.STAINED_CLAY));
		player.getInventory().setItem(31, new ItemStack(Material.STAINED_CLAY));
		
		ItemStack toGameGUI = new ItemStack(Material.BIRCH_DOOR_ITEM, 1);
		ItemMeta meta = toGameGUI.getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', gameManager.getPlugin().lang.BUTTON_GAME_GUI));
		toGameGUI.setItemMeta(meta);
		player.getInventory().setItem(35, toGameGUI);
		
		ItemStack toMainGUI = new ItemStack(Material.SPRUCE_DOOR_ITEM, 1);
		meta = toMainGUI.getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', gameManager.getPlugin().lang.BUTTON_MAIN_GUI));
		toMainGUI.setItemMeta(meta);
		player.getInventory().setItem(27, toMainGUI);
		
		player.openInventory(inv);
		this.state = GameState.PAUSE;
		spawn(pauseTicks);
		this.runTaskTimer(Main.getPlugin(Main.class), 0, this.moveTicks);
	}
	
	private void spawn(int pauseTicks) {
		if(Main.debug) Bukkit.getConsoleSender().sendMessage("scheduled task for spawning");
		Bukkit.getScheduler().scheduleSyncDelayedTask(gameManager.getPlugin(), new Runnable() {
			@Override
			public void run() {
				fallingElement = AllElements.getNewRandom();
				while (!correctStartPosition(fallingElement, true));
				setState(GameState.FALLING);
			}
		}, pauseTicks);
	}
	
	private boolean correctStartPosition(IElement fallingElement, boolean makeChanges) {
		int[] slots = fallingElement.getSlots();
		int column, center = fallingElement.getCenter();
		if(Main.debug)Bukkit.getConsoleSender().sendMessage("Center: " + center);
		int left = 0, right = 0;
		boolean toHigh = true;
		for(int slot : slots){
			if(slot > -10){
				toHigh = false;
				break;
			}
		}
		if(toHigh){
			if(makeChanges)fallingElement.setCenter(center + 9);
			return false;
		}
		
		for(int slot : slots){
			column = Math.abs(slot) % 9;
			if(column == 1 || column == 2){
				right ++;
			} else if(column == 0 || column == 8){
				left++;
			}
		}
		if((Math.abs(center) % 9) == 1)right ++;
		if((Math.abs(center) % 9) == 0)left ++;
		
		
		if(Main.debug){
			Bukkit.getConsoleSender().sendMessage("Element: " +fallingElement.getClass().getName());
			Bukkit.getConsoleSender().sendMessage("left: " + left + "    right: " + right);
		}
		
		
		if(left == 0 || right ==0) return true;
		
		if(left < right){
			if(makeChanges)fallingElement.setCenter(center - 1);
			return false;
		}
		
		if(left > right){
			if(makeChanges)fallingElement.setCenter(center + 1);
			return false;
		}
		
		return true;
	}
	
	private boolean correctPosition(IElement fallingElement, boolean makeChanges) {
		int[] slots = fallingElement.getSlots();
		int column, center = fallingElement.getCenter();
		if(Main.debug)Bukkit.getConsoleSender().sendMessage("Center: " + center);
		int left = 0, right = 0;
		
		for(int slot : slots){
			column = Math.abs(slot) % 9;
			if(column == 8 || column == 7){
				right ++;
			} else if(column == 0 || column == 1){
				left++;
			}
		}
		if((Math.abs(center) % 9) == 1)right ++;
		if((Math.abs(center) % 9) == 0)left ++;
		
		
		if(Main.debug){
			Bukkit.getConsoleSender().sendMessage("Element: " +fallingElement.getClass().getName());
			Bukkit.getConsoleSender().sendMessage("left: " + left + "    right: " + right);
		}
		
		
		if(left == 0 || right ==0) return true;
		
		if(left < right){
			if(makeChanges)fallingElement.setCenter(center - 1);
			return false;
		}
		
		if(left > right){
			if(makeChanges)fallingElement.setCenter(center + 1);
			return false;
		}
		
		return true;
	}
	
	
	
	@Override
	public void onDisable() {
		if(Bukkit.getScheduler().isCurrentlyRunning(this.getTaskId()) || Bukkit.getScheduler().isQueued(this.getTaskId())){
			this.cancel();
			if(Main.debug) Bukkit.getConsoleSender().sendMessage("canceled task");
		}
		state = GameState.END;
		inv.clear();
		((TetrisGameManager)gameManager).resetInv(player);
	}
	
	
	@Override
	public int getNumPlayers() {
		return numPlayers;
	}
	
	@Override
	public void run() {
		if(Main.debug && !(state == GameState.END)) Bukkit.getConsoleSender().sendMessage(state.toString());
		switch (state){
			case FALLING:
				ArrayList<Integer> slots = new ArrayList<>();
				for(int slot : fallingElement.getSlots()){
					slots.add(slot);
				}
				for(int slot : slots) {
					if (slot + 9 >= inv.getSize() || (!slots.contains(slot+9) && slot+9 >=0 && inv.getItem(slot+9) != null)){
						if(!fullInGrid(fallingElement.getSlots())){
							state = GameState.END;
							nms.updateInventoryTitle(player, "&4Lost!");
							return;
						} else if(!toBreak(true)) {
							state = GameState.PAUSE;
							spawn(pauseTicks);
							return;
						} else {
							return;
						}
					}
				}
				for(int slot : fallingElement.getSlots()){
					if(slot > -1 && slot < inv.getSize())
					inv.setItem(slot, null);
				}
				fallingElement.setCenter(fallingElement.getCenter() + 9);
				ItemStack item = fallingElement.getItem();
				for(int slot : fallingElement.getSlots()){
					if(slot >= 0 && slot < inv.getSize())
					inv.setItem(slot, item);
				}
				player.updateInventory();
				break;
			
			case FILLING:
				int rows = inv.getSize() / 9;
				ArrayList<Integer> fullRows = new ArrayList<>();
				
				rowLoop:
				for(int row = 0; row < rows; row++){
					for(int add = 0; add < 9 ; add++){
						if(inv.getItem(row*9 + add) != null){
							fullRows.add(row);
							continue rowLoop;
						}
					}
				}
				if(fullRows.isEmpty()){
					this.state = GameState.PAUSE;
					spawn(pauseTicks);
					return;
				}
				// will be set definitely, since the ArrayList is not empty
				int firstFilledRow = 0;
				for(int i = 0; i<rows;i++){
					if(fullRows.contains(i)){
						firstFilledRow = i;
						break;
					}
				}
				int firstRowToFill = 0;
				for(int row = firstFilledRow; row<rows;row++){
					if(!fullRows.contains(row)){
						firstRowToFill = row;
					}
				}
				if(Main.debug)Bukkit.getConsoleSender().sendMessage("firstFilledRow :" + firstFilledRow + "    firstRowToFill:" + firstRowToFill);
				if(firstRowToFill == 0){
					this.state = GameState.PAUSE;
					spawn(pauseTicks);
				}
				// let all rows above the first empty one fall down by one
				for(int row = firstRowToFill; row > firstFilledRow ; row--){
					for(int add = 0; add < 9 ;add ++){
						inv.setItem(row*9 + add, inv.getItem((row-1)*9 + add));
					}
				}
				// delete the first filled row (it already fall down by one)
				for(int add = 0; add < 9 ;add ++){
					inv.setItem(firstFilledRow*9 + add, null);
				}
				break;
			
			case PAUSE:
				
				
				break;
			
			case END:
				
				
				break;
			
			default:
				break;
		}
	}
	
	private boolean fullInGrid(int[] slots) {
		for(int slot : slots){
			if(slot < 0) return false;
		}
		return true;
	}
	
	private boolean toBreak(boolean scheduleForBreak) {
		int rows = inv.getSize() / 9;
		ArrayList<Integer> toBreak = new ArrayList<>();
		for(int row = rows - 1; row >=0; row--){
			int inRow = 0;
			for (int slot = row * 9; slot < (row*9 + 9); slot ++){
				if(inv.getItem(slot) != null){
					inRow ++;
				}
			}
			if(Main.debug) Bukkit.getConsoleSender().sendMessage("Tested row " + row + "    toBreak: " + (inRow == 9));
			if(inRow == 9){
				toBreak.add(row);
			}
		}
		if(toBreak.isEmpty()) return false;
		if(!scheduleForBreak) return true;
		
		// schedule for breaking the rows
		state = GameState.PAUSE;
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(gameManager.getPlugin(), new Runnable() {
			@Override
			public void run() {
				for(int row : toBreak){
					for(int add = 0; add < 9; add++) {
						inv.setItem(row*9 + add, null);
					}
				}
				state = GameState.FILLING;
			}
		}, pauseTicks);
		return true;
	}
	
	public void setState(GameState state) {
		this.state = state;
		if(Main.debug) Bukkit.getConsoleSender().sendMessage("set state to: " + state);
	}
	
	public void moveToLeft() {
		if(state != GameState.FALLING) return;
		if(!fullInGrid(fallingElement.getSlots())) return;
		fallingElement.setCenter(fallingElement.getCenter() - 1);
		if(!correctPosition(fallingElement, false)){
			fallingElement.setCenter(fallingElement.getCenter() + 1);
		} else {
			for(int slot : fallingElement.getSlots()){
				inv.setItem(slot + 1, null);
			}
			for(int slot : fallingElement.getSlots()){
				inv.setItem(slot, fallingElement.getItem());
			}
		}
	}
	
	public void turn(int i) {
		if(state != GameState.FALLING) return;
		if(!fullInGrid(fallingElement.getSlots())) return;
		if(i == 1){
			this.fallingElement.setPosition(Position.getNextPosition(fallingElement.getPosition()));
		} else if(i == 3){
			this.fallingElement.setPosition(Position.getLastPosition(fallingElement.getPosition()));
		}
		if(!correctPosition(fallingElement, false)){
			if(i == 1){
				this.fallingElement.setPosition(Position.getLastPosition(fallingElement.getPosition()));
			} else if(i == 3){
				this.fallingElement.setPosition(Position.getNextPosition(fallingElement.getPosition()));
			}
		} else {
			if(i == 1){
				this.fallingElement.setPosition(Position.getLastPosition(fallingElement.getPosition()));
			} else if(i == 3){
				this.fallingElement.setPosition(Position.getNextPosition(fallingElement.getPosition()));
			}
			for(int slot : fallingElement.getSlots()){
				inv.setItem(slot, null);
			}
			if(i == 1){
				this.fallingElement.setPosition(Position.getNextPosition(fallingElement.getPosition()));
			} else if(i == 3){
				this.fallingElement.setPosition(Position.getLastPosition(fallingElement.getPosition()));
			}
			for(int slot : fallingElement.getSlots()){
				inv.setItem(slot, fallingElement.getItem());
			}
		}
	}
	
	public void moveToRight() {
		if(state != GameState.FALLING) return;
		if(!fullInGrid(fallingElement.getSlots())) return;
		int center = fallingElement.getCenter();
		fallingElement.setCenter(center + 1);
		if(Main.debug) Bukkit.getConsoleSender().sendMessage("checking position of falling");
		if(!correctPosition(fallingElement, false)){
			fallingElement.setCenter(center);
		} else {
			for(int slot : fallingElement.getSlots()){
				inv.setItem(slot - 1, null);
			}
			for(int slot : fallingElement.getSlots()){
				inv.setItem(slot, fallingElement.getItem());
			}
		}
	}
	
	public void moveDown() {
		if(state != GameState.FALLING) return;
		
		int i;
		main:
		for(i = 0; i < inv.getSize() / 9; i++){
			second:
			for(int slot : fallingElement.getSlots()){
				if(slot + i * 9 >= inv.getSize()) break main;
				for(int slot2 : fallingElement.getSlots()){
					if(slot + i * 9 == slot2) continue second;
				}
				if(inv.getItem(slot + i * 9) != null) break main;
			}
		}
		
		for(int slot : fallingElement.getSlots()){
			inv.setItem(slot, null);
		}
		
		fallingElement.setCenter(fallingElement.getCenter() + (i-1) * 9);
		
		for(int slot : fallingElement.getSlots()){
			inv.setItem(slot, fallingElement.getItem());
		}
		
		if(!toBreak(true)) {
			state = GameState.PAUSE;
			spawn(pauseTicks);
		}
	}
}