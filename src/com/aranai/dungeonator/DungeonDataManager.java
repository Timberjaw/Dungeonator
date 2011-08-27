package com.aranai.dungeonator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Logger;

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
	private HashMap<String,Vector<Byte>> adjacencyCache;
	
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
		this.adjacencyCache = new HashMap<String,Vector<Byte>>();
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
	
	public boolean saveChunk(DungeonChunk chunk)
	{
		try {
			return dataStore.saveChunk(chunk);
		} catch (DataStoreSaveException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public boolean saveRoom(DungeonRoom room)
	{
		try {
			return dataStore.saveRoom(room);
		} catch (DataStoreSaveException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public DungeonRoom[] getRoomsForNewChunk(DungeonChunk chunk)
	{
		String fullPath = "";
		DungeonRoom[] rooms = new DungeonRoom[16];
		
		for(int i = 0; i < 16; i++)
		{
			// Get surrounding doorways
			Vector<Byte> doorways = this.getAdjacentDoorways(chunk.getWorldName(), chunk.getX(), chunk.getZ(), i);
			
			// Get a random room
			try {
				rooms[i] = dataStore.getLibraryRoomRandom(doorways);
				
				// Make sure we actually got a result, and bail out if we didn't
				if(rooms[i] == null) { return null; }
				
				// Get the full path to the source tile
				fullPath = Dungeonator.TileFolderPath+rooms[i].getFilename()+".nbt";
				
				// Check cache
				if(roomCache.containsKey(fullPath))
				{
					rooms[i] = roomCache.get(fullPath);
					//System.out.println("Cache hit for "+fullPath);
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
		
		// Second loop to do some cleanup and avoid cache issues
		for(int i = 0; i < 16; i++)
		{
			// Set chunk
			rooms[i].setDungeonChunk(chunk);
		}
		
		return rooms;
	}
	
	public Vector<Byte> getAdjacentDoorways(String world, int x, int z, int y)
	{
		Vector<Byte> doors = new Vector<Byte>();
		Logger log = Dungeonator.getLogger();
		
		// We need to get doorway info from 0-6 rooms (N,E,S,W,U,D)
		// +X is South, -X is North, +Z is West, -Z is East
		try {
			// Get northern neighbor
			DungeonRoom neighborN = dataStore.getRoom(world, x-1, y, z);
			
			if(neighborN.isLoaded())
			{
				// Check southern doorways of northern neighbor
				if(neighborN.hasDoorway(Direction.S)) { doors.add(Direction.S); }
				if(neighborN.hasDoorway(Direction.SSE)) { doors.add(Direction.SSE); }
				if(neighborN.hasDoorway(Direction.SSW)) { doors.add(Direction.SSW); }
			}
			
			// Get eastern neighbor
			DungeonRoom neighborE = dataStore.getRoom(world, x, y, z-1);
			
			if(neighborE.isLoaded())
			{
				// Check western doorways of eastern neighbor
				if(neighborE.hasDoorway(Direction.W)) { doors.add(Direction.W); }
				if(neighborE.hasDoorway(Direction.WNW)) { doors.add(Direction.WNW); }
				if(neighborE.hasDoorway(Direction.WSW)) { doors.add(Direction.WSW); }
			}
			
			// Get southern neighbor
			DungeonRoom neighborS = dataStore.getRoom(world, x, x+1, z);
			
			if(neighborS.isLoaded())
			{
				// Check northern doorways of southern neighbor
				if(neighborS.hasDoorway(Direction.N)) { doors.add(Direction.N); }
				if(neighborS.hasDoorway(Direction.NNW)) { doors.add(Direction.NNW); }
				if(neighborS.hasDoorway(Direction.NNE)) { doors.add(Direction.NNE); }
			}
			
			// Get western neighbor
			DungeonRoom neighborW = dataStore.getRoom(world, x, y, z+1);
			
			if(neighborW.isLoaded())
			{
				// Check eastern doorways of western neighbor
				if(neighborW.hasDoorway(Direction.E)) { doors.add(Direction.E); }
				if(neighborW.hasDoorway(Direction.ENE)) { doors.add(Direction.ENE); }
				if(neighborW.hasDoorway(Direction.ESE)) { doors.add(Direction.ESE); }
			}
			
			// Get upper neighbor
			DungeonRoom neighborU = dataStore.getRoom(world, x, y+1, z);
			
			if(neighborU.isLoaded())
			{
				// Check lower doorway of upper neighbor
				if(neighborU.hasDoorway(Direction.DOWN)) { doors.add(Direction.DOWN); }
			}
			
			// Get lower neighbor
			DungeonRoom neighborD = dataStore.getRoom(world, x, y-1, z);
			
			if(neighborD.isLoaded())
			{
				// Check upper doorway of lower neighbor
				if(neighborD.hasDoorway(Direction.UP)) { doors.add(Direction.UP); }
			}
		} catch (DataStoreGetException e) { e.printStackTrace(); }
		
		//log.info("Found "+doors.size()+" neighboring doors for "+world+"<"+x+","+y+","+z+">");
		
		return doors;
	}
}
