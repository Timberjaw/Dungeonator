package com.aranai.dungeonator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;

import org.jnbt.ByteArrayTag;
import org.jnbt.CompoundTag;
import org.jnbt.NBTInputStream;

import com.aranai.dungeonator.datastore.*;
import com.aranai.dungeonator.dungeonchunk.DungeonChunk;
import com.aranai.dungeonator.dungeonchunk.DungeonRoom;

/**
 * Abstracts all data store access for Dungeonator.
 */
public class DungeonDataManager {

	/** The Dungeonator instance. */
	private Dungeonator plugin;
	
	/** The dungeon data store. This may be a MySQL data store, a flatfile store, or whatever. */
	private IDungeonDataStore dataStore;
	
	private Hashtable<String,DungeonRoom> roomCache;
	
	/**
	 * Instantiates the DungeonDataManager.
	 *
	 * @param plugin the Dungeonator instance
	 */
	public DungeonDataManager(Dungeonator plugin, IDungeonDataStore dataStore)
	{
		this.plugin = plugin;
		this.dataStore = dataStore;
		this.dataStore.initialize(plugin);
		
		this.roomCache = new Hashtable<String,DungeonRoom>();
	}
	
	/**
	 * Gets the requested DungeonChunk from the data store.
	 *
	 * @param hash the hash for the chunk
	 * @return the DungeonChunk if found, or null if an error occurs
	 */
	public DungeonChunk getChunk(String world, int x, int z)
	{
		DungeonChunk chunk = null;
		
		try
		{
			chunk = dataStore.getChunk(world, x, z);
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
	
	/**
	 * Save a room to the library.
	 *
	 * @param room the room to save
	 */
	public void saveLibraryRoom(DungeonRoom room)
	{
		try {
			// Save the room
			dataStore.saveLibraryRoom(room);
			
			// Notify the editor
			plugin.getChunkEditor().getActiveEditor().sendMessage("Saved room '"+room.getName()+"["+room.getFilename()+"] to Library.");
		} catch (DataStoreSaveException e) {
			// Failed to save room
			e.printStackTrace();
		}
	}
	
	public DungeonRoom[] getRoomsForNewChunk(DungeonChunk chunk)
	{
		String fullPath = "";
		DungeonRoom[] rooms = new DungeonRoom[16];
		
		for(int i = 0; i < 16; i++)
		{
			// Get a random room
			try {
				rooms[i] = dataStore.getLibraryRoomRandom();
				
				if(rooms[i] == null) { return null; }
				
				fullPath = Dungeonator.TileFolderPath+File.separator+rooms[i].getFilename()+".nbt";
				
				// Check cache
				if(roomCache.containsKey(fullPath))
				{
					rooms[i] = roomCache.get(fullPath);
					System.out.println("Cache hit for "+fullPath);
					continue;
				}
				
				// Get schematic
				CompoundTag schematic = null;
				try {
					// Open file input stream
					FileInputStream fis = new FileInputStream(fullPath);
					
					try {
						// Open NBT input stream
						NBTInputStream nis = new NBTInputStream(fis);
						
						// Read NBT data
						org.jnbt.Tag tag = nis.readTag();
						
						if(tag instanceof CompoundTag)
						{
							schematic = (CompoundTag)tag;
						}
					} catch (IOException e) { e.printStackTrace(); }
				} catch (FileNotFoundException e) { e.printStackTrace(); }
				
				// Verify the data was loaded
				if(schematic != null)
				{
					// Set blocks and block data
					rooms[i].setRawBlocks(((ByteArrayTag)schematic.getValue().get("blocks")).getValue());
					//rooms[i].setRawBlockData(((ByteArrayTag)schematic.getValue().get("blockData")).getValue());
					
					roomCache.put(fullPath, rooms[i]);
				}
			} catch (DataStoreGetException e) { e.printStackTrace(); return null; }
		}
		
		return rooms;
	}
}
