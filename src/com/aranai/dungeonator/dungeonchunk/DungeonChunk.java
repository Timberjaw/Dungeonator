package com.aranai.dungeonator.dungeonchunk;

import java.util.Vector;

import org.bukkit.Chunk;

import com.aranai.dungeonator.Direction;

/**
 * Stores and manipulates Dungeonator chunk information, including type, neighbors, and doorways.
 * 
 */
public class DungeonChunk implements IDungeonChunk {
	
	/** The world name. */
	private String world;
	
	/** X coordinate for the chunk. */
	private int x;
	
	/** Y coordinate for the chunk. */
	private int y;
	
	/** Z coordinate for the chunk. */
	private int z;
	
	/** The handle for the chunk data */
	private Chunk chunk;
	
	/** Random seed used for procedural chunks. */
	private long seed = 0;
	
	/** Chunk type */
	private DungeonChunkType type;
	
	/** Neighboring chunks (NESW) */
	private DungeonChunk[] neighbors = new DungeonChunk[4];
	
	/** Doorways */
	private DungeonChunkDoorway[] doorways = new DungeonChunkDoorway[12];
	
	/**
	 * Instantiates a new dungeon chunk.
	 */
	public DungeonChunk()
	{
		this(DungeonChunkType.BASIC_TILE);
	}
	
	/**
	 * Instantiates a new dungeon chunk with a specified DungeonChunkType.
	 *
	 * @param type the type
	 */
	public DungeonChunk(DungeonChunkType type)
	{
		this.type = type;
		this.world = "world";
		this.x = 0;
		this.z = 0;
	}
	
	/**
	 * Instantiates a new dungeon chunk from an existing chunk
	 *
	 * @param chunk the chunk
	 */
	public DungeonChunk(Chunk chunk)
	{
		this(DungeonChunkType.BASIC_TILE, chunk.getWorld().getName(), chunk.getX(), chunk.getZ(), chunk);
	}
	
	public DungeonChunk(DungeonChunkType type, String world, int x, int z, Chunk chunk)
	{
		this.type = type;
		this.world = world;
		this.x = x;
		this.z = z;
		this.chunk = chunk;
	}
	
	/* (non-Javadoc)
	 * @see com.aranai.Dungeonator.IDungeonChunk#getWorld()
	 */
	@Override
	public String getWorld() {
		return world;
	}

	/* (non-Javadoc)
	 * @see com.aranai.Dungeonator.IDungeonChunk#getX()
	 */
	@Override
	public int getX() {
		return x;
	}
	
	/* (non-Javadoc)
	 * @see com.aranai.Dungeonator.IDungeonChunk#setX()
	 */
	@Override
	public void setX(int x) {
		this.x = x;
	}
	
	/* (non-Javadoc)
	 * @see com.aranai.Dungeonator.IDungeonChunk#getY()
	 */
	@Override
	public int getY() {
		return y;
	}
	
	/* (non-Javadoc)
	 * @see com.aranai.Dungeonator.IDungeonChunk#setY()
	 */
	@Override
	public void setY(int y) {
		this.y = y;
	}

	/* (non-Javadoc)
	 * @see com.aranai.Dungeonator.IDungeonChunk#getZ()
	 */
	@Override
	public int getZ() {
		return z;
	}
	
	/* (non-Javadoc)
	 * @see com.aranai.Dungeonator.IDungeonChunk#setZ()
	 */
	@Override
	public void setZ(int z) {
		this.z = z;
	}
	
	/* (non-Javadoc)
	 * @see com.aranai.Dungeonator.IDungeonChunk#getSeed()
	 */
	@Override
	public long getSeed() {
		return seed;
	}

	/* (non-Javadoc)
	 * @see com.aranai.Dungeonator.IDungeonChunk#setSeed(long)
	 */
	@Override
	public void setSeed(long seed) {
		this.seed = seed;
	}
	
	/* (non-Javadoc)
	 * @see com.aranai.Dungeonator.IDungeonChunk#getType()
	 */
	@Override
	public DungeonChunkType getType() {
		return type;
	}

	/* (non-Javadoc)
	 * @see com.aranai.Dungeonator.IDungeonChunk#setType(com.aranai.Dungeonator.DungeonChunkType)
	 */
	@Override
	public void setType(DungeonChunkType type) {
		this.type = type;
		
	}

	/* (non-Javadoc)
	 * @see com.aranai.Dungeonator.IDungeonChunk#hasDoorway(com.aranai.Dungeonator.Direction)
	 */
	@Override
	public boolean hasDoorway(byte d) {
		return (d <= doorways.length && doorways[d] != null);
	}

	/* (non-Javadoc)
	 * @see com.aranai.Dungeonator.IDungeonChunk#getDoorway(byte)
	 */
	@Override
	public DungeonChunkDoorway getDoorway(byte direction) {
		if(this.hasDoorway(direction))
		{
			return doorways[direction];
		}
		
		return null;
	}
	
	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.IDungeonChunk#getDoorwaysOnSide(byte)
	 */
	@Override
	public Vector<DungeonChunkDoorway> getDoorwaysOnSide(byte[] side)
	{
		Vector<DungeonChunkDoorway> sideDoorways = new Vector<DungeonChunkDoorway>(3);
		
		// Loop through the directions available for this side
		for(byte i = 0; i < side.length; i++)
		{
			if(this.hasDoorway(side[i]))
			{
				// This DungeonChunk has a doorway at this side
				// Add the doorway to the list
				sideDoorways.add(this.getDoorway(side[i]));
			}
		}
		
		return sideDoorways;
	}
	
	/* (non-Javadoc)
	 * @see com.aranai.Dungeonator.IDungeonChunk#setDoorway(byte, com.aranai.Dungeonator.DungeonChunkDoorway)
	 */
	@Override
	public void setDoorway(DungeonChunkDoorway doorway) {
		doorways[doorway.getDirection()] = doorway;
	}
	
	/* (non-Javadoc)
	 * @see com.aranai.Dungeonator.IDungeonChunk#hasNeighbor(byte)
	 */
	@Override
	public boolean hasNeighbor(byte direction) {
		if(this.isValidChunkDirection(direction))
		{
			return (neighbors[direction] != null);
		}
		
		return false;
	}

	/* (non-Javadoc)
	 * @see com.aranai.Dungeonator.IDungeonChunk#getNeighbor(byte)
	 */
	@Override
	public IDungeonChunk getNeighbor(byte direction) {
		if(this.hasNeighbor(direction))
		{
			return neighbors[direction];
		}
		
		return null;
	}

	/* (non-Javadoc)
	 * @see com.aranai.Dungeonator.IDungeonChunk#setNeighbor(byte, com.aranai.Dungeonator.IDungeonChunk)
	 */
	@Override
	public void setNeighbor(byte direction, IDungeonChunk neighbor) {
		if(this.isValidChunkDirection(direction))
		{
			neighbors[direction] = (DungeonChunk)neighbor;
		}
	}
	
	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.IDungeonChunk#getHandle()
	 */
	@Override
	public Chunk getHandle()
	{
		return chunk;
	}
	
	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.IDungeonChunk#setHandle()
	 */
	@Override
	public void setHandle(Chunk handle)
	{
		chunk = handle;
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