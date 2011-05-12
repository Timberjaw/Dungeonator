package com.aranai.dungeonator.dungeonchunk;

import java.util.Vector;

import org.bukkit.Chunk;

/**
 * Specifies required methods for all dungeon chunk classes
 */
public interface IDungeonRoom {
	
	/**
	 * Gets the parent dungeon chunk for the room.
	 *
	 * @return the parent dungeon chunk
	 */
	public DungeonChunk getDungeonChunk();
	
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
	public DungeonRoomType getType();
	
	/**
	 * Sets the DungeonChunkType for the chunk.
	 *
	 * @param type the new type
	 */
	public void setType(DungeonRoomType type);
	
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
	public DungeonRoomDoorway getDoorway(byte direction);
	
	/**
	 * Gets all of the doorways on the specified side.
	 *
	 * @param direction the direction of the side to get
	 * @return the doorways on the specified side
	 */
	public Vector<DungeonRoomDoorway> getDoorwaysOnSide(byte[] side);
	
	/**
	 * Sets a doorway. The direction will be inferred from the doorway instance itself.
	 *
	 * @param doorway the DungeonChunkDoorway to set
	 */
	public void setDoorway(DungeonRoomDoorway doorway);
	
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
	public IDungeonRoom getNeighbor(byte direction);
	
	/**
	 * Sets the neighboring chunk for the specified direction.
	 *
	 * @param direction the direction of the neighbor to set
	 * @param neighbor the chunk to set as a neighbor
	 */
	public void setNeighbor(byte direction, IDungeonRoom neighbor);
}
