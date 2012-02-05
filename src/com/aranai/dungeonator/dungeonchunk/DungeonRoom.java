package com.aranai.dungeonator.dungeonchunk;

import java.util.Vector;

import org.bukkit.block.BlockState;
import org.bukkit.util.BlockVector;
import org.jnbt.CompoundTag;
import org.jnbt.IntTag;
import org.jnbt.StringTag;

import com.aranai.dungeonator.Direction;
import com.aranai.dungeonator.datastore.DataStoreAssetException;
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
	
	/** Loaded status */
	private boolean loaded = false;
	
	/** Random seed used for procedural rooms. */
	private long seed = 0;
	
	/** The room name. */
	private String name = "";
	
	/** The room filename. */
	private String filename = "";
	
	/** The library id. */
	private long libraryID = 0;
	
	/** The room set id. */
	private long libraryRoomSetID = 0;
	
	/** The room set path. Redundantly included here for performance reasons during generation. */
	private String libraryRoomSetPath = "";
	
	/** The room set */
	private DungeonRoomSet roomSet;
	
	/** Temporary raw schematic */
	private CompoundTag schematic;
	
	/** Temporary raw block array */
	private byte[] tempRawBlocks;
	
	/** Temporary raw block data array */
	private byte[] tempRawBlockData;
	
	/** The DungeonChunk for this room */
	private DungeonChunk chunk;
	
	/** Room type */
	private DungeonRoomType type;
	
	/** Neighboring rooms (NESW, above, below) */
	private DungeonRoom[] neighbors = new DungeonRoom[6];
	
	/** Doorways */
	private DungeonRoomDoorway[] doorways = new DungeonRoomDoorway[14];
	
	/** Allowed themes */
	private Vector<String> allowedThemes;
	
	/** Default theme */
	private String defaultTheme;
	
	/** Widget Nodes */
	private Vector<DungeonWidgetNode> nodes;
	
	public DungeonRoom()
	{
		chunk = null;
		setLocation(0, 0, 0);
		allowedThemes = new Vector<String>();
		defaultTheme = "DEFAULT";
		allowedThemes.add(defaultTheme);
		schematic = null;
		nodes = new Vector<DungeonWidgetNode>();
	}
	
	public DungeonRoom(DungeonChunk chunk, int y)
	{
		this();
		this.chunk = chunk;
		setLocation(chunk.getX(), y, chunk.getZ());
	}
	
	public void setLocation(int x, int y, int z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.dungeonchunk.IDungeonRoom#getDungeonChunk()
	 */
	@Override
	public DungeonChunk getDungeonChunk() {
		return chunk;
	}
	
	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.dungeonchunk.IDungeonRoom#setDungeonChunk()
	 */
	@Override
	public void setDungeonChunk(DungeonChunk chunk) {
		this.chunk = chunk;
	}
	
	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.dungeonchunk.IDungeonRoom#isLoaded()
	 */
	public boolean isLoaded()
	{
		return loaded;
	}
	
	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.dungeonchunk.IDungeonRoom#setLoaded(boolean)
	 */
	public void setLoaded(boolean newLoaded)
	{
		loaded = newLoaded;
	}

	/* (non-Javadoc)
	 * @see com.aranai.Dungeonator.IDungeonRoom#getX()
	 */
	@Override
	public int getX() {
		return x;
	}
	
	/* (non-Javadoc)
	 * @see com.aranai.Dungeonator.IDungeonRoom#getY()
	 */
	@Override
	public int getY() {
		return y;
	}

	/* (non-Javadoc)
	 * @see com.aranai.Dungeonator.IDungeonRoom#getZ()
	 */
	@Override
	public int getZ() {
		return z;
	}
	
	/* (non-Javadoc)
	 * @see com.aranai.Dungeonator.IDungeonRoom#getSeed()
	 */
	@Override
	public long getSeed() {
		return seed;
	}

	/* (non-Javadoc)
	 * @see com.aranai.Dungeonator.IDungeonRoom#setSeed(long)
	 */
	@Override
	public void setSeed(long seed) {
		this.seed = seed;
	}
	
	/* (non-Javadoc)
	 * @see com.aranai.Dungeonator.IDungeonRoom#getType()
	 */
	@Override
	public DungeonRoomType getType() {
		return type;
	}

	/* (non-Javadoc)
	 * @see com.aranai.Dungeonator.IDungeonRoom#setType(com.aranai.Dungeonator.DungeonChunkType)
	 */
	@Override
	public void setType(DungeonRoomType type) {
		this.type = type;
		
	}

	/* (non-Javadoc)
	 * @see com.aranai.Dungeonator.IDungeonRoom#hasDoorway(com.aranai.Dungeonator.Direction)
	 */
	@Override
	public boolean hasDoorway(byte d) {
		return (d <= doorways.length && doorways[d] != null);
	}

	/* (non-Javadoc)
	 * @see com.aranai.Dungeonator.IDungeonRoom#getDoorway(byte)
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
	 * @see com.aranai.dungeonator.IDungeonRoom#getDoorwaysOnSide(byte)
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
	 * @see com.aranai.dungeonator.dungeonchunk.IDungeonRoom#getDoorways()
	 */
	@Override
	public Vector<DungeonRoomDoorway> getDoorways() {
		Vector<DungeonRoomDoorway> doorways = new Vector<DungeonRoomDoorway>();
		
		// Loop through the directions available for this side
		for(byte i = 0; i < this.doorways.length; i++)
		{
			if(this.hasDoorway(i))
			{
				// This DungeonChunk has a doorway at this side
				// Add the doorway to the list
				doorways.add(this.getDoorway(i));
			}
		}
		
		return doorways;
	}
	
	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.dungeonchunk.IDungeonRoom#getDoorwaysRaw()
	 */
	@Override
	public byte[] getDoorwaysRaw()
	{
		byte[] doorways = new byte[this.doorways.length];
		
		// Loop through the directions available for this side
		for(byte i = 0; i < this.doorways.length; i++)
		{
			if(this.hasDoorway(i))
			{
				// This DungeonChunk has a doorway at this side
				// Add the doorway to the list
				doorways[i] = 1;
			}
		}
		
		return doorways;
	}
	
	/* (non-Javadoc)
	 * @see com.aranai.Dungeonator.IDungeonRoom#setDoorway(byte, com.aranai.Dungeonator.DungeonChunkDoorway)
	 */
	@Override
	public void setDoorway(DungeonRoomDoorway doorway) {
		doorways[doorway.getDirection()] = doorway;
	}
	
	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.dungeonchunk.IDungeonRoom#setDoorway(byte, boolean)
	 */
	@Override
	public void setDoorway(byte direction, boolean status) {
		if(Direction.isValidDirection(direction))
		{
			if(status)
			{
				// Set the doorway
				doorways[direction] = new DungeonRoomDoorway(direction);
			}
			else
			{
				// Unset the doorway
				doorways[direction] = null;
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.dungeonchunk.IDungeonRoom#resetDoorways()
	 */
	@Override
	public void resetDoorways() {
		for(int i = 0; i < doorways.length; i++)
		{
			if(doorways[i] != null)
			{
				doorways[i] = null;
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see com.aranai.Dungeonator.IDungeonRoom#hasNeighbor(byte)
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
	 * @see com.aranai.Dungeonator.IDungeonRoom#getNeighbor(byte)
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
	 * @see com.aranai.Dungeonator.IDungeonRoom#setNeighbor(byte, com.aranai.Dungeonator.IDungeonRoom)
	 */
	@Override
	public void setNeighbor(byte direction, IDungeonRoom neighbor) {
		if(this.isValidRoomDirection(direction))
		{
			neighbors[direction] = (DungeonRoom)neighbor;
		}
	}
	
	/**
	 * Checks if the specified direction is a valid chunk direction (NESW,Up,Down)
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
		int tick = 0;
		if(chunk != null && chunk.isReady())
		{
			/*
			 * If the chunk is in ready state, retrieve the real-world blocks
			 */
			
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
						if(blocks[pos] > 0) { tick++; }
					}
				}
			}
			
			System.out.println("Found "+tick+" blocks for room at Y "+(this.y*8)+". "+this.toString());
			
			return blocks;
		}
		else if(tempRawBlocks != null)
		{
			/*
			 * If the chunk is NOT ready, retrieve the local blocks
			 */
			
			return tempRawBlocks;
		}
		
		return null;
	}
	
	/**
	 * Sets the raw blocks for the room.
	 *
	 * @param blocks the raw blocks
	 */
	public void setRawBlocks(byte[] blocks)
	{
		this.tempRawBlocks = blocks;
	}
	
	/**
	 * Gets the raw block data for the room.
	 *
	 * @return the raw block data byte array
	 */
	public byte[] getRawBlockData()
	{
		if(chunk != null && chunk.isReady())
		{
			/*
			 * If the chunk is in ready state, retrieve the real-world block data
			 */

			byte[] data = new byte[16*16*8];
			int pos = 0;
			
			for(int x = 0; x < 16; x++)
			{
				for(int z = 0; z < 16; z++)
				{
					for(int y = 0; y < 8; y++)
					{
						pos = DungeonMath.getRoomPosFromCoords(x, y, z);
						data[pos] = chunk.getHandle().getBlock(x, (this.y*8)+y, z).getData();
					}
				}
			}
			
			return data;
		}
		else if(tempRawBlockData != null)
		{
			/*
			 * If the chunk is NOT ready, retrieve the local block data
			 */
			
			return tempRawBlockData;
		}
		
		return null;
	}
	
	/**
	 * Sets the raw block data for the room.
	 *
	 * @param blocks the raw block data
	 */
	public void setRawBlockData(byte[] blockData)
	{
		this.tempRawBlockData = blockData;
	}
	
	/**
	 * Gets the schematic.
	 *
	 * @return the schematic
	 */
	public CompoundTag getSchematic()
	{
		return schematic;
	}
	
	/**
	 * Sets the schematic.
	 *
	 * @param the new schematic
	 */
	public void setSchematic(CompoundTag s)
	{
		schematic = s;
	}
	
	/**
	 * Gets the tile entities for the room
	 * 
	 * @return BlockState[]
	 */
	public BlockState[] getTileEntities()
	{
		BlockState[] chunkTileEntities = this.chunk.getHandle().getTileEntities();
		Vector<BlockState> roomTileEntities = new Vector<BlockState>();
		
		for(BlockState b : chunkTileEntities)
		{
			if(b.getY() >= (this.y*8) && b.getY() < (this.y*8)+15)
			{
				roomTileEntities.add(b);
			}
		}
		
		return roomTileEntities.toArray(chunkTileEntities);
	}

	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.dungeonchunk.IDungeonRoom#setLibraryId(long)
	 */
	@Override
	public void setLibraryId(long id) {
		this.libraryID = id;
	}

	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.dungeonchunk.IDungeonRoom#getLibraryId()
	 */
	@Override
	public long getLibraryId() {
		return this.libraryID;
	}

	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.dungeonchunk.IDungeonRoom#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.dungeonchunk.IDungeonRoom#getName()
	 */
	@Override
	public String getName() {
		return this.name;
	}
	
	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.dungeonchunk.IDungeonRoom#setFilename(java.lang.String)
	 */
	@Override
	public void setFilename(String name) {
		this.filename = name;
	}

	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.dungeonchunk.IDungeonRoom#getFilename()
	 */
	@Override
	public String getFilename() {
		return this.filename;
	}
	
	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.dungeonchunk.IDungeonRoom#getRandomTheme()
	 */
	public String getRandomTheme()
	{
		return allowedThemes.get(Math.min((int) (Math.random() * allowedThemes.size()), allowedThemes.size()));
	}
	
	public void addTheme(String theme)
	{
		theme = theme.toUpperCase();
		if(!allowedThemes.contains(theme))
		{
			allowedThemes.add(theme);
		}
	}
	
	public void removeTheme(String theme)
	{
		allowedThemes.remove(theme);
		
		if(theme.equals(defaultTheme))
		{
			if(allowedThemes.size() > 0)
			{
				defaultTheme = allowedThemes.get(0);
			}
		}
	}
	
	public void setThemes(Vector<String> themes)
	{
		allowedThemes = themes;
	}
	
	public void resetThemes()
	{
		allowedThemes = new Vector<String>();
		addTheme("default");
		setDefaultTheme("default");
	}
	
	public void setDefaultTheme(String theme)
	{
		if(allowedThemes.contains(theme))
		{
			defaultTheme = theme.toUpperCase();
		}
	}
	
	public String getDefaultTheme()
	{
		return defaultTheme;
	}
	
	public Vector<String> getThemes()
	{
		return allowedThemes;
	}
	
	public String getThemeCSV()
	{
		String csv = "";
		
		for(String s : allowedThemes)
		{
			csv = csv.concat(s).concat(",");
		}
		
		csv = csv.substring(0, csv.length()-1);
		
		return csv;
	}
	
	public void setThemeCSV(String themes)
	{
		String[] t = themes.split(",");
		for(String s : t)
		{
			addTheme(s);
		}
	}
	
	/**
	 * Adds a widget node to the room's node list.
	 *
	 * @param node the node
	 */
	public void addNode(DungeonWidgetNode node)
	{
		node.setNodeID(nodes.size());
		nodes.add(node);
	}
	
	public void addNodeFromTag(CompoundTag tag) throws DataStoreAssetException
	{
		DungeonWidgetNode tmpNode = null;
		
		try
		{
			// Get ID
			int id = ((IntTag)tag.getValue().get("id")).getValue();
			
			// Get size code
			int sizeCode = ((IntTag)tag.getValue().get("size")).getValue();
			
			// Get attachment face
			String face = ((StringTag)tag.getValue().get("face")).getValue();
			
			// Get position
			int x = ((IntTag)tag.getValue().get("x")).getValue();
			int y = ((IntTag)tag.getValue().get("y")).getValue();
			int z = ((IntTag)tag.getValue().get("z")).getValue();
			BlockVector pos = new BlockVector(x,y,z);
			
			tmpNode = new DungeonWidgetNode(
					DungeonWidget.Size.GetByCode(sizeCode),
					pos,
					DungeonWidgetNode.AttachmentFace.GetFaceByName(face),
					id
			);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new DataStoreAssetException("Could not parse widget node from tag.", "DungeonRoom");
		}
		
		this.addNode(tmpNode);
	}
	
	/**
	 * Sets a node.
	 *
	 * @param node the node
	 * @param index the index
	 */
	public void setNode(DungeonWidgetNode node, int index)
	{
		// Increase the size of the node list if the index is out of bounds
		if(nodes.size() <= index)
		{
			nodes.setSize(index);
		}
		
		node.setNodeID(index);
		nodes.set(index, node);
	}
	
	/**
	 * Gets a specified widget node.
	 *
	 * @param index the index of the node to get
	 * @return the node
	 */
	public DungeonWidgetNode getNode(int index)
	{
		return nodes.get(index);
	}
	
	/**
	 * Removes a specified widget node.
	 *
	 * @param index the index of the node to get
	 * @return the node
	 */
	public void removeNode(int index)
	{
		if(nodes.size() > index)
		{
			nodes.remove(index);
			resetNodeIDs();
		}
	}
	
	/**
	 * Gets the widget node list.
	 *
	 * @return the nodes
	 */
	public Vector<DungeonWidgetNode> getNodes()
	{
		return nodes;
	}
	
	/**
	 * Reset node ids.
	 */
	private void resetNodeIDs()
	{
		for(int i = 0; i < nodes.size(); i++)
		{
			nodes.get(i).setNodeID(i);
		}
	}
	
	/**
	 * Return a string representation of the room
	 * @return String
	 */
	public String toString()
	{
		return "{DungeonRoom<"+x+","+y+","+z+">}";
	}

	/**
	 * @return the roomSet
	 */
	public DungeonRoomSet getRoomSet() {
		return roomSet;
	}

	/**
	 * @param roomSet the roomSet to set
	 */
	public void setRoomSet(DungeonRoomSet roomSet) {
		this.roomSet = roomSet;
	}

	/**
	 * @return the libraryRoomSetID
	 */
	public long getLibraryRoomSetID() {
		return libraryRoomSetID;
	}

	/**
	 * @param libraryRoomSetID the libraryRoomSetID to set
	 */
	public void setLibraryRoomSetID(long libraryRoomSetID) {
		this.libraryRoomSetID = libraryRoomSetID;
	}

	/**
	 * @return the libraryRoomSetPath
	 */
	public String getLibraryRoomSetPath() {
		return libraryRoomSetPath;
	}

	/**
	 * @param libraryRoomSetPath the libraryRoomSetPath to set
	 */
	public void setLibraryRoomSetPath(String libraryRoomSetPath) {
		this.libraryRoomSetPath = libraryRoomSetPath;
	}
}
