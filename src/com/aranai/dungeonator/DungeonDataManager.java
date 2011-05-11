package com.aranai.dungeonator;

import com.aranai.dungeonator.datastore.*;
import com.aranai.dungeonator.dungeonchunk.DungeonChunk;

/**
 * Abstracts all data store access for Dungeonator.
 */
public class DungeonDataManager {

	/** The Dungeonator instance. */
	private Dungeonator plugin;
	
	/** The dungeon data store. This may be a MySQL data store, a flatfile store, or whatever. */
	private DungeonDataStore dataStore;
	
	/**
	 * Instantiates the DungeonDataManager.
	 *
	 * @param plugin the Dungeonator instance
	 */
	public DungeonDataManager(Dungeonator plugin, DungeonDataStore dataStore)
	{
		this.plugin = plugin;
		this.dataStore = dataStore;
	}
	
	/**
	 * Gets the requested DungeonChunk from the data store.
	 *
	 * @param hash the hash for the chunk
	 * @return the DungeonChunk if found, or null if an error occurs
	 */
	public DungeonChunk getChunk(String hash)
	{
		DungeonChunk chunk = null;
		
		try
		{
			chunk = dataStore.getChunk(hash);
		}
		catch(DataStoreGetException e)
		{
			/*
			 * Failed to retrieve the requested chunk. Log the error, mourn our
			 * loss, and move on with our lives.
			 */
			
			Dungeonator.getLogger().severe("Failed to get requested DungeonChunk at Location: "+ e.getLocation() +". Reason: " + e.getReason());
		}
		
		return chunk;
	}
}
