package com.aranai.dungeonator;

/**
 * DungeonChunkDoorway represents a single doorway for a chunk.
 * Provides access to meta data (doorway type, direction, and so forth). 
 */
public class DungeonChunkDoorway {
	
	/** The direction. */
	private byte direction;
	
	/**
	 * Instantiates a new dungeon chunk doorway.
	 *
	 * @param d the direction attachment for the doorway
	 */
	public DungeonChunkDoorway(byte d)
	{
		this.direction = d;
	}
	
	/**
	 * Gets the direction of the doorway.
	 *
	 * @return the direction
	 */
	public byte getDirection()
	{
		return this.direction;
	}
}
