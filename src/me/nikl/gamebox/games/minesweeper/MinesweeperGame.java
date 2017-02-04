package me.nikl.gamebox.games.minesweeper;

import io.netty.util.internal.ConcurrentSet;
import me.nikl.gamebox.Language;
import me.nikl.gamebox.Main;
import me.nikl.gamebox.games.AGame;
import me.nikl.gamebox.nms.NMSUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Wool;

import java.util.Arrays;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * Created by niklas on 10/30/16.
 *
 * MinesweeperGame
 */
public class MinesweeperGame extends AGame{
	
	
	private ItemStack empty, flagged, mine, covered, number, toGameGUI;
	private Inventory inv;
	private int num, bombsNum, flags;
	private String[] positions;
	private boolean[] cov; //Array with covered/not covered info
	private boolean changingInv;
	private String displayFlags, displayTime, currentState;
	private UUID player;
	private Language lang;
	private boolean started;
	private NMSUtil updater;
	
	// save the game config
	private FileConfiguration config;
	
	private MinesweeperGameManager gManager;
	
	private Main plugin;
	private MinesweeperGameTimer timer;
	
	// store the slot that may contain the toGameGUI button after the game
	private int toGUIslot;
	
	public MinesweeperGame(Main plugin, UUID player, MinesweeperGameManager gManager){
		super(player, gManager);
		this.gManager = gManager;
		this.config = gManager.getGameConfig();
		this.updater = plugin.getNMS();
		this.setStarted(false);
		this.player = player;
		this.setChangingInv(false);
		this.num = 54;
		this.plugin = plugin;
		this.lang = plugin.lang;
		this.bombsNum = 0;
		this.displayTime = "00:00";
		this.toGUIslot = -1;
		if(this.config == null){
			Bukkit.getConsoleSender().sendMessage(plugin.chatColor(Main.prefix + " &4Failed to load MinesweeperGame config!"));
			gManager.disableGame(this); // ToDo!
			return;
		}
		if(this.config.isInt("mines")){
			this.bombsNum = this.config.getInt("mines");
		}
		if(bombsNum < 1 || bombsNum > 30){
			Bukkit.getConsoleSender().sendMessage(Main.prefix + " Check the config, a not valid number of mines was set. (MinesweeperGame)");
			this.bombsNum = 8;
		}
		if(!getMaterials()){
			Bukkit.getConsoleSender().sendMessage(plugin.chatColor(Main.prefix + " &4Failed to load materials from config (MinesweeperGame)"));
			Bukkit.getConsoleSender().sendMessage(plugin.chatColor(Main.prefix + " &4Using default materials"));
			this.flagged = new ItemStack(Material.SIGN);
			ItemMeta metaFlagged = flagged.getItemMeta();
			metaFlagged.setDisplayName("Flag");
			flagged.setItemMeta(metaFlagged);
			flagged.setAmount(1);
			this.covered = new ItemStack(Material.STAINED_GLASS_PANE);
			covered.setDurability((short) 8);
			ItemMeta metaCovered = covered.getItemMeta();
			metaCovered.setDisplayName("Cover");
			covered.setItemMeta(metaCovered);
			covered.setAmount(1);
			this.mine = new ItemStack(Material.TNT);
			ItemMeta metaMine = mine.getItemMeta();
			metaMine.setDisplayName("Boooom");
			mine.setItemMeta(metaMine);
			this.number = new Wool(DyeColor.ORANGE).toItemStack();
			ItemMeta metaNumber = number.getItemMeta();
			metaNumber.setDisplayName("Warning");
			number.setItemMeta(metaNumber);
		}
		
		this.toGameGUI = new ItemStack(Material.BIRCH_DOOR_ITEM, 1);
		ItemMeta meta = toGameGUI.getItemMeta();
		meta.setDisplayName(plugin.chatColor(lang.BUTTON_GAME_GUI));
		toGameGUI.setItemMeta(meta);
		
		this.flags=0;
		this.positions = new String[num];
		this.cov = new boolean[num];
		for(int i = 0 ;i<num;i++){
			positions[i] = "0";
			cov[i]=true;
		}
		this.inv = Bukkit.getServer().createInventory(null, num, ChatColor.translateAlternateColorCodes('&', lang.MINESWEEPER_TITLE_BEGINNING));
		createGame();
		getPlayers()[0].openInventory(inv);
	}
	
