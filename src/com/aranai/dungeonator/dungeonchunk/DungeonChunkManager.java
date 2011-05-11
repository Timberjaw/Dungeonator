package com.aranai.dungeonator.dungeonchunk;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Chunk;

import com.aranai.dungeonator.Direction;
import com.aranai.dungeonator.DungeonDataManager;
import com.aranai.dungeonator.generator.DungeonChunkGenerator;

/**
 * Handles storage and access for DungeonChunks via a local cache.
 * The DungeonChunkManager interfaces with the DungeonDataManager
 * for data store abstraction, and the DungeonChunkGenerator for
 * new chunk generation.
 */
public class DungeonChunkManager {
	
	/** Local cache of dungeon chunk information. */
	private ConcurrentHashMap<String,DungeonChunk> chunkCache;
	
	/** DungeonDataManager instance */
	private DungeonDataManager dataManager;
	
	/** DungeonChunkGenerator instance */
	private DungeonChunkGenerator chunkGenerator;
	
	/**
	 * Instantiates the dungeon chunk manager.
	 */
	public DungeonChunkManager(DungeonDataManager dataManager)
	{
		// Initialize the chunk cache
		chunkCache = new ConcurrentHashMap<String,DungeonChunk>();
		
		// Set the data manager
		this.dataManager = dataManager;
	}
	
	/**
	 * Stores and caches the Dungeonator-specific chunk data.
	 *
	 * @param chunk the DungeonChunk to store
	 * @see DungeonChunk
	 */
	public void storeChunk(DungeonChunk chunk)
	{
		// Save / update chunk information to data store
		/** TODO: Save/update chunk information to data store */
		
		// Update chunk cache
		this.updateCachedChunk(chunk);
	}
	
	/**
	 * Save a {@link DungeonChunk} to the local cache.
	 *
	 * @param chunk the DungeonChunk to save
	 */
	public void updateCachedChunk(DungeonChunk chunk)
	{
		// Save the chunk to the local chunk cache
		String hash = this.getChunkHash(chunk.getWorld(), chunk.getX(), chunk.getZ());
		chunkCache.put(hash, chunk);
	}
	
	/**
	 * Load a {@link DungeonChunk} from the local cache or the data store.
	 * Convenience alias for {@link #loadChunk(String)}
	 *
	 * @param world the world in which the chunk is located
	 * @param x the x coordinate for the chunk
	 * @param z the z coordinate for the chunk
	 * @param chunk the chunk data
	 */
	public void loadChunk(String world, int x, int z, Chunk chunkData)
	{
		this.loadChunk(this.getChunkHash(world, x, z), chunkData);
	}
	
	/**
	 * Load a {@link DungeonChunk} from the local cache or the data store.
	 *
	 * @param hash the hash for the DungeonChunk
	 */
	public void loadChunk(String hash, Chunk chunkData)
	{
		// Check chunk cache
		if(!this.isChunkCached(hash))
		{
			// Get chunk from data store
			DungeonChunk chunk = dataManager.getChunk(hash);
			
			/** TODO: Attach chunk data */
			
			// Cache chunk
			this.updateCachedChunk(chunk);
		}
	}
	
	/**
	 * Gets a {@link DungeonChunk} matching the specified coordinates.
	 * Convenience alias for {@link #getChunk(String)}
	 *
	 * @param world the world for the chunk
	 * @param x the x coordinate for the chunk
	 * @param z the z coordinate for the chunk
	 * @return the chunk
	 */
	public DungeonChunk getChunk(String world, int x, int z)
	{
		return this.getChunk(this.getChunkHash(world, x, z));
	}
	
	/**
	 * Gets the {@link DungeonChunk} for the specified coordinates
	 *
	 * @param hash the hash for the chunk
	 * @return the DungeonChunk
	 * @see DungeonChunkManager#getChunkHash(String, int, int)
	 */
	public DungeonChunk getChunk(String hash)
	{
		// Load chunk
		this.loadChunk(hash, null);
		
		return this.getCachedChunk(hash);
	}
	
