package com.aranai.dungeonator.datastore;

import com.aranai.dungeonator.Dungeonator;
import com.aranai.dungeonator.dungeonchunk.DungeonChunk;
import com.aranai.dungeonator.dungeonchunk.DungeonRoom;

/**
 * Provides a default (non-functional) implementation for others to extend.
 */
public class DungeonDataStore implements IDungeonDataStore {

	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.datastore.IDungeonDataStore#initialize()
	 */
	@Override
	public void initialize(Dungeonator plugin)
	{
		Dungeonator.getLogger().warning("Basic DataStore initialized. Data storage is NOT functional.");
	}
	
	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.IDungeonDataStore#getChunk(java.lang.String)
	 */
	@Override
	public DungeonChunk getChunk(String world, int x, int z) throws DataStoreGetException {
		/* Throw a DataStoreGetException. In practice this should never
		 * happen, because no one would be silly enough to attempt to use
		 * this class directly.
		 */
		
		throw new DataStoreGetException("Using DungeonDataStore class directly accomplishes nothing.", "getChunk");
	}

	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.IDungeonDataStore#saveChunk(com.aranai.dungeonator.DungeonChunk)
	 */
	@Override
	public boolean saveChunk(DungeonChunk chunk) throws DataStoreSaveException {
		/* Throw a DataStoreSaveException. In practice this should never
		 * happen, because no one would be silly enough to attempt to use
		 * this class directly.
		 */
		
		throw new DataStoreSaveException("Using DungeonDataStore class directly accomplishes nothing.", "saveChunk");
	}

	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.IDungeonDataStore#deleteChunk(com.aranai.dungeonator.DungeonChunk)
	 */
	@Override
	public void deleteChunk(DungeonChunk chunk) throws DataStoreDeleteException {
		/* Throw a DataStoreDeleteException. In practice this should never
		 * happen, because no one would be silly enough to attempt to use
		 * this class directly.
		 */
		
		throw new DataStoreDeleteException("Using DungeonDataStore class directly accomplishes nothing.", "deleteChunk");
	}

	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.datastore.IDungeonDataStore#getRoom(java.lang.String, int, int, int)
	 */
	@Override
	public DungeonRoom getRoom(String world, int x, int y, int z) throws DataStoreGetException {
		/* Throw a DataStoreGetException. In practice this should never
		 * happen, because no one would be silly enough to attempt to use
		 * this class directly.
		 */
		
		throw new DataStoreGetException("Using DungeonDataStore class directly accomplishes nothing.", "getRoom");
	}

	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.datastore.IDungeonDataStore#saveRoom(com.aranai.dungeonator.dungeonchunk.DungeonRoom)
	 */
	@Override
	public boolean saveRoom(DungeonRoom room) throws DataStoreSaveException {
		/* Throw a DataStoreSaveException. In practice this should never
		 * happen, because no one would be silly enough to attempt to use
		 * this class directly.
		 */
		
		throw new DataStoreSaveException("Using DungeonDataStore class directly accomplishes nothing.", "saveRoom");
	}

	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.datastore.IDungeonDataStore#deleteRoom(java.lang.String, int, int, int)
	 */
	@Override
	public void deleteRoom(String world, int x, int y, int z) throws DataStoreDeleteException {
		/* Throw a DataStoreDeleteException. In practice this should never
		 * happen, because no one would be silly enough to attempt to use
		 * this class directly.
		 */
		
		throw new DataStoreDeleteException("Using DungeonDataStore class directly accomplishes nothing.", "deleteRoom");
	}

	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.datastore.IDungeonDataStore#getLibraryRoom(java.lang.String)
	 */
	@Override
	public DungeonRoom getLibraryRoom(String hash) throws DataStoreGetException {
		/* Throw a DataStoreGetException. In practice this should never
		 * happen, because no one would be silly enough to attempt to use
		 * this class directly.
		 */
		
		throw new DataStoreGetException("Using DungeonDataStore class directly accomplishes nothing.", "getLibraryRoom");
	}

	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.datastore.IDungeonDataStore#saveLibraryRoom(com.aranai.dungeonator.dungeonchunk.DungeonRoom)
	 */
	@Override
	public boolean saveLibraryRoom(DungeonRoom room) throws DataStoreSaveException {
		/* Throw a DataStoreSaveException. In practice this should never
		 * happen, because no one would be silly enough to attempt to use
		 * this class directly.
		 */
		
		throw new DataStoreSaveException("Using DungeonDataStore class directly accomplishes nothing.", "saveLibraryRoom");
	}

	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.datastore.IDungeonDataStore#deleteLibraryRoom(java.lang.String)
	 */
	@Override
	public void deleteLibraryRoom(String hash) throws DataStoreDeleteException {
		/* Throw a DataStoreDeleteException. In practice this should never
		 * happen, because no one would be silly enough to attempt to use
		 * this class directly.
		 */
		
		throw new DataStoreDeleteException("Using DungeonDataStore class directly accomplishes nothing.", "deleteLibraryRoom");
	}

}
