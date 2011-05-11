package com.aranai.dungeonator.dungeonchunk;

import java.util.Vector;

import org.bukkit.Chunk;

/**
 * Specifies required methods for all dungeon chunk classes
 */
public interface IDungeonChunk {
	
	/**
	 * Gets the world name.
	 *
	 * @return the world name
	 */
	public String getWorld();
	
	/**
	 * Gets the x coordinate for the chunk.
	 *
	 * @return the x coordinate for the chunk
	 */
	public int getX();
	
	/**
	 * Gets the z coordinate for the chunk.
	 *
	 * @return the z coordinate for the chunk
	 */
	public int getZ();
	
	/**
	 * Gets the seed.
	 *
	 * @return the seed
	 */
	public long getSeed();
	
	/**
	 * Sets the seed.
	 *
	 * @param seed the new seed
	 */
	public void setSeed(long seed);
	
	/**
	 * Gets the DungeonChunkType for the chunk.
	 *
	 * @return the type
	 */
	public DungeonChunkType getType();
	
	/**
	 * Sets the DungeonChunkType for the chunk.
	 *
	 * @param type the new type
	 */
	public void setType(DungeonChunkType type);
	
	/**
	 * Checks for a doorway matching the specified direction.
	 *
	 * @param d the doorway direction to check
	 * @return true, if the chunk has a matching doorway
	 */
	public boolean hasDoorway(byte direction);
	
	/**
	 * Gets the doorway matching the specified direction.
	 *
	 * @param direction the direction of the doorway to get
	 * @return the doorway, or null if no doorway exists at the specified direction
	 */
	public DungeonChunkDoorway getDoorway(byte direction);
	
	/**
	 * Gets all of the doorways on the specified side.
	 *
	 * @param direction the direction of the side to get
	 * @return the doorways on the specified side
	 */
	public Vector<DungeonChunkDoorway> getDoorwaysOnSide(byte[] side);
	
	/**
	 * Sets a doorway. The direction will be inferred from the doorway instance itself.
	 *
	 * @param doorway the DungeonChunkDoorway to set
	 */
	public void setDoorway(DungeonChunkDoorway doorway);
	
	/**
	 * Checks for a neighboring chunk matching the specified direction.
	 *
	 * @param d the neighbor direction to check
	 * @return true, if the chunk has a matching neighbor
	 */
	public boolean hasNeighbor(byte direction);
	
	/**
	 * Gets the neighboring chunk matching the specified direction.
	 *
	 * @param direction the direction of the chunk to get
	 * @return the neighboring chunk, or null if no neighbor exists at the specified direction
	 */
	public IDungeonChunk getNeighbor(byte direction);
	
	/**
	 * Sets the neighboring chunk for the specified direction.
	 *
	 * @param direction the direction of the neighbor to set
	 * @param neighbor the chunk to set as a neighbor
	 */
	public void setNeighbor(byte direction, IDungeonChunk neighbor);
	
	/**
	 * Gets the handle for the DungeonChunk's chunk data.
	 *
	 * @return the chunk
	 */
	public Chunk getHandle();
	
	/**
	 * Sets the handle for the DungeonChunk's chunk data.
	 */
	public void setHandle(Chunk handle);
}