	@Override
	public void onDisable() {
		cancelTimer();
	}
	
	@Override
	public Player[] getPlayers() {
		Player[] player = new Player[1];
		player[0] = Bukkit.getPlayer(this.player);
		return player;
	}
	
	@Override
	public int getNumPlayers() {
		return numPlayers;
	}
	
	
	private void uncoverEmpty(int slot) {
		Set<Integer> uncover = new ConcurrentSet<>();
		Set<Integer> newUncover = new ConcurrentSet<>();
		
		uncover.add(slot);
		int currentSlot = slot;
		
		int[] add = getSurroundings(slot);
		for(int i = 0; i < add.length; i++){
			add[i] = add[i] + currentSlot;
			if(!uncover.contains(add[i])){
				newUncover.add(add[i]);
			}
		}
		while (!newUncover.isEmpty()){
			for(int checkSlot : newUncover){
				if (!uncover.contains(checkSlot)){
					uncover.add(checkSlot);
					newUncover.remove(checkSlot);
					if(positions[checkSlot].equalsIgnoreCase("0")){
						int[] newAdd = getSurroundings(checkSlot);
						for(int i = 0; i < newAdd.length; i++){
							newAdd[i] = newAdd[i] + checkSlot;
							if(!uncover.contains(newAdd[i]) && !newUncover.contains(newAdd[i])){
								newUncover.add(newAdd[i]);
							}
						}
					}
				}
			}
		}
		for(int uncoverSlot : uncover){
			int amount = 0;
			try {
				amount = Integer.parseInt(positions[uncoverSlot]);
			} catch (NumberFormatException e) {
				Bukkit.getLogger().severe("Something went wrong while building the game");
			}
			if(amount == 0){
				this.inv.setItem(uncoverSlot, empty);
			} else {
				number.setAmount(amount);
				this.inv.setItem(uncoverSlot, number);
			}
			cov[uncoverSlot] = false;
		}
	}
	
	public int[] getSurroundings(int slot){
		int[] add;
		if(slot == 0){// corner left top
			add = new int[3];
			add[0] = 1; add[1] = 9; add[2] = 10;
		} else if (slot == 8){// corner top right
			add = new int[3];
			add[0] = -1; add[1] = 8; add[2] = 9;
		} else if (slot == 45){// corner bottom left
			add = new int[3];
			add[0] = -9; add[1] = -8; add[2] = 1;
		} else if (slot == 53){// corner bottom right
			add = new int[3];
			add[0] = -10; add[1] = -9; add[2] = -1;
		} else if(slot>0 && slot<8){// edge top
			add = new int[5];
			add[0] = -1; add[1] = 1; add[2] = 8; add[3] = 9; add[4] = 10;
		} else if(slot == 17 || slot == 26 || slot == 35 || slot == 44){// edge right
			add = new int[5];
			add[0] = -10; add[1] = -9; add[2] = -1; add[3] = 8; add[4] = 9;
		} else if(slot>45 && slot<53){// edge bottom
			add = new int[5];
			add[0] = -1; add[1] = -10; add[2] = -9; add[3] = -8; add[4] = 1;
		} else if(slot == 9 || slot == 18 || slot == 27 || slot == 36){// edge left
			add = new int[5];
			add[0] = -9; add[1] = -8; add[2] = 1; add[3] = 9; add[4] = 10;
		} else {
			add = new int[8];
			add[0] = -10; add[1] = -9; add[2] = -8; add[3] = -1; add[4] = 1; add[5] = 8; add[6] = 9; add[7] = 10;
		}
		return add;
	}
	
	
	