	/**
	 * Gets a cached {@link DungeonChunk} based on world, x and z coordinates
	 * Convenience alias for {@link #getCachedChunk(String)}.
	 *
	 * @param world the world
	 * @param x the x coordinate
	 * @param z the z coordinate
	 * @return the cached chunk
	 */
	@SuppressWarnings("unused")
	private DungeonChunk getCachedChunk(String world, int x, int z)
	{
		return this.getCachedChunk(this.getChunkHash(world, x, z));
	}
	
	/**
	 * Gets a cached {@link DungeonChunk} based on its hash code
	 *
	 * @param hash the hash for the DungeonChunk
	 * @return the cached DungeonChunk
	 * @see #getChunkHash(String, int, int)
	 */
	private DungeonChunk getCachedChunk(String hash)
	{
		if(this.isChunkCached(hash))
		{
			return chunkCache.get(hash);
		}
		
		return null;
	}
	
	/**
	 * Gets a chunk hash based on world name, x and z coordinates.
	 *
	 * @param world the world name
	 * @param x the x coordinate
	 * @param z the z coordinate
	 * @return the chunk hash
	 */
	public String getChunkHash(String world, int x, int z)
	{
		return (world+"."+x+"."+z);
	}
	
	/**
	 * Checks if the chunk is cached.
	 * Convenience alias for {@link #isChunkCached(String)}
	 *
	 * @param world the world name
	 * @param x the x coordinate for the chunk
	 * @param z the z coordinate for the chunk
	 * @return true, if the chunk is cached
	 */
	public boolean isChunkCached(String world, int x, int z)
	{
		return this.isChunkCached(this.getChunkHash(world, x, z));
	}
	
	/**
	 * Checks if the chunk is cached.
	 *
	 * @param hash the hash for the chunk
	 * @return true, if the chunk is cached
	 */
	public boolean isChunkCached(String hash)
	{
		return (chunkCache.containsKey(hash));
	}

	/**
	 * Checks if a DungeonChunk has been generated for the specified Chunk.
	 *
	 * @param world the world
	 * @param x the x coordinate for the chunk
	 * @param z the z coordinate for the chunk
	 * @return true, if a DungeonChunk has already been generated
	 */
	public boolean isChunkGenerated(String world, int x, int z) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Generate a DungeonChunk for the specified Chunk.
	 *
	 * @param world the world name
	 * @param x the x coordinate for the chunk
	 * @param z the z coordinate for the chunk
	 * @param chunk the handle for the chunk
	 */
	public void generateChunk(String world, int x, int z, Chunk chunk) {
		// Build an empty DungeonChunk
		DungeonChunk newChunk = new DungeonChunk(DungeonChunkType.BASIC_TILE, world, x, z, chunk);
		
		// Check for neighbors
		DungeonChunk neighborN = null;
		DungeonChunk neighborE = null;
		DungeonChunk neighborS = null;
		DungeonChunk neighborW = null;
		
		if(this.isChunkGenerated(world, x+16, z)) { neighborN = this.getChunk(world, x+16, z); }
		if(this.isChunkGenerated(world, x, z+16)) { neighborE = this.getChunk(world, x, z+16); }
		if(this.isChunkGenerated(world, x-16, z)) { neighborS = this.getChunk(world, x-16, z); }
		if(this.isChunkGenerated(world, x, z-16)) { neighborW = this.getChunk(world, x, z-16); }
		
		// Add neighbors
		newChunk.setNeighbor(Direction.N, neighborN);
		newChunk.setNeighbor(Direction.E, neighborE);
		newChunk.setNeighbor(Direction.S, neighborS);
		newChunk.setNeighbor(Direction.W, neighborW);
		
		// Generate chunk data
		chunkGenerator.generateChunk(newChunk);
		
		// TODO: Save chunk data
	}
}
