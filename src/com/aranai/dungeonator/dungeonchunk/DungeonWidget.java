package com.aranai.dungeonator.dungeonchunk;

import org.bukkit.util.BlockVector;
import org.jnbt.CompoundTag;

import com.aranai.dungeonator.generator.DungeonMath;

/**
 * Represents a single widget instance. Widgets are small objects used to dynamically populate rooms.
 */
public class DungeonWidget {

	/** Size classes */
	public static enum Size {
		TINY (0, 2),
		SMALL (1, 3),
		MEDIUM (2, 4),
		LARGE (3, 6),
		HUGE (4, 8);
		
		// Numeric code for use in data storage
		private final int code;
		
		// Maximum bound in any dimension
		private final int bound;
		
		Size(int code, int bound)
		{
			this.code = code;
			this.bound = bound;
		}
		
		public int code()
		{
			return this.code;
		}
		
		public int bound()
		{
			return this.bound;
		}
		
		public static Size GetByCode(int code)
		{
			for(Size s : Size.values())
			{
				if(s.code() == code)
				{
					return s;
				}
			}
			
			return null;
		}
	};
	
	/** Size Class */
	private Size size;
	
	/** Bounds */
	private BlockVector bounds;
	
	/** Origin (attachment point) */
	private BlockVector origin;
	
	/** Position (if loaded) */
	private BlockVector position;
	
	/** Library ID (if loaded) */
	private long libraryID = -1;
	
	/** Filename (if loaded) */
	private String filename = "";
	
	// Parent room (if loaded)
	private DungeonRoom room = null;
	
	/** Temporary raw schematic */
	private CompoundTag schematic;
	
	/** Temporary raw block array */
	private byte[] tempRawBlocks;
	
	/** Temporary raw block data array */
	private byte[] tempRawBlockData;
	
	/** Allowed themes */
	private java.util.Vector<String> allowedThemes;
	
	/** Default theme */
	private String defaultTheme;
	
	public DungeonWidget() { this(Size.TINY); }
	public DungeonWidget(Size size) { this(size, size.bound(), size.bound(), size.bound()); }
	public DungeonWidget(Size size, int bound_x, int bound_y, int bound_z) { this(size, bound_x, bound_y, bound_z, 0, 0, 0); }
	
	public DungeonWidget(Size size, int bound_x, int bound_y, int bound_z, int origin_x, int origin_y, int origin_z)
	{
		this(size, new BlockVector(bound_x, bound_y, bound_z), new BlockVector(origin_x, origin_y, origin_z));
	}
	
	public DungeonWidget(Size size, BlockVector bounds, BlockVector origin)
	{
		setSize(size);
		setBounds(bounds);
		setOrigin(origin);
		setPosition(new BlockVector(0,0,0));
	}
	
	public DungeonWidget(long id, String filename, Size size, BlockVector bounds, BlockVector origin)
	{
		this(size, bounds, origin);
		setLibraryID(id);
		setFilename(filename);
	}
	
	/**
	 * Gets the bounds of the widget.
	 *
	 * @return the bounds
	 */
	public BlockVector getBounds()
	{
		return bounds;
	}
	
	/**
	 * Sets the bounds of the widget.
	 *
	 * @param bound_x the bound_x
	 * @param bound_y the bound_y
	 * @param bound_z the bound_z
	 */
	public void setBounds(int bound_x, int bound_y, int bound_z)
	{
		setBounds(new BlockVector(bound_x, bound_y, bound_z));
	}
	
	/**
	 * Sets the bounds of the widget.
	 *
	 * @param bounds the new bounds
	 */
	public void setBounds(BlockVector bounds)
	{
		this.bounds = bounds;
	}
	
	/**
	 * Gets the origin of the widget.
	 *
	 * @return the origin
	 */
	public BlockVector getOrigin()
	{
		return origin;
	}
	
	/**
	 * Sets the origin of the widget.
	 *
	 * @param origin_x the origin_x
	 * @param origin_y the origin_y
	 * @param origin_z the origin_z
	 */
	public void setOrigin(int origin_x, int origin_y, int origin_z)
	{
		setOrigin(new BlockVector(origin_x, origin_y, origin_z));
	}
	
	/**
	 * Sets the origin.
	 *
	 * @param origin the new origin
	 */
	public void setOrigin(BlockVector origin)
	{
		this.origin = origin;
	}
	
	/**
	 * Gets the position of the widget.
	 *
	 * @return the position
	 */
	public BlockVector getPosition()
	{
		return position;
	}
	
	/**
	 * Sets the position of the widget.
	 *
	 * @param pos_x the pos_x
	 * @param pos_y the pos_y
	 * @param pos_z the pos_z
	 */
	public void setPosition(int pos_x, int pos_y, int pos_z)
	{
		setPosition(new BlockVector(pos_x, pos_y, pos_z));
	}
	
	/**
	 * Sets the position.
	 *
	 * @param position the new position
	 */
	public void setPosition(BlockVector pos)
	{
		this.position = pos;
	}
	
	/**
	 * @return the size
	 */
	public Size getSize() {
		return size;
	}
	
	/**
	 * @param size the size to set
	 */
	public void setSize(Size size) {
		this.size = size;
	}
	
	/**
	 * @return the libraryID
	 */
	public long getLibraryID() {
		return libraryID;
	}
	
	/**
	 * @param libraryID the libraryID to set
	 */
	public void setLibraryID(long libraryID) {
		this.libraryID = libraryID;
	}
	
