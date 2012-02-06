package com.aranai.dungeonator.dungeonchunk;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Vector;

import org.bukkit.util.BlockVector;
import org.jnbt.ByteArrayTag;
import org.jnbt.CompoundTag;
import org.jnbt.IntTag;
import org.jnbt.LongTag;
import org.jnbt.NBTInputStream;
import org.jnbt.NBTOutputStream;
import org.jnbt.StringTag;

import com.aranai.dungeonator.Dungeonator;
import com.aranai.dungeonator.datastore.DataStoreAssetException;
import com.aranai.dungeonator.datastore.IAsset;
import com.aranai.dungeonator.generator.DungeonMath;

/**
 * Represents a single widget instance. Widgets are small objects used to dynamically populate rooms.
 */
public class DungeonWidget implements IAsset {

	/** Size classes */
	public static enum Size {
		TINY (0, 2, "tiny"),
		SMALL (1, 3, "small"),
		MEDIUM (2, 4, "medium"),
		LARGE (3, 5, "large"),
		HUGE (4, 6, "huge");
		
		// Numeric code for use in data storage
		private final int code;
		
		// Maximum bound in any dimension
		private final int bound;
		
		// Friendly string name
		private final String name;
		
		Size(int code, int bound, String name)
		{
			this.code = code;
			this.bound = bound;
			this.name = name;
		}
		
		public int code()
		{
			return this.code;
		}
		
		public int bound()
		{
			return bound;
		}
		
		public String getName()
		{
			return name;
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
		
		public static Size GetByName(String name)
		{
			name = name.toLowerCase();
			
			for(Size s : Size.values())
			{
				if(s.getName().equals(name))
				{
					return s;
				}
			}
			
			return null;
		}
	};
	
	/** Loaded status */
	private boolean loaded;
	
	/** Size Class */
	private Size size;
	
	/** Bounds */
	private int bound;
	
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
	private Vector<String> allowedThemes;
	
	/** Default theme */
	private String defaultTheme;
	
	public DungeonWidget() { this(Size.TINY); }
	public DungeonWidget(Size size) { this(size,0,0,0); }
	
	public DungeonWidget(Size size, int origin_x, int origin_y, int origin_z)
	{
		this(size, new BlockVector(origin_x, origin_y, origin_z));
	}
	
	public DungeonWidget(Size size, BlockVector origin)
	{
		setSize(size);
		setOrigin(origin);
		setPosition(new BlockVector(0,0,0));
		
		allowedThemes = new Vector<String>();
		defaultTheme = "default";
	}
	
