package com.aranai.dungeonator.datastore;

import java.util.Hashtable;
import java.util.Vector;

import org.bukkit.util.BlockVector;

import com.aranai.dungeonator.Dungeonator;
import com.aranai.dungeonator.dungeonchunk.DungeonChunk;
import com.aranai.dungeonator.dungeonchunk.DungeonRoom;
import com.aranai.dungeonator.dungeonchunk.DungeonRoomSet;
import com.aranai.dungeonator.dungeonchunk.DungeonWidget;
import com.aranai.dungeonator.dungeonchunk.DungeonWidget.Size;

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
	 * @see com.aranai.dungeonator.datastore.IDungeonDataStore#shutdown()
	 */
	@Override
	public void shutdown()
	{
		Dungeonator.getLogger().warning("Basic DataStore shutdown.");
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
	public DungeonRoom getLibraryRoom(long id) throws DataStoreGetException {
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

	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.datastore.IDungeonDataStore#getLibraryRoomRandom()
	 */
	@Override
	public DungeonRoom getLibraryRoomRandom(Vector<Byte> doorways) throws DataStoreGetException {
		/* Throw a DataStoreGetException. In practice this should never
		 * happen, because no one would be silly enough to attempt to use
		 * this class directly.
		 */
		
		throw new DataStoreGetException("Using DungeonDataStore class directly accomplishes nothing.", "getLibraryRoomRandom");
	}

	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.datastore.IDungeonDataStore#getChunkRooms(java.lang.String, int, int)
	 */
	@Override
	public DungeonRoom[] getChunkRooms(String world, int x, int z) throws DataStoreGetException {
		throw new DataStoreGetException("Using DungeonDataStore class directly accomplishes nothing.", "getChunkRooms");
	}

	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.datastore.IDungeonDataStore#saveRooms(com.aranai.dungeonator.dungeonchunk.DungeonRoom[])
	 */
	@Override
	public boolean saveRooms(DungeonRoom[] rooms) throws DataStoreSaveException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.datastore.IDungeonDataStore#saveRoomSet(com.aranai.dungeonator.dungeonchunk.DungeonRoomSet)
	 */
	@Override
	public boolean saveRoomSet(DungeonRoomSet set)
			throws DataStoreSaveException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public DungeonWidget getLibraryWidget(long id) throws DataStoreGetException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean saveLibraryWidget(DungeonWidget widget)
			throws DataStoreSaveException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public DungeonRoomSet getRoomSet(String world, int x, int y, int z)
			throws DataStoreGetException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean saveLibraryRoomSet(DungeonRoomSet set)
			throws DataStoreSaveException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public DungeonRoom[][][] getReservedRooms(String world, int x1, int y1,
			int z1, int x2, int y2, int z2) throws DataStoreGetException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean saveReservedRoom(String world, int x, int y, int z, long id)
			throws DataStoreSaveException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Hashtable<String, Long> getAllReservedRooms(String world)
			throws DataStoreGetException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean deleteReservedRoom(String world, int x, int y, int z)
			throws DataStoreDeleteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Vector<DungeonRoomSet> getLibraryRoomSetsRandom(int number)
			throws DataStoreGetException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DungeonWidget getRandomLibraryWidget(Size size)
			throws DataStoreGetException {
		// TODO Auto-generated method stub
		return null;
	}

}