	private Boolean getMaterials() {
		Boolean worked = true;
		
		Material mat = null;
		int data = 0;
		for(String key : Arrays.asList("cover", "warning", "mine", "flag")){
			if(!this.config.isSet("materials." + key)) return false;
			String value = this.config.getString("materials." + key);
			String[] obj = value.split(":");
			String name = "default";
			boolean named = false;
			if(this.config.isSet("displaynames." + key) && this.config.isString("displaynames." + key)){
				name = this.config.getString("displaynames." + key);
				named = true;
			}
			
			
			if (obj.length == 2) {
				try {
					mat = Material.matchMaterial(obj[0]);
				} catch (Exception e) {
					worked = false; // material name doesn't exist
				}
				
				try {
					data = Integer.valueOf(obj[1]);
				} catch (NumberFormatException e) {
					worked = false; // data not a number
				}
			} else {
				try {
					mat = Material.matchMaterial(value);
				} catch (Exception e) {
					worked = false; // material name doesn't exist
				}
			}
			if(mat == null) return false;
			if(key.equals("cover")){
				this.covered = new ItemStack(mat, 1);
				if (obj.length == 2) covered.setDurability((short) data);
				ItemMeta metaCovered = covered.getItemMeta();
				metaCovered.setDisplayName("Cover");
				if(named)
					metaCovered.setDisplayName(plugin.chatColor(name));
				covered.setItemMeta(metaCovered);
				covered.setAmount(1);
				
			} else if(key.equals("warning")){
				this.number = new ItemStack(mat, 1);
				if (obj.length == 2) number.setDurability((short) data);
				ItemMeta metaNumber = number.getItemMeta();
				metaNumber.setDisplayName("Warning");
				if(named)
					metaNumber.setDisplayName(plugin.chatColor(name));
				number.setItemMeta(metaNumber);
				
			} else if(key.equals("mine")){
				this.mine = new ItemStack(mat, 1);
				if (obj.length == 2) mine.setDurability((short) data);
				ItemMeta metaMine = mine.getItemMeta();
				metaMine.setDisplayName("Boooom");
				if(named)
					metaMine.setDisplayName(plugin.chatColor(name));
				mine.setItemMeta(metaMine);
				
			} else if(key.equals("flag")){
				this.flagged = new ItemStack(mat, 1);
				if (obj.length == 2) flagged.setDurability((short) data);
				ItemMeta metaFlagged = flagged.getItemMeta();
				metaFlagged.setDisplayName("Flag");
				if(named)
					metaFlagged.setDisplayName(plugin.chatColor(name));
				flagged.setItemMeta(metaFlagged);
				flagged.setAmount(1);
			}
		}
		
		this.empty = new ItemStack(Material.AIR);
		return worked;
	}
	
	private void createGame(){
		Random r = new Random();
		int rand = r.nextInt(num);
		int count = 0;
		while(count < bombsNum){
			if(positions[rand].equals("mine")){
				rand = r.nextInt(num);
				continue;
			}
			positions[rand] = "mine";
			count++;
			rand = r.nextInt(num);
		}
		for(int i=0;i<num;i++){
			if(positions[i].equals("mine")){
				continue;
			}
			positions[i] = getNextMines(i);
		}
		
		for(int i=0;i<num;i++){
			this.inv.setItem(i, covered);
		}
		
		
		
		
		
	}
	
	private String getNextMines(int i) {
		int count = 0;
		int[] add = getSurroundings(i);
		for (int a : add){
			if(!(i+a >= 0 && i+a < num)){
				continue;
			}
			if(positions[i+a].equals("mine")){
				count++;
			}
		}
		return String.valueOf(count);
	}
	
