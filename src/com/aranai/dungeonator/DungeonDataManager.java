package com.aranai.dungeonator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

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
	
	private Hashtable<String,CompoundTag> roomCache;
	
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
		
		this.roomCache = new Hashtable<String,CompoundTag>();
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
			boolean success = dataStore.saveChunk(chunk);
			if(success)
			{
				plugin.getChunkManager().setChunkGenerated(chunk.getWorldName(), chunk.getX(), chunk.getZ(), true);
			}
		} catch (DataStoreSaveException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public boolean saveRooms(DungeonRoom[] rooms)
	{
		try {
			return dataStore.saveRooms(rooms);
		} catch (DataStoreSaveException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public boolean saveRoom(DungeonRoom room)
	{
		DungeonRoom[] rooms = {room};
		return this.saveRooms(rooms);
	}
	
	public DungeonRoom[] getRoomsForChunk(DungeonChunk chunk)
	{
		String fullPath = "";
		DungeonRoom[] rooms = null;
		
		try {
			// Get the rooms for the specified chunk
			rooms = dataStore.getChunkRooms(chunk.getWorldName(), chunk.getX(), chunk.getZ());
			
			if(rooms == null)
			{
				return null;
			}
			
			// Check for existing schematics or load schematics from file
			for(int i = 0; i < 16; i++)
			{
				if(rooms[i] == null)
				{
					System.out.println("No room for {"+chunk.getWorldName()+","+chunk.getX()+","+chunk.getZ()+"}");
					continue;
				}
				// Get the full path to the source tile
				fullPath = Dungeonator.TileFolderPath+rooms[i].getFilename()+".nbt";
				
				CompoundTag schematic = this.getSchematic(fullPath);
				
				// Verify the data was loaded
				if(schematic != null)
				{
					// Set blocks and block data
					rooms[i].setRawBlocks(((ByteArrayTag)schematic.getValue().get("blocks")).getValue());
					rooms[i].setRawBlockData(((ByteArrayTag)schematic.getValue().get("blockData")).getValue());
				}
			}
		} catch (DataStoreGetException e) { e.printStackTrace(); }
		
		return rooms;
	}
	
	/**
	 * Gets a list of random rooms for a new chunk.
	 *
	 * @param chunk the chunk
	 * @return the rooms for the new chunk
	 */
	public DungeonRoom[] getRoomsForNewChunk(DungeonChunk chunk)
	{
		String fullPath = "";
		DungeonRoom[] rooms = new DungeonRoom[16];
		
		Vector<Byte>[] doorways = this.getAdjacentDoorways(chunk.getWorldName(), chunk.getX(), chunk.getZ());
		
		for(int i = 0; i < 16; i++)
		{
			// If room is not lowest, check for UP door from below (previous i)
			if(i > 0 && rooms[i-1].hasDoorway(Direction.UP))
			{
				doorways[i].add(Direction.DOWN);
			}
			
			// Get a random room
			try {
				rooms[i] = dataStore.getLibraryRoomRandom(doorways[i]);
				
				// Make sure we actually got a result, and bail out if we didn't
				if(rooms[i] == null) { return null; }
				
				// Get the full path to the source tile
				fullPath = Dungeonator.TileFolderPath+rooms[i].getFilename()+".nbt";
				
				CompoundTag schematic = this.getSchematic(fullPath);
				
				// Verify the data was loaded
				if(schematic != null)
				{
					// Set blocks and block data
					rooms[i].setRawBlocks(((ByteArrayTag)schematic.getValue().get("blocks")).getValue());
					rooms[i].setRawBlockData(((ByteArrayTag)schematic.getValue().get("blockData")).getValue());
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
	
	public CompoundTag getSchematic(String fullPath)
	{
		// Check cache
		if(roomCache.containsKey(fullPath))
		{
			return roomCache.get(fullPath);
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
					roomCache.put(fullPath, schematic);
					return schematic;
				}
			} catch (IOException e) { e.printStackTrace(); }
		} catch (FileNotFoundException e) { e.printStackTrace(); }
		
		return null;
	}
	
	/**
	 * Get all doorways for a specific chunk
	 * @param world
	 * @param x
	 * @param z
	 * @return
	 */
	public Vector<Byte>[] getAdjacentDoorways(String world, int x, int z)
	{
		@SuppressWarnings("unchecked")
		Vector<Byte>[] doors = new Vector[16];
		for(int i = 0; i < 16; i++)
		{
			doors[i] = new Vector<Byte>();
		}
		
		try {
			// Get northern neighbors
			DungeonRoom[] neighborsN = dataStore.getChunkRooms(world, x-1, z);
			
			if(neighborsN != null)
			{
				for(int i = 0; i < 16; i++)
				{
					if(neighborsN[i] != null && neighborsN[i].isLoaded())
					{
						// Check southern doorways of northern neighbor
						if(neighborsN[i].hasDoorway(Direction.S)) { doors[neighborsN[i].getY()].add(Direction.S); }
						if(neighborsN[i].hasDoorway(Direction.SSE)) { doors[neighborsN[i].getY()].add(Direction.SSE); }
						if(neighborsN[i].hasDoorway(Direction.SSW)) { doors[neighborsN[i].getY()].add(Direction.SSW); }
					}
				}
			}
			
			DungeonRoom[] neighborsE = dataStore.getChunkRooms(world, x, z-1);
			
			if(neighborsE != null)
			{
				for(int i = 0; i < 16; i++)
				{
					if(neighborsE[i] != null && neighborsE[i].isLoaded())
					{
						// Check western doorways of eastern neighbor
						if(neighborsE[i].hasDoorway(Direction.W)) { doors[neighborsE[i].getY()].add(Direction.W); }
						if(neighborsE[i].hasDoorway(Direction.WNW)) { doors[neighborsE[i].getY()].add(Direction.WNW); }
						if(neighborsE[i].hasDoorway(Direction.WSW)) { doors[neighborsE[i].getY()].add(Direction.WSW); }
					}
				}
			}
			
			DungeonRoom[] neighborsS = dataStore.getChunkRooms(world, x+1, z);
			
			if(neighborsS != null)
			{
				for(int i = 0; i < 16; i++)
				{
					if(neighborsS[i] != null && neighborsS[i].isLoaded())
					{
						// Check northern doorways of southern neighbor
						if(neighborsS[i].hasDoorway(Direction.N)) { doors[neighborsS[i].getY()].add(Direction.N); }
						if(neighborsS[i].hasDoorway(Direction.NNE)) { doors[neighborsS[i].getY()].add(Direction.NNE); }
						if(neighborsS[i].hasDoorway(Direction.NNW)) { doors[neighborsS[i].getY()].add(Direction.NNW); }
					}
				}
			}
			
			DungeonRoom[] neighborsW = dataStore.getChunkRooms(world, x, z+1);
			
			if(neighborsW != null)
			{
				for(int i = 0; i < 16; i++)
				{
					if(neighborsW[i] != null && neighborsW[i].isLoaded())
					{
						// Check eastern doorways of western neighbor
						if(neighborsW[i].hasDoorway(Direction.E)) { doors[neighborsW[i].getY()].add(Direction.E); }
						if(neighborsW[i].hasDoorway(Direction.ESE)) { doors[neighborsW[i].getY()].add(Direction.ESE); }
						if(neighborsW[i].hasDoorway(Direction.ENE)) { doors[neighborsW[i].getY()].add(Direction.ENE); }
					}
				}
			}
		} catch (DataStoreGetException e) { e.printStackTrace(); }
		
		return doors;
	}
	
	public Vector<Byte> getAdjacentDoorways(String world, int x, int z, int y)
	{
		Vector<Byte> doors = new Vector<Byte>();
		
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
			if(y < 15)
			{
				DungeonRoom neighborU = dataStore.getRoom(world, x, y+1, z);
				
				if(neighborU.isLoaded())
				{
					// Check lower doorway of upper neighbor
					if(neighborU.hasDoorway(Direction.DOWN)) { doors.add(Direction.DOWN); }
				}
			}
			
			// Get lower neighbor
			if(y > 0)
			{
				DungeonRoom neighborD = dataStore.getRoom(world, x, y-1, z);
				
				if(neighborD.isLoaded())
				{
					// Check upper doorway of lower neighbor
					if(neighborD.hasDoorway(Direction.UP)) { doors.add(Direction.UP); }
				}
			}
		} catch (DataStoreGetException e) { e.printStackTrace(); }
		
		return doors;
	}
}
