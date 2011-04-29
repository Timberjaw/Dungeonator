package com.aranai.dungeonator.datastore;

import com.aranai.dungeonator.DungeonChunk;

/**
 * Interface for data store classes. Specifies standard methods for accessing,
 * updating or deleting Dungeonator metadata.
 */
public interface IDungeonDataStore {
	
	/**
	 * Gets a DungeonChunk from the data store.
	 *
	 * @param hash the hash for the chunk
	 * @return the DungeonChunk if the hash is valid, or null otherwise
	 */
	public DungeonChunk getChunk(String hash) throws DataStoreGetException;
	
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
}
