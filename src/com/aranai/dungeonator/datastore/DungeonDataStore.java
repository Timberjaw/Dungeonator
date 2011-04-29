package com.aranai.dungeonator.datastore;

import com.aranai.dungeonator.DungeonChunk;

/**
 * Provides a default (non-functional) implementation for others to extend.
 */
public class DungeonDataStore implements IDungeonDataStore {

	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.IDungeonDataStore#getChunk(java.lang.String)
	 */
	@Override
	public DungeonChunk getChunk(String hash) throws DataStoreGetException {
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

}
