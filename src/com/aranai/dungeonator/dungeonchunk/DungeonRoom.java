package com.aranai.dungeonator.dungeonchunk;

import java.util.Vector;

import org.bukkit.Chunk;
import org.bukkit.craftbukkit.CraftChunk;

import com.aranai.dungeonator.Direction;
import com.aranai.dungeonator.generator.DungeonMath;

/**
 * Represents a 16Xx16Zx8Y cuboid region within an overall DungeonChunk. Each DungeonChunk contains 16 DungeonRooms stacked vertically.
 */
public class DungeonRoom implements IDungeonRoom {
	
	/** The X coordinate of the room. This coordinate should match the DungeonChunk X coordinate. */
	private int x;
	
	/** The Y coordinate of the room. This ranges from 1-16. */
	private int y;
	
	/** The Z coordinate of the room. This coordinate should match the DungeonChunk Z coordinate. */
	private int z;
	
	/** Random seed used for procedural rooms. */
	private long seed = 0;
	
	/** The DungeonChunk for this room */
	private DungeonChunk chunk;
	
	/** Room type */
	private DungeonRoomType type;
	
	/** Neighboring rooms (NESW, above, below) */
	private DungeonRoom[] neighbors = new DungeonRoom[6];
	
	/** Doorways */
	private DungeonRoomDoorway[] doorways = new DungeonRoomDoorway[12];
	
	public DungeonRoom(DungeonChunk chunk)
	{
		this.chunk = chunk;
		this.x = chunk.getX();
		this.z = chunk.getZ();
		this.y = 1;
	}
	
	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.dungeonchunk.IDungeonRoom#getDungeonChunk()
	 */
	@Override
	public DungeonChunk getDungeonChunk() {
		return chunk;
	}

	/* (non-Javadoc)
	 * @see com.aranai.Dungeonator.IDungeonChunk#getX()
	 */
	@Override
	public int getX() {
		return x;
	}

	/* (non-Javadoc)
	 * @see com.aranai.Dungeonator.IDungeonChunk#getZ()
	 */
	@Override
	public int getZ() {
		return z;
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
	public DungeonRoomType getType() {
		return type;
	}

	/* (non-Javadoc)
	 * @see com.aranai.Dungeonator.IDungeonChunk#setType(com.aranai.Dungeonator.DungeonChunkType)
	 */
	@Override
	public void setType(DungeonRoomType type) {
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
	public DungeonRoomDoorway getDoorway(byte direction) {
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
	public Vector<DungeonRoomDoorway> getDoorwaysOnSide(byte[] side)
	{
		Vector<DungeonRoomDoorway> sideDoorways = new Vector<DungeonRoomDoorway>(3);
		
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
	public void setDoorway(DungeonRoomDoorway doorway) {
		doorways[doorway.getDirection()] = doorway;
	}
	
	/* (non-Javadoc)
	 * @see com.aranai.Dungeonator.IDungeonChunk#hasNeighbor(byte)
	 */
	@Override
	public boolean hasNeighbor(byte direction) {
		if(this.isValidRoomDirection(direction))
		{
			return (neighbors[direction] != null);
		}
		
		return false;
	}

	/* (non-Javadoc)
	 * @see com.aranai.Dungeonator.IDungeonChunk#getNeighbor(byte)
	 */
	@Override
	public IDungeonRoom getNeighbor(byte direction) {
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
	public void setNeighbor(byte direction, IDungeonRoom neighbor) {
		if(this.isValidRoomDirection(direction))
		{
			neighbors[direction] = (DungeonRoom)neighbor;
		}
	}
	
	/**
	 * Checks if the specified direction is a valid chunk direction (NESW)
	 *
	 * @param direction the direction to check
	 * @return true, if the direction is a valid chunk direction
	 */
	public boolean isValidRoomDirection(byte direction)
	{
		return (direction == Direction.N || direction == Direction.S || direction == Direction.E || direction == Direction.W
				|| direction == Direction.UP || direction == Direction.DOWN);
	}
	
	/**
	 * Gets the raw blocks for the room.
	 *
	 * @return the block byte array
	 */
	public byte[] getRawBlocks()
	{
		byte[] blocks = new byte[16*16*8];
		int pos = 0;
		
		for(int x = 0; x < 16; x++)
		{
			for(int z = 0; z < 16; z++)
			{
				for(int y = 0; y < 8; y++)
				{
					pos = DungeonMath.getRoomPosFromCoords(x, y, z);
					blocks[pos] = (byte)chunk.getHandle().getBlock(x, (this.y*8)+y, z).getTypeId();
				}
			}
		}
		
		return blocks;
	}
}
