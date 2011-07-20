package com.aranai.dungeonator.dungeonchunk;

import java.util.Vector;

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
	 * Gets the DungeonRoomType for the room.
	 *
	 * @return the type
	 */
	public DungeonRoomType getType();
	
	/**
	 * Sets the DungeonRoomType for the room.
	 *
	 * @param type the new type
	 */
	public void setType(DungeonRoomType type);
	
	/**
	 * Sets the library id.
	 *
	 * @param id the new library id
	 */
	public void setLibraryId(long id);
	
	/**
	 * Gets the library id.
	 *
	 * @return the library id
	 */
	public long getLibraryId();
	
	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name);
	
	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName();
	
	/**
	 * Sets the filename.
	 *
	 * @param name the new filename
	 */
	public void setFilename(String name);
	
	/**
	 * Gets the filename.
	 *
	 * @return the filename
	 */
	public String getFilename();
	
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
	 * Gets all of the doorways for the room.
	 *
	 * @return the doorways
	 */
	public Vector<DungeonRoomDoorway> getDoorways();
	
	/**
	 * Gets all of the doorways for the room in raw byte format.
	 *
	 * @return the doorways
	 */
	public byte[] getDoorwaysRaw();
	
	/**
	 * Sets a doorway. The direction will be inferred from the doorway instance itself.
	 *
	 * @param doorway the DungeonChunkDoorway to set
	 */
	public void setDoorway(DungeonRoomDoorway doorway);
	
	/**
	 * Sets/unsets a doorway with a specific direction.
	 *
	 * @param direction the direction
	 * @param status the status (true for on, false for off)
	 */
	public void setDoorway(byte direction, boolean status);
	
	/**
	 * Reset (remove) all doorways.
	 */
	public void resetDoorways();
	
	/**
	 * Checks for a neighboring chunk matching the specified direction.
	 *
	 * @param d the neighbor direction to check
	 * @return true, if the chunk has a matching neighbor
	 */
	public boolean hasNeighbor(byte direction);
	
	/**
	 * Gets the neighboring room matching the specified direction.
	 *
	 * @param direction the direction of the room to get
	 * @return the neighboring room, or null if no neighbor exists at the specified direction
	 */
	public IDungeonRoom getNeighbor(byte direction);
	
	/**
	 * Sets the neighboring room for the specified direction.
	 *
	 * @param direction the direction of the neighbor to set
	 * @param neighbor the room to set as a neighbor
	 */
	public void setNeighbor(byte direction, IDungeonRoom neighbor);
}
