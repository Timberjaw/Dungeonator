package com.aranai.dungeonator.datastore;

import java.util.Hashtable;
import java.util.Vector;

import org.bukkit.util.BlockVector;

import com.aranai.dungeonator.Dungeonator;
import com.aranai.dungeonator.dungeonchunk.DungeonChunk;
import com.aranai.dungeonator.dungeonchunk.DungeonRoom;
import com.aranai.dungeonator.dungeonchunk.DungeonRoomSet;
import com.aranai.dungeonator.dungeonchunk.DungeonWidget;

/**
 * Interface for data store classes. Specifies standard methods for accessing,
 * updating or deleting Dungeonator metadata.
 */
public interface IDungeonDataStore {
	
	/**
	 * Initialize the DataStore. Create necessary folders or tables and perform startup tasks.
	 */
	public void initialize(Dungeonator plugin);
	
	/**
	 * Shut down the DataStore
	 */
	public void shutdown();
	
	/**
	 * Gets a DungeonChunk from the data store.
	 *
	 * @param world the world
	 * @param x the x coordinate of the chunk
	 * @param z the z coordinate of the chunk
	 * @return the DungeonChunk if the hash is valid, or null otherwise
	 * @throws DataStoreGetException the data store get exception
	 */
	public DungeonChunk getChunk(String world, int x, int z) throws DataStoreGetException;
	
	/**
	 * Saves a DungeonChunk to the data store.
	 *
	 * @param chunk the DungeonChunk to save
	 * @return true, if successful
	 * @throws DataStoreSaveException if an error occurs while saving the chunk
	 */
	public boolean saveChunk(DungeonChunk chunk) throws DataStoreSaveException;
	
	/**
	 * Delete chunk.
	 *
	 * @param chunk the chunk
	 * @throws DataStoreDeleteException if an error occurs while deleting the chunk 
	 */
	public void deleteChunk(DungeonChunk chunk) throws DataStoreDeleteException;
	
	/**
	 * Gets all active rooms for a chunk.
	 *
	 * @param world the world name
	 * @param x the x coordinate of the chunk
	 * @param z the z coordinate of the chunk
	 * @return an array of the DungeonRooms, or null if the coordinates have not yet been populated
	 * @throws DataStoreGetException the data store get exception
	 */
	public DungeonRoom[] getChunkRooms(String world, int x, int z) throws DataStoreGetException;
	
	/**
	 * Gets an active room.
	 *
	 * @param world the world name
	 * @param x the x coordinate of the room
	 * @param y the y coordinate of the room
	 * @param z the z coordinate of the room
	 * @return the DungeonRoom, or null if the coordinates have not yet been populated
	 * @throws DataStoreGetException the data store get exception
	 */
	public DungeonRoom getRoom(String world, int x, int y, int z) throws DataStoreGetException;
	
	/**
	 * Save an active room.
	 *
	 * @param room the DungeonRoom
	 * @return true, if successful
	 * @throws DataStoreSaveException the data store save exception
	 */
	public boolean saveRoom(DungeonRoom room) throws DataStoreSaveException;
	
	/**
	 * Gets an active room set.
	 *
	 * @param world the world name
	 * @param x the x coordinate of the room set origin
	 * @param y the y coordinate of the room set origin
	 * @param z the z coordinate of the room set origin
	 * @return the DungeonRoomSet, or null if the coordinates have not yet been populated
	 * @throws DataStoreGetException the data store get exception
	 */
	public DungeonRoomSet getRoomSet(String world, int x, int y, int z) throws DataStoreGetException;
	
	/**
	 * Save a room set.
	 *
	 * @param set the DungeonRoomSet
	 * @return true, if successful
	 * @throws DataStoreSaveException the data store save exception
	 */
	public boolean saveRoomSet(DungeonRoomSet set) throws DataStoreSaveException;
	
	/**
	 * Save a room set to the library.
	 *
	 * @param set the DungeonRoomSet
	 * @return true, if successful
	 * @throws DataStoreSaveException the data store save exception
	 */
	public boolean saveLibraryRoomSet(DungeonRoomSet set) throws DataStoreSaveException;
	
	/**
	 * Delete room.
	 *
	 * @param world the world
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @throws DataStoreDeleteException the data store delete exception
	 */
	public void deleteRoom(String world, int x, int y, int z) throws DataStoreDeleteException;
	