	/**
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}
	
	/**
	 * @param filename the filename to set
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	/**
	 * @return the room
	 */
	public DungeonRoom getRoom() {
		return room;
	}
	
	/**
	 * @param room the room to set
	 */
	public void setRoom(DungeonRoom room) {
		this.room = room;
	}
	
	/**
	 * Sets the location.
	 *
	 * @param room the room
	 * @param position the position
	 */
	public void setLocation(DungeonRoom room, BlockVector position)
	{
		this.room = room;
		this.position = position;
	}
	
	/**
	 * Gets the raw blocks.
	 *
	 * @return the raw blocks
	 */
	public byte[] getRawBlocks()
	{
		if(room != null && room.getDungeonChunk().isReady())
		{
			/*
			 * If the chunk is in ready state, retrieve the real-world blocks
			 */
			
			DungeonChunk chunk = room.getDungeonChunk();
			
			byte[] blocks = new byte[size.bound()^3];
			int pos = 0;
			
			for(int x = position.getBlockX(); x < position.getBlockX()+bounds.getBlockX(); x++)
			{
				for(int z = position.getBlockZ(); z < position.getBlockZ()+bounds.getBlockZ(); z++)
				{
					for(int y = position.getBlockY(); y < position.getBlockY()+bounds.getBlockY(); y++)
					{
						pos = DungeonMath.getWidgetPosFromCoords(x-position.getBlockX(), y-position.getBlockY(), z-position.getBlockZ(), size);
						blocks[pos] = (byte)chunk.getHandle().getBlock(x, (room.getY()*8)+y, z).getTypeId();
					}
				}
			}
			
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
	 * Sets the raw blocks.
	 *
	 * @param blocks the new raw blocks
	 */
	public void setRawBlocks(byte[] blocks)
	{
		tempRawBlocks = blocks;
	}
	
	/**
	 * Gets the raw block data.
	 *
	 * @return the raw block data
	 */
	public byte[] getRawBlockData()
	{
		if(room != null && room.getDungeonChunk().isReady())
		{
			/*
			 * If the chunk is in ready state, retrieve the real-world blocks
			 */
			
			DungeonChunk chunk = room.getDungeonChunk();
			
			byte[] blocks = new byte[size.bound()^3];
			int pos = 0;
			
			for(int x = position.getBlockX(); x < position.getBlockX()+bounds.getBlockX(); x++)
			{
				for(int z = position.getBlockZ(); z < position.getBlockZ()+bounds.getBlockZ(); z++)
				{
					for(int y = position.getBlockY(); y < position.getBlockY()+bounds.getBlockY(); y++)
					{
						pos = DungeonMath.getWidgetPosFromCoords(x-position.getBlockX(), y-position.getBlockY(), z-position.getBlockZ(), size);
						blocks[pos] = (byte)chunk.getHandle().getBlock(x, (room.getY()*8)+y, z).getData();
					}
				}
			}
			
			return blocks;
		}
		else if(tempRawBlockData != null)
		{
			/*
			 * If the chunk is NOT ready, retrieve the local blocks
			 */
			
			return tempRawBlockData;
		}
		
		return null;
	}
	
	/**
	 * Sets the raw block data.
	 *
	 * @param blockData the new raw block data
	 */
	public void setRawBlockData(byte[] blockData)
	{
		tempRawBlockData = blockData;
	}
	
	/**
	 * @return the schematic
	 */
	public CompoundTag getSchematic() {
		return schematic;
	}
	
	/**
	 * @param schematic the schematic to set
	 */
	public void setSchematic(CompoundTag schematic) {
		this.schematic = schematic;
	}
	
	/**
	 * Gets the random theme.
	 *
	 * @return the random theme
	 */
	public String getRandomTheme()
	{
		return allowedThemes.get(Math.min((int) (Math.random() * allowedThemes.size()), allowedThemes.size()));
	}
	
	/**
	 * Adds the theme.
	 *
	 * @param theme the theme
	 */
	public void addTheme(String theme)
	{
		theme = theme.toUpperCase();
		if(!allowedThemes.contains(theme))
		{
			allowedThemes.add(theme);
		}
	}
	
	/**
	 * Removes the theme.
	 *
	 * @param theme the theme
	 */
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
	
	/**
	 * Sets the themes.
	 *
	 * @param themes the new themes
	 */
	public void setThemes(java.util.Vector<String> themes)
	{
		allowedThemes = themes;
	}
	
	/**
	 * Reset themes.
	 */
	public void resetThemes()
	{
		allowedThemes = new java.util.Vector<String>();
		addTheme("default");
		setDefaultTheme("default");
	}
	
	/**
	 * Sets the default theme.
	 *
	 * @param theme the new default theme
	 */
	public void setDefaultTheme(String theme)
	{
		if(allowedThemes.contains(theme))
		{
			defaultTheme = theme.toUpperCase();
		}
	}
	
	/**
	 * Gets the default theme.
	 *
	 * @return the default theme
	 */
	public String getDefaultTheme()
	{
		return defaultTheme;
	}
	
	/**
	 * Gets the themes.
	 *
	 * @return the themes
	 */
	public java.util.Vector<String> getThemes()
	{
		return allowedThemes;
	}
	
	/**
	 * Gets the theme csv.
	 *
	 * @return the theme csv
	 */
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
	
	/**
	 * Sets the theme csv.
	 *
	 * @param themes the new theme csv
	 */
	public void setThemeCSV(String themes)
	{
		String[] t = themes.split(",");
		for(String s : t)
		{
			addTheme(s);
		}
	}
}
