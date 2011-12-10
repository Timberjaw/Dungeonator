package com.aranai.dungeonator;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.aranai.dungeonator.amt.ThemeManager;
import com.aranai.dungeonator.datastore.IDungeonDataStore;
import com.aranai.dungeonator.datastore.SqliteDungeonDataStore;
import com.aranai.dungeonator.dungeonchunk.DungeonChunkManager;
import com.aranai.dungeonator.dungeonmaster.DungeonMaster;
import com.aranai.dungeonator.generator.DungeonRoomEditor;

/**
 * The Dungeonator Plugin for Bukkit.
 * A wonderland of cryptic corridors, terrible traps,
 * claustrophobic chambers, malevolent monsters,
 * and many more magical mysteries 
 */
public class Dungeonator extends JavaPlugin {
	
	private static Logger log;
	
	/** The world listener. Used for detecting chunk load and unload events. */
	private DWorldListener worldListener;
	
	/** The player listener. Used for detecting player commands. */
	private DPlayerListener playerListener;
	
	/** The entity listener. Used for detecting entity events. */
	private DEntityListener entityListener;
	
	/** The dungeon data store. */
	private IDungeonDataStore dataStore;
	
	/** The dungeon data manager. */
	private DungeonDataManager dataManager;
	
	/** The theme manager. */
	private ThemeManager themeManager;
	
	/** The dungeon chunk manager. */
	private DungeonChunkManager chunkManager;
	
	/** The dungeon room editor. */
	private DungeonRoomEditor roomEditor;
	
	/** The DM handler */
	private DungeonMaster dungeonMaster;
	
	/** Debug field: flattenOn: to flatten, or not to flatten */
	public boolean flattenOn = false;
	
	/** Base data folder path */
	public static String BaseFolderPath;
	
	/** Tile folder path */
	public static String TileFolderPath;
	
	/** Processed Tile folder path */
	public static String ProcessedTileFolderPath;
	
	/** Widget folder path */
	public static String WidgetFolderPath;

	/**
	 * Instantiates the JavaPlugin.
	 */
	public Dungeonator() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.bukkit.plugin.Plugin#onDisable()
	 */
	@Override
	public void onDisable() {
		// Close DB connection
		this.dataStore.shutdown();
	}

	/* (non-Javadoc)
	 * @see org.bukkit.plugin.Plugin#onEnable()
	 */
	@Override
	public void onEnable() {
		// Get logger
		log = this.getServer().getLogger();
		
		// Set folder paths
		try {
			// Base folder path
			Dungeonator.BaseFolderPath = this.getDataFolder().getCanonicalPath().toString()+File.separator;
			// Tile folder path
			Dungeonator.TileFolderPath = Dungeonator.BaseFolderPath+"tiles"+File.separator;
			// Processed Tile folder path
			Dungeonator.ProcessedTileFolderPath = Dungeonator.TileFolderPath+"processed"+File.separator;
			// Widget folder path
			Dungeonator.WidgetFolderPath = Dungeonator.BaseFolderPath+"widgets"+File.separator;
			
			// Make sure we have local folders for our database and such
			if (!new File(Dungeonator.BaseFolderPath).exists()) {
				try {
					(new File(Dungeonator.BaseFolderPath)).mkdir();
				} catch (Exception e) {
					log.log(Level.SEVERE, "[Dungeonator]: Unable to create plugin folder.");
				}
			}
			if (!new File(Dungeonator.TileFolderPath).exists()) {
				try {
					(new File(Dungeonator.TileFolderPath)).mkdir();
				} catch (Exception e) {
					log.log(Level.SEVERE, "[Dungeonator]: Unable to create tile folder.");
				}
			}
			if (!new File(Dungeonator.WidgetFolderPath).exists()) {
				try {
					(new File(Dungeonator.WidgetFolderPath)).mkdir();
				} catch (Exception e) {
					log.log(Level.SEVERE, "[Dungeonator]: Unable to create widget folder.");
				}
			}
		} catch (IOException e) { e.printStackTrace(); }
		
		// Initialize data store
		dataStore = new SqliteDungeonDataStore();
		
		// Initialize data manager
		dataManager = new DungeonDataManager(this, dataStore);
		
		// Initialize theme manager
		themeManager = new ThemeManager();
		
		// Initialize chunk manager
		chunkManager = new DungeonChunkManager(dataManager);
		
		// Initialize chunk editor
		roomEditor = new DungeonRoomEditor(this);
		
		// Initialize DM
		dungeonMaster = new DungeonMaster();
		
		// Enable message
        PluginDescriptionFile pdfFile = this.getDescription();
		log.info("[Dungeonator] version ["+ pdfFile.getVersion()+ "] loaded");
		
		// Initialize world listener
		worldListener = new DWorldListener(this);
		
		// Initialize player listener
		playerListener = new DPlayerListener(this);
		
		// Initialize entity listener
		entityListener = new DEntityListener(this);
		
		/*
		 * Register events
		 */
		
		PluginManager pm = this.getServer().getPluginManager();
		
		// World load
		pm.registerEvent(Event.Type.WORLD_INIT, worldListener, Priority.Normal, this);
		
		// Chunk Load
		pm.registerEvent(Event.Type.CHUNK_LOAD, worldListener, Priority.Normal, this);
		
		// Player Respawn
		pm.registerEvent(Event.Type.PLAYER_RESPAWN, playerListener, Priority.Normal, this);
		
		// Entity Damage
		pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Priority.Normal, this);
	}
	
	@Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
    	return this.playerListener.onCommand(sender, command, commandLabel, args);
    }
	
	/**
	 * Gets the Dungeonator logger instance.
	 *
	 * @return the logger
	 */
	public static Logger getLogger()
	{
		return Dungeonator.log;
	}
	
	/**
	 * Gets the theme manager.
	 *
	 * @return the theme manager
	 */
	public ThemeManager getThemeManager()
	{
		return themeManager;
	}
	
	/**
	 * Gets the chunk manager.
	 *
	 * @return the chunk manager
	 */
	public DungeonChunkManager getChunkManager()
	{
		return chunkManager;
	}
	
	/**
	 * Gets the chunk editor.
	 *
	 * @return the chunk editor
	 */
	public DungeonRoomEditor getChunkEditor()
	{
		return roomEditor;
	}
	
	/**
	 * Gets the data manager.
	 *
	 * @return the data manager
	 */
	public DungeonDataManager getDataManager()
	{
		return dataManager;
	}
	
	public DungeonMaster getDungeonMaster()
	{
		return dungeonMaster;
	}
}