	/**
	 * Gets a DungeonRoom record from the Library.
	 *
	 * @param the id of the room
	 * @return the DungeonRoom if the id is valid, or null otherwise
	 * @throws DataStoreGetException the data store get exception
	 */
	public DungeonRoom getLibraryRoom(long id) throws DataStoreGetException;
	
	/**
	 * Save a DungeonRoom record in the Library.
	 *
	 * @param room the room
	 * @return true, if successful
	 * @throws DataStoreSaveException the data store save exception
	 */
	public boolean saveLibraryRoom(DungeonRoom room) throws DataStoreSaveException;
	
	/**
	 * Delete a DungeonRoom record from the Library.
	 *
	 * @param hash the DungeonRoom's hash
	 * @throws DataStoreDeleteException the data store delete exception
	 */
	public void deleteLibraryRoom(String hash) throws DataStoreDeleteException;
	
	/**
	 * Gets a random room from the Library.
	 *
	 * @return the room
	 * @throws DataStoreGetException the data store get exception
	 */
	public DungeonRoom getLibraryRoomRandom(Vector<Byte> doorways) throws DataStoreGetException;
	
	/**
	 * Gets a random list of library room sets.
	 *
	 * @param number the number of room sets to retrieve
	 * @return the random sets
	 * @throws DataStoreGetException the data store get exception
	 */
	public Vector<DungeonRoomSet> getLibraryRoomSetsRandom(int number) throws DataStoreGetException;

	/**
	 * Saves multiple rooms simultaneously
	 * 
	 * @param the DungeonRooms to save
	 * @return true, if successful
	 * @throws DataStoreSaveException
	 */
	public boolean saveRooms(DungeonRoom[] rooms) throws DataStoreSaveException;
	
	/**
	 * Gets a DungeonWidget record from the library.
	 *
	 * @param id the id of the widget
	 * @return the DungeonWidget if the ID is valid, or null otherwise
	 * @throws DataStoreGetException the data store get exception
	 */
	public DungeonWidget getLibraryWidget(long id) throws DataStoreGetException;
	
	/**
	 * Gets a random library widget by size.
	 *
	 * @param size the size
	 * @return the random library widget
	 * @throws DataStoreGetException the data store get exception
	 */
	public DungeonWidget getRandomLibraryWidget(DungeonWidget.Size size) throws DataStoreGetException;
	
	/**
	 * Save a DungeonWidget record in the library.
	 *
	 * @param widget the widget
	 * @return true, if successful
	 * @throws DataStoreSaveException the data store save exception
	 */
	public boolean saveLibraryWidget(DungeonWidget widget) throws DataStoreSaveException;
	
	/**
	 * Gets a list of reserved rooms within a specified volume.
	 *
	 * @param world the world name
	 * @param x1 the starting x coord
	 * @param y1 the starting y coord
	 * @param z1 the starting z coord
	 * @param x2 the ending x coord
	 * @param y2 the ending y coord
	 * @param z2 the ending z coord
	 * @return a list of reserved rooms within the volume, or an empty list if no reserved rooms were found
	 * @throws DataStoreGetException the data store get exception
	 */
	public DungeonRoom[][][] getReservedRooms(String world, int x1, int y1, int z1, int x2, int y2, int z2) throws DataStoreGetException;
	
	/**
	 * Gets info on all the reserved rooms for a world.
	 *
	 * @param world the world
	 * @return the room info
	 * @throws DataStoreGetException the data store get exception
	 */
	public Hashtable<String,Long> getAllReservedRooms(String world) throws DataStoreGetException;
	
	/**
	 * Save a reserved room.
	 *
	 * @param world the world name
	 * @param x the x coord
	 * @param y the y coord
	 * @param z the z coord
	 * @param id the library id for the room
	 * @return true, if successful
	 * @throws DataStoreSetException the data store set exception
	 */
	public boolean saveReservedRoom(String world, int x, int y, int z, long id) throws DataStoreSaveException;
	
	/**
	 * Delete reserved room.
	 *
	 * @param world the world
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @return true, if successful
	 * @throws DataStoreDeleteException the data store delete exception
	 */
	public boolean deleteReservedRoom(String world, int x, int y, int z) throws DataStoreDeleteException;
}
