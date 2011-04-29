package com.aranai.dungeonator;

import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.aranai.dungeonator.datastore.DungeonDataStore;
import com.aranai.dungeonator.generator.DungeonChunkEditor;

/**
 * The Dungeonator JavaPlugin for Bukkit.
 */
public class Dungeonator extends JavaPlugin {
	
	private Logger log;
	
	/** The world listener. Used for detecting chunk load and unload events. */
	private DWorldListener worldListener;
	
	/** The player listener. Used for detecting player commands. */
	private DPlayerListener playerListener;
	
	/** The dungeon data store. */
	private DungeonDataStore dataStore;
	
	/** The dungeon data manager. */
	private DungeonDataManager dataManager;
	
	/** The dungeon chunk manager. */
	private DungeonChunkManager chunkManager;
	
	/** The dungeon chunk editor. */
	private DungeonChunkEditor chunkEditor;
	
	/** Debug field: flattenOn: to flatten, or not to flatten */
	public boolean flattenOn = false;
	
	/** Debug field: py: player y at time of flattening */
	public int py = 1000;

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
	}

	/* (non-Javadoc)
	 * @see org.bukkit.plugin.Plugin#onEnable()
	 */
	@Override
	public void onEnable() {
		// Get logger
		log = this.getServer().getLogger();
		
		// Initialize data store
		dataStore = new DungeonDataStore(); // TODO: Use a real data store so we can actually, y'know, store data
		
		// Initialize data manager
		dataManager = new DungeonDataManager(this, dataStore);
		
		// Initialize chunk manager
		chunkManager = new DungeonChunkManager(dataManager);
		
		// Enable message
        PluginDescriptionFile pdfFile = this.getDescription();
		log.info("[Dungeonator] version ["+ pdfFile.getVersion()+ "] loaded");
		
		// Initialize world listener
		worldListener = new DWorldListener(this);
		
		// Initialize player listener
		playerListener = new DPlayerListener(this);
		
		// Register events
		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvent(Event.Type.CHUNK_LOAD, worldListener, Priority.Normal, this);
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
	public Logger getLogger()
	{
		return log;
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
	public DungeonChunkEditor getChunkEditor()
	{
		return chunkEditor;
	}
}