	public DungeonWidget(long id, String filename, Size size, BlockVector origin)
	{
		this(size, origin);
		setLibraryID(id);
		setFilename(filename);
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
		bound = size.bound();
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
			
			byte[] blocks = new byte[(int)Math.pow(size.bound(), 3)];
			int pos = 0;
			
			for(int x = 0; x < bound; x++)
			{
				for(int z = 0; z < bound; z++)
				{
					for(int y = 0; y < bound; y++)
					{
						pos = DungeonMath.getWidgetPosFromCoords(x, y, z, size);
						blocks[pos] = (byte)chunk.getHandle().getBlock(x+position.getBlockX(), y+position.getBlockY(), z+position.getBlockZ()).getTypeId();
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
			
			byte[] blocks = new byte[(int)Math.pow(size.bound(), 3)];
			int pos = 0;
			
			for(int x = 0; x < bound; x++)
			{
				for(int z = 0; z < bound; z++)
				{
					for(int y = 0; y < bound; y++)
					{
						pos = DungeonMath.getWidgetPosFromCoords(x, y, z, size);
						blocks[pos] = (byte)chunk.getHandle().getBlock(x+position.getBlockX(), y+position.getBlockY(), z+position.getBlockZ()).getData();
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
		
		if(csv.length() > 0)
		{
			csv = csv.substring(0, csv.length()-1);
		}
		
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
	
	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.datastore.IAsset#saveAsset(java.lang.String)
	 */
	@Override
	public boolean saveAsset(String path, String filename) throws DataStoreAssetException {
		String fullPath = path+filename+".nbt";
		
		// Schematic tags
		HashMap<String,org.jnbt.Tag> tags = new HashMap<String,org.jnbt.Tag>();
		HashMap<String,org.jnbt.Tag> metaTags = new HashMap<String,org.jnbt.Tag>();
		HashMap<String,org.jnbt.Tag> widgetTags = new HashMap<String,org.jnbt.Tag>();
		
		/*
		 * Build asset tags
		 */
		
		// Meta: Author
		metaTags.put("author", new StringTag("author", Dungeonator.getInstance().getChunkEditor().getActiveEditor().getName()));
		
		// Meta: Date created/updated
		metaTags.put("dateUpdated", new LongTag("dateUpdated", System.currentTimeMillis()));
		
		// Add meta tag
		tags.put("meta", new CompoundTag("meta", metaTags));
		
		// Widget: Size
		widgetTags.put("size", new IntTag("size", this.size.code()));
		
		// Widget: Blocks
		widgetTags.put("blocks", new ByteArrayTag("blocks", getRawBlocks()));
		
		// Widget: Block Data
		widgetTags.put("blockData", new ByteArrayTag("blockData", getRawBlockData()));
		
		// TODO: Widget: Tile Entities
		
		// TODO: Widget: Themes
		
		// Add widget tags to schematic
		tags.put("widget", new CompoundTag("widget", widgetTags));
		
		// Build final schematic tag
		CompoundTag schematic = new CompoundTag("WidgetSchematic", tags);
			
		// Save asset tag to file
		try {
			// Create file output stream
			OutputStream output = new FileOutputStream(fullPath);
		
			// Create NBT output stream
			NBTOutputStream os = new NBTOutputStream(output);
			
			// Write the room to the file
			os.writeTag(schematic);
			
			// Close the NBT output stream
			os.close();
			
			// Close the file output stream
			output.flush();
			output.close();
		} catch (IOException e) { e.printStackTrace(); throw new DataStoreAssetException("Could not save asset file.", fullPath); }
			
		// Set local asset tag
		setAssetTag(schematic);
		
		return false;
	}
	
	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.datastore.IAsset#loadAsset(java.lang.String)
	 */
	@Override
	public void loadAsset(String path, String filename) throws DataStoreAssetException {
		String fullPath = path+filename+".nbt";
		CompoundTag schematic = null;
		
		// Load the file
		try {
			// Open file input stream
			FileInputStream fis = new FileInputStream(fullPath);
			
			try {
				// Open NBT input stream
				NBTInputStream nis = new NBTInputStream(fis);
				
				// Read NBT data
				org.jnbt.Tag tag = nis.readTag();
				
				if(tag instanceof CompoundTag)
				{
					schematic = (CompoundTag)tag;
				}
			} catch (IOException e) { e.printStackTrace(); throw new DataStoreAssetException("Failed to load widget from file. IOException.", fullPath); }
		} catch (FileNotFoundException e) { e.printStackTrace(); throw new DataStoreAssetException("Failed to load widget from file. File not found.", fullPath); }
		
		// Set local asset tag
		if(schematic != null)
		{
			try { setAssetTag(schematic); } catch (DataStoreAssetException e) { throw new DataStoreAssetException("Could not parse widget tag: "+e.getReason(), fullPath); }
			return;
		}
		
		// Something bad happened
		throw new DataStoreAssetException("Failed to load widget from file.", fullPath);
	}
	
	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.datastore.IAsset#getAssetTag()
	 */
	@Override
	public CompoundTag getAssetTag() throws DataStoreAssetException {
		if(this.isLoaded()) { return schematic; }
		throw new DataStoreAssetException("Widget Asset is not loaded.", filename);
	}
	
	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.datastore.IAsset#setAssetTag()
	 */
	@Override
	public void setAssetTag(CompoundTag tag) throws DataStoreAssetException {
		try
		{
			// Parse asset tag
			CompoundTag widgetTag = (CompoundTag)tag.getValue().get("widget");
		
			// Size class
			size = Size.GetByCode(((IntTag)widgetTag.getValue().get("size")).getValue());
		
			// Blocks
			tempRawBlocks = ((ByteArrayTag)widgetTag.getValue().get("blocks")).getValue();
		
			// Block data
			tempRawBlockData = ((ByteArrayTag)widgetTag.getValue().get("blockData")).getValue();
		
			// TODO: Themes
		}
		catch(Exception e) { e.printStackTrace(); throw new DataStoreAssetException("Could not set asset tag. Structure may be invalid.", ""); }
		
		// Set local asset tag
		schematic = tag;
	}
	
	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.datastore.IAsset#isLoaded()
	 */
	@Override
	public boolean isLoaded() {
		return loaded;
	}
}