	public void reveal(){
		for(int i=0;i<num;i++){
			cov[i] = false;
			if(positions[i].equals("mine")){
				this.inv.setItem(i, mine);
			} else {
				int amount = 0;
				try {
					amount = Integer.parseInt(positions[i]);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
				if(amount == 0){
					this.toGUIslot = i;
					this.inv.setItem(i, empty);
					continue;
				}
				number.setAmount(amount);
				this.inv.setItem(i, number);
			}
		}
		final int lastEmptySlotFinal = toGUIslot;
		if(lastEmptySlotFinal > 0) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				@Override
				public void run() {
					inv.setItem(lastEmptySlotFinal, toGameGUI);
				}
			}, 20);
		}
		
	}
	
	public Inventory getInv(){
		return this.inv;
	}
	
	public Boolean isCovered(int slot){
		return this.cov[slot];
	}
	
	public Boolean isFlaged(ItemStack itemS){
		return flagged.getType().equals(itemS.getType()) && flagged.getData().equals(itemS.getData());
	}
	
	public Boolean isEmpty(int slot){
		return inv.getItem(slot) == null;
	}
	
	public void setFlagged(int slot) {
		this.inv.setItem(slot, flagged);
		flags++;
		this.displayFlags = "   &2"+flags+"&r/&4"+bombsNum;
		currentState = lang.MINESWEEPER_TITLE_INGAME.replaceAll("%state%", displayFlags).replaceAll("%timer%", displayTime);
		setState(currentState);
	}
	
	public void deFlag(int slot) {
		this.inv.setItem(slot, covered);
		flags--;
		this.displayFlags = "   &2"+flags+"&r/&4"+bombsNum;
		currentState = lang.MINESWEEPER_TITLE_INGAME.replaceAll("%state%", displayFlags).replaceAll("%timer%", displayTime);
		setState(currentState);
	}
	
	public void uncover(int slot){
		if(positions[slot].equals("mine")){
			cancelTimer();
			reveal();
			setState(lang.MINESWEEPER_TITLE_LOST);
		} else {
			int amount = 0;
			try {
				amount = Integer.parseInt(positions[slot]);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
			if(amount == 0){
				this.inv.setItem(slot, empty);
				if(gManager.automaticReveal) {
					uncoverEmpty(slot);
				}
			} else {
				number.setAmount(amount);
				this.inv.setItem(slot, number);
			}
			cov[slot] = false;
		}
	}
	
	public boolean isWon() {
		int count = 0;
		for(int i=0;i<num;i++){
			if(cov[i]){
				count++;
			}
		}
		if(count == bombsNum){
			return true;
		}
		return false;
	}
	
	public void setState(String state){
		Player playerP = Bukkit.getPlayer(player);
		if(playerP == null){
			gManager.removeGame(player);
		}
		updater.updateInventoryTitle(Bukkit.getPlayer(player), ChatColor.translateAlternateColorCodes('&',state));
	}
	
	public String getState(){
		return inv.getName();
	}
	
	public void setState() {
		this.displayFlags = "   &2"+flags+"&r/&4"+bombsNum;
		currentState = lang.MINESWEEPER_TITLE_INGAME.replaceAll("%state%", displayFlags).replaceAll("%timer%", displayTime);
		setState(currentState);
	}
	
	public boolean isChangingInv() {
		return changingInv;
	}
	
	public void setChangingInv(boolean changingInv) {
		this.changingInv = changingInv;
	}
	
	public void setTime(String string) {
		this.displayTime = string;
		currentState = lang.MINESWEEPER_TITLE_INGAME.replaceAll("%state%", displayFlags).replaceAll("%timer%", displayTime);
		setState(currentState);
		//showGame(Bukkit.getPlayer(player));
	}
	
	public void startTimer() {
		this.timer = new MinesweeperGameTimer(this);
	}
	
	public String getDisplayTime() {
		return this.displayTime;
	}
	
	public void cancelTimer() {
		if(this.timer != null){
			this.timer.cancel();
		}
	}
	
	public boolean isStarted() {
		return started;
	}
	
	public void setStarted(boolean started) {
		this.started = started;
	}
	
	public void start() {
		setStarted(true);
		startTimer();
		setState();
	}
	
	public boolean isToGameGUI(int slot) {
		return slot == toGUIslot;
	}
}
