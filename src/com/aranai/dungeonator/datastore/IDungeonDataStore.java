package com.aranai.dungeonator.datastore;

import java.util.Vector;

import com.aranai.dungeonator.Dungeonator;
import com.aranai.dungeonator.dungeonchunk.DungeonChunk;
import com.aranai.dungeonator.dungeonchunk.DungeonRoom;

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
}
