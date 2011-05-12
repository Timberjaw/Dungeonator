package com.aranai.dungeonator.dungeonchunk;

import org.bukkit.Chunk;
import org.bukkit.World;

import com.aranai.dungeonator.Direction;

/**
 * Stores and manipulates Dungeonator chunk information.
 */
public class DungeonChunk {
	
	/** The world. */
	private World world;
	
	/** X coordinate for the chunk. */
	private int x;
	
	/** Z coordinate for the chunk. */
	private int z;
	
	/** The handle for the chunk data */
	private Chunk chunk;

	/** The neighboring chunks */
	private DungeonChunk[] neighbors = new DungeonChunk[4];
	
	/**
	 * Instantiates a new dungeon chunk from an existing chunk
	 *
	 * @param chunk the chunk
	 */
	public DungeonChunk(Chunk chunk)
	{
		this(chunk, DungeonRoomType.BASIC_TILE, chunk.getX(), chunk.getZ());
	}
	
	public DungeonChunk(Chunk chunk, DungeonRoomType type, int x, int z)
	{
		this.world = chunk.getWorld();
		this.x = x;
		this.z = z;
		this.chunk = chunk;
	}
	
	/* 
	 * Gets the native chunk handle for the DungeonChunk
	 */
	public Chunk getHandle()
	{
		return chunk;
	}
	
	/**
	 * Gets the chunk's parent world
	 *
	 * @return the parent world
	 */
	public World getWorld()
	{
		return world;
	}
	
	/**
	 * Gets the name of the chunk's parent world.
	 *
	 * @return the world name
	 */
	public String getWorldName()
	{
		return world.getName();
	}
	
	/**
	 * Gets the x coordinate for the chunk.
	 *
	 * @return the x coordinate
	 */
	public int getX()
	{
		return x;
	}
	
	/**
	 * Gets the z coordinate for the chunk.
	 *
	 * @return the z coordinate
	 */
	public int getZ()
	{
		return z;
	}
	
	/* 
	 * Sets the native chunk handle for the DungeonChunk
	 */
	public void setHandle(Chunk handle)
	{
		chunk = handle;
	}
	
	/**
	 * Checks for an existing neighbor in the specified direction.
	 *
	 * @param direction the direction
	 * @return true, if successful
	 */
	public boolean hasNeighbor(byte direction) {
		if(this.isValidChunkDirection(direction))
		{
			return (neighbors[direction] != null);
		}
		
		return false;
	}
	
	/* 
	 * Gets the neighboring chunk in the specified direction
	 */
	public DungeonChunk getNeighbor(byte direction) {
		if(this.hasNeighbor(direction))
		{
			return neighbors[direction];
		}
		
		return null;
	}

	/* 
	 * Set a neighboring chunk in the specified direction
	 */
	public void setNeighbor(byte direction, DungeonChunk neighbor) {
		if(this.isValidChunkDirection(direction))
		{
			neighbors[direction] = (DungeonChunk)neighbor;
		}
	}
	
	/**
	 * Checks if the specified direction is a valid chunk direction (NESW)
	 *
	 * @param direction the direction to check
	 * @return true, if the direction is a valid chunk direction
	 */
	public boolean isValidChunkDirection(byte direction)
	{
		return (direction == Direction.N || direction == Direction.S || direction == Direction.E || direction == Direction.W);
	}
}