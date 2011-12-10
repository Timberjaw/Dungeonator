package com.aranai.dungeonator.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.CraftChunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.block.Sign;
import org.jnbt.ByteArrayTag;
import org.jnbt.ByteTag;
import org.jnbt.CompoundTag;
import org.jnbt.IntTag;
import org.jnbt.LongTag;
import org.jnbt.NBTInputStream;
import org.jnbt.NBTOutputStream;
import org.jnbt.StringTag;

import com.aranai.dungeonator.event.DCommandEvent;
import com.aranai.dungeonator.Direction;
import com.aranai.dungeonator.Dungeonator;
import com.aranai.dungeonator.amt.ThemeManager;
import com.aranai.dungeonator.amt.ThemeMaterialTranslation;
import com.aranai.dungeonator.dungeonchunk.DungeonChunk;
import com.aranai.dungeonator.dungeonchunk.DungeonRoom;
import com.aranai.dungeonator.dungeonchunk.DungeonRoomDoorway;

/**
 * Handles in-game chunk editing for tiles. Supports manual construction and
 * cuboid operations. The editing operation is destructive, and should be
 * performed on test maps or an editor-specific map only. The editor
 * automatically generates doorway hints and chunk boundaries to assist in
 * the editing process.
 * 
 * Meta data tagging commands are provided for specifying doorways, internal
 * pathing, widget regions, and so forth.
 */
public class DungeonRoomEditor {
	
	/** Dungeonator instance */
	private Dungeonator dungeonator;
	
	/** Flag: editor is active. */
	private boolean isActive;
	
	/** Flag: editor has unsaved changes. */
	private boolean hasUnsavedChanges;
	
	/** The chunk. */
	private DungeonChunk chunk;
	
	/** The room. */
	private DungeonRoom room;
	
	/** The active path. */
	private String activePath;
	
	/** The active file. */
	private String activeFile;
	
	/** The current room's theme. */
	private String activeTheme;
	
	/** The editing player. */
	private Player editor;
	
	/** The floor height for the editor. This will be set to the player's y coordinate when starting the editor. */
	private int editor_y = -1;
	
	/** Test library */
	private Vector<String> testLibrary;
	
	/** The theme manager */
	private ThemeManager themeManager;
	
	/**
	 * Instantiates the dungeon chunk editor.
	 */
	public DungeonRoomEditor(Dungeonator d)
	{
		dungeonator = d;
		chunk = null;
		isActive = false;
		hasUnsavedChanges = false;
		activeFile = "";
		activePath = "";
		activeTheme = "DEFAULT";
		testLibrary = new Vector<String>();
		themeManager = d.getThemeManager();
	}
	
	/**
	 * Checks if the room editor is active.
	 *
	 * @return true, if the room editor is active
	 */
	public boolean isActive()
	{
		return isActive;
	}
	
	/**
	 * Gets the active editor.
	 *
	 * @return the active editor
	 */
	public Player getActiveEditor()
	{
		return editor;
	}
	
	/**
	 * Starts the editing operation. The specified chunk and adjacent chunks
	 * will be flattened. Doorway and border hints will be generated.
	 *
	 * @param c the c
	 */
	public void start(boolean flatten, boolean hint)
	{
		if(flatten)
		{
			/*
			 * Flatten 9 chunks total: the selected chunk and the 8 surrounding chunks
			 */
			
			editor.sendMessage("Flattening chunks...");
			
			Material mat;
			
			for(int fX = chunk.getX()-1; fX <= chunk.getX()+1; fX++)
			{
				for(int fZ = chunk.getZ()-1; fZ <= chunk.getZ()+1; fZ++)
				{
					if(fX == chunk.getX() && fZ == chunk.getZ())
					{
						mat = Material.COBBLESTONE;
					}
					else
					{
						mat = Material.STONE;
					}
					this.flattenChunk(chunk.getWorld().getChunkAt(fX, fZ), mat);
				}
			}
			
			/*
			 * Teleport player to avoid killing them
			 */
			
			editor.teleport(new Location(editor.getWorld(), editor.getLocation().getX(), 10, editor.getLocation().getZ()));
		}
		
		if(hint)
		{
			/*
			 * Add corner hints
			 */
			
			editor.sendMessage("Adding corner hints...");
			
			World w = chunk.getWorld();
			
			int blockX = chunk.getX() << 4;
			int blockZ = chunk.getZ() << 4;
			
			for(int y = 8; y < 16; y++)
			{
				w.getBlockAt(blockX-1, y, blockZ-1).setType(Material.OBSIDIAN);
				w.getBlockAt(blockX-1, y, blockZ+15+1).setType(Material.OBSIDIAN);
				w.getBlockAt(blockX+15+1, y, blockZ+15+1).setType(Material.OBSIDIAN);
				w.getBlockAt(blockX+15+1, y, blockZ-1).setType(Material.OBSIDIAN);
			}
			
			/*
			 * Add doorway hints
			 */
			
			for(int y = 8; y < 12; y++)
			{
				w.getBlockAt(blockX+0, y, blockZ-1).setType(Material.OBSIDIAN);
				w.getBlockAt(blockX+4, y, blockZ-1).setType(Material.OBSIDIAN);
				w.getBlockAt(blockX+5, y, blockZ-1).setType(Material.OBSIDIAN);
				w.getBlockAt(blockX+10, y, blockZ-1).setType(Material.OBSIDIAN);
				w.getBlockAt(blockX+11, y, blockZ-1).setType(Material.OBSIDIAN);
				w.getBlockAt(blockX+15, y, blockZ-1).setType(Material.OBSIDIAN);
				
				w.getBlockAt(blockX+0, y, blockZ+16).setType(Material.OBSIDIAN);
				w.getBlockAt(blockX+4, y, blockZ+16).setType(Material.OBSIDIAN);
				w.getBlockAt(blockX+5, y, blockZ+16).setType(Material.OBSIDIAN);
				w.getBlockAt(blockX+10, y, blockZ+16).setType(Material.OBSIDIAN);
				w.getBlockAt(blockX+11, y, blockZ+16).setType(Material.OBSIDIAN);
				w.getBlockAt(blockX+15, y, blockZ+16).setType(Material.OBSIDIAN);
				
				w.getBlockAt(blockX-1, y, blockZ+0).setType(Material.OBSIDIAN);
				w.getBlockAt(blockX-1, y, blockZ+4).setType(Material.OBSIDIAN);
				w.getBlockAt(blockX-1, y, blockZ+5).setType(Material.OBSIDIAN);
				w.getBlockAt(blockX-1, y, blockZ+10).setType(Material.OBSIDIAN);
				w.getBlockAt(blockX-1, y, blockZ+11).setType(Material.OBSIDIAN);
				w.getBlockAt(blockX-1, y, blockZ+15).setType(Material.OBSIDIAN);
				
				w.getBlockAt(blockX+16, y, blockZ+0).setType(Material.OBSIDIAN);
				w.getBlockAt(blockX+16, y, blockZ+4).setType(Material.OBSIDIAN);
				w.getBlockAt(blockX+16, y, blockZ+5).setType(Material.OBSIDIAN);
				w.getBlockAt(blockX+16, y, blockZ+10).setType(Material.OBSIDIAN);
				w.getBlockAt(blockX+16, y, blockZ+11).setType(Material.OBSIDIAN);
				w.getBlockAt(blockX+16, y, blockZ+15).setType(Material.OBSIDIAN);
			}
			
			for(int x = blockX; x < blockX + 16; x++)
			{
				w.getBlockAt(x, 12, blockZ-1).setType(Material.OBSIDIAN);
				w.getBlockAt(x, 12, blockZ+16).setType(Material.OBSIDIAN);
			}
			
			for(int z = blockZ; z < blockZ + 16; z++)
			{
				w.getBlockAt(blockX-1, 12, z).setType(Material.OBSIDIAN);
				w.getBlockAt(blockX+16, 12, z).setType(Material.OBSIDIAN);
			}
			
			editor.sendMessage("Adding doorway hints...");
		}
		
		/*
		 * Mark editor as active
		 */
		
		isActive = true;
		hasUnsavedChanges = true;
		
		this.room = new DungeonRoom(this.chunk, 1);
	}
	
	/**
	 * Load.
	 *
	 * @param file the file
	 */
	public void load(String path, String name)
	{
		CompoundTag schematic = null;
		
		// Notify the editor that we are loading the file
		editor.sendMessage("Loading: " + path + name);
		
		// If the editor is not started, start it
		if(!this.isActive)
		{
			this.cmdNew(new DCommandEvent(editor, "new", new String[]{"flatten:true", "hint:true"}));
		}
		
		// Load the file
		try {
			// Open file input stream
			FileInputStream fis = new FileInputStream(path+File.separator+name);
			
			try {
				// Open NBT input stream
				NBTInputStream nis = new NBTInputStream(fis);
				
				// Read NBT data
				org.jnbt.Tag tag = nis.readTag();
				
				if(tag instanceof CompoundTag)
				{
					schematic = (CompoundTag)tag;
				}
			} catch (IOException e) { e.printStackTrace(); }
		} catch (FileNotFoundException e) { e.printStackTrace(); }
		
		// Verify the data was loaded
		if(schematic != null)
		{
			// Get blocks
			byte[] blocks = ((ByteArrayTag)schematic.getValue().get("blocks")).getValue();
			
			// Get block data
			byte[] blockData = ((ByteArrayTag)schematic.getValue().get("blockData")).getValue();
			
			// Add blocks to chunk
			for(int x = 0; x < 16; x++)
			{
				for(int z = 0; z < 16; z++)
				{
					for(int y = 0; y < 8; y++)
					{
						// Set block type and basic data value
						this.chunk.getHandle().getBlock(x, y+this.editor_y, z).setTypeIdAndData(blocks[DungeonMath.getRoomPosFromCoords(x, y, z)], (byte)0, false);
					}
				}
			}
			
			// Set data values
			// This has to run after all the blocks have been added, to prevent weirdness with MC overriding the data values
			for(int x = 0; x < 16; x++)
			{
				for(int z = 0; z < 16; z++)
				{
					for(int y = 0; y < 8; y++)
					{
						this.chunk.getHandle().getBlock(x, y+this.editor_y, z).setData(blockData[DungeonMath.getRoomPosFromCoords(x, y, z)], false);
					}
				}
			}
			
			// Handle tile entities
			if(schematic.getValue().containsKey("tileEntities"))
			{
				Map<String,org.jnbt.Tag> tileEntities = ((CompoundTag)schematic.getValue().get("tileEntities")).getValue();
				
				for(org.jnbt.Tag t : tileEntities.values())
				{
					// TODO: this.chunk.addTileEntityFromTag(t);
					Map<String,org.jnbt.Tag> ct = ((CompoundTag)t).getValue();
					Map<String,org.jnbt.Tag> data = ((CompoundTag)ct.get("data")).getValue();
					StringTag typeTag = (StringTag)ct.get("type");
					String type = typeTag.getValue();
					
					if(type.equalsIgnoreCase("sign"))
					{
						// Debug info
						editor.sendMessage("Found tile entity of type 'sign'");
						
						// Get block
						int x = ((IntTag)data.get("x")).getValue();
						int y = ((IntTag)data.get("y")).getValue();
						int z = ((IntTag)data.get("z")).getValue();
						Block b = this.chunk.getHandle().getBlock(x, y+this.editor_y, z);
						BlockState bs = b.getState();
						
						// Get lines
						if(bs instanceof Sign)
						{
							Sign s = (Sign)bs;
							s.setLine(0, ((StringTag)data.get("line1")).getValue());
							s.setLine(1, ((StringTag)data.get("line2")).getValue());
							s.setLine(2, ((StringTag)data.get("line3")).getValue());
							s.setLine(3, ((StringTag)data.get("line4")).getValue());
							s.update();
						}
					}
				}
			}
			
			// Load doorways
			int exitCount = 0;
			this.room.resetDoorways();
			byte[] exits = ((ByteArrayTag)schematic.getValue().get("exits")).getValue();
			
			for(byte i = 0; i < exits.length; i++)
			{
				if(exits[i] != 0)
				{
					this.room.setDoorway(i, true);
					exitCount++;
				}
			}
			
			// Load themes
			StringTag themeTag = (StringTag)schematic.getValue().get("themes");
			if(themeTag != null)
			{
				room.resetThemes();
				String themes = themeTag.getValue();
				for(String s : themes.split(","))
				{
					room.addTheme(s);
				}
			}
			
			// Load default theme
			StringTag defaultThemeTag = (StringTag)schematic.getValue().get("defaultTheme");
			if(defaultThemeTag != null)
			{
				room.setDefaultTheme(defaultThemeTag.getValue());
			}
			
			// Set filename and path
			activeFile = name;
			activePath = path;
			
			// Semi-debug: notify the user of how many doorways were added
			editor.sendMessage("Adding "+exitCount+" doorways.");
		}
	}
	
	/**
	 * Cancel the edit operation. The DungeonChunk will be reset, no data
	 * will be saved.
	 */
	public void cancel()
	{
		// Cancel the edit operation
		
		if(isActive)
		{
			hasUnsavedChanges = false;
			isActive = false;
			activeFile = "";
		}
	}
	
	/**
	 * Save the currently active room with an specified path and
	 * specified name.
	 *
	 * @param path the path
	 * @param name the name
	 */
	public void save(String path, String name, boolean saveToLibrary)
	{
		// Make sure the editor is active
		if(!isActive)
		{
			editor.sendMessage("Editor is not active.");
			return;
		}
		
		// Notify the editor that we are saving the file
		editor.sendMessage("Saving to: " + path + name);
		
		/*
		 * Example NBT Format:
		 * 
		 * CompoundTag("DungeonRoomSchematic"):
		 *  - ByteTag("type"): byte								Type ID for the DungeonRoom
		 * 	- ByteArrayTag("exits"): byte[]						Exits from the room
		 *  - StringTag("themes"): string[]					Valid themes for the room
		 * 	- CompoundTag("widgetSpawns"):						Potential widget spawn locations for the room
		 * 		- CompoundTag("widget"): 						A single widget spawn location (origin is at NW corner of the widget to be placed)
		 * 			- ByteTag("type") : byte						Type ID for the widget spawn location, or 0 if any widget type is acceptable
		 * 			- ShortTag("locX") : short						X coordinate for the widget spawn location
		 * 			- ShortTag("locY") : short						Y coordinate for the widget spawn location
		 * 			- ShortTag("locZ") : short						Z coordinate for the widget spawn location
		 * 			- ShortTag("maxX") : short						Maximum X size for a widget placed at this location
		 *  		- ShortTag("maxY") : short						Maximum Y size for a widget placed at this location
		 *  		- ShortTag("maxZ") : short						Maximum Z size for a widget placed at this location
		 * 	- ByteArrayTag("blocks", byte[])					Raw block data
		 */
		
		// Schematic tags
		HashMap<String,org.jnbt.Tag> tags = new HashMap<String,org.jnbt.Tag>();
		
		// Meta tags
		HashMap<String,org.jnbt.Tag> metaTags = new HashMap<String,org.jnbt.Tag>();
		
		// Author
		metaTags.put("author", new StringTag("author", this.editor.getName()));
		
		// Date created/updated
		metaTags.put("dateUpdated", new LongTag("dateUpdated", System.currentTimeMillis()));
		
		// Meta compound tag
		tags.put("meta", new CompoundTag("meta", metaTags));
		
		// Tile Entity tags
		HashMap<String,org.jnbt.Tag> tileEntityTags = new HashMap<String,org.jnbt.Tag>();
		
		BlockState[] tileEntities = room.getTileEntities();
		
		for(BlockState b : tileEntities)
		{
			// Entity compound tag
			HashMap<String,org.jnbt.Tag> entityTags = new HashMap<String,org.jnbt.Tag>();
			
			// Sign Entity
			if(b instanceof Sign)
			{
				// Set type as sign
				entityTags.put("type", new StringTag("type", "sign"));
				
				// Save text lines 1-4
				Sign s = (Sign)b;
				HashMap<String,org.jnbt.Tag> lineTags = new HashMap<String,org.jnbt.Tag>();
				lineTags.put("x", new IntTag("x", s.getX() & 0xF));
				lineTags.put("y", new IntTag("y", (s.getY() & 0x7)));
				lineTags.put("z", new IntTag("z", s.getZ() & 0xF));
				lineTags.put("line1", new StringTag("line1", s.getLine(0)));
				lineTags.put("line2", new StringTag("line2", s.getLine(1)));
				lineTags.put("line3", new StringTag("line3", s.getLine(2)));
				lineTags.put("line4", new StringTag("line4", s.getLine(3)));
				entityTags.put("data", new CompoundTag("data", lineTags));
				
				tileEntityTags.put(b.toString(), new CompoundTag(b.toString(), entityTags));
			}
		}
		
		// Tile Entity compound tag
		tags.put("tileEntities", new CompoundTag("tileEntities", tileEntityTags));
		
		// Room type ID
		tags.put("type", new ByteTag("type", (byte) 0));
		
		// Room exits
		tags.put("exits", new ByteArrayTag("exits", room.getDoorwaysRaw()));
		
		// Room themes
		tags.put("themes", new StringTag("themes", room.getThemeCSV()));
		
		// Room default theme
		tags.put("defaultTheme", new StringTag("defaultTheme", room.getDefaultTheme()));
		
		// Loop through themes and save cached copies of the translated versions
		byte[] blockData = this.room.getRawBlockData();
		byte[] blocks = this.room.getRawBlocks();
		
		try {
			for(String s : this.room.getThemes())
			{
				// Handle path and filename for processed versions
				String tmpPath = path;
				String tmpName = name;
				if(!s.equals(room.getDefaultTheme()))
				{
					path = path.concat("processed"+File.separator);
					name = name.concat("."+s.toUpperCase());
				}
				
				ThemeMaterialTranslation mt = null;
				byte[] tmpBlocks = new byte[blocks.length];
				byte[] tmpBlockData = new byte[blockData.length];
				
				// Translate the block data
				for(int i = 0; i < blocks.length; i++)
				{
					mt = themeManager.getFullTranslation(room.getDefaultTheme(), s, new ThemeMaterialTranslation(blocks[i], blockData[i]));
					if(mt != null && mt.type > 0)
					{
						tmpBlocks[i] = mt.type;
						tmpBlockData[i] = mt.sub;
					}
				}
				
				// Blocks
				tags.put("blocks", new ByteArrayTag("blocks", tmpBlocks));
				
				// Block data values
				tags.put("blockData", new ByteArrayTag("blockData", tmpBlockData));
				
				CompoundTag schematic = new CompoundTag("DungeonRoomSchematic", tags);
				
				// Create file output stream
				OutputStream output = new FileOutputStream(path+name+".nbt");
				
				// Create NBT output stream
				NBTOutputStream os = new NBTOutputStream(output);
				
				// Write the room to the file
				os.writeTag(schematic);
				
				// Close the NBT output stream
				os.close();
				
				// Close the file output stream
				output.flush();
				output.close();
				
				// Reset path and name
				path = tmpPath;
				name = tmpName;
				
				// Set active file and path
				activeFile = name;
				activePath = path;
			}
		} catch (IOException e) { e.printStackTrace(); }
		
		// Save to test library
		if(!testLibrary.contains(name))
		{
			editor.sendMessage("Added room to test library.");
			this.testLibrary.add(name);
		}
		
		// Save to real library
		if(saveToLibrary)
		{
			room.setFilename(name);
			dungeonator.getDataManager().saveLibraryRoom(room);
		}
	}
	
	/**
	 * Process an edit command.
	 *
	 * @param p the player
	 * @param command the command
	 * @param args additional arguments
	 */
	public void onCommand(DCommandEvent cmd)
	{
		this.editor = cmd.getPlayer();
		
		if(cmd.getCmd().equals("new"))
		{
			this.cmdNew(cmd);
		}
		else if(cmd.getCmd().equals("load"))
		{
			this.cmdLoad(cmd);
		}
		else if(cmd.getCmd().equals("cancel"))
		{
			this.cmdCancel(cmd);
		}
		else if(cmd.getCmd().equals("save"))
		{
			this.cmdSave(cmd);
		}
		else if(cmd.getCmd().equals("exits") || cmd.getCmd().equals("doors"))
		{
			this.cmdExits(cmd);
		}
		else if(cmd.getCmd().equals("theme"))
		{
			this.cmdTheme(cmd);
		}
	}
	
	/**
	 * Command: New DungeonChunk
	 * 
	 * This will activate the editor with a blank chunk.
	 *
	 * @param p the p
	 * @param args the args
	 */
	public void cmdNew(DCommandEvent cmd)
	{
		/*
		 * Get 'flatten' and 'hint' args
		 */
		
		boolean flatten = cmd.getNamedArgBool("flatten", true);
		boolean hint = cmd.getNamedArgBool("hint", true);
		
		/*
		 * Start the editor
		 */
		
		int playerY = cmd.getPlayer().getLocation().getBlockY();
		int roundedY = playerY - (playerY % 8); // Round the player's y coordinate to the nearest multiple of 8
		this.editor_y = Math.max(Math.min(roundedY, 112), 8); // Force the floor to be placed no lower than 8 and no higher than 112
		this.chunk = new DungeonChunk(cmd.getChunk());
		this.start(flatten, hint);
		
		/*
		 * Reset the active filename and path
		 */
		
		this.activeFile = "";
		this.activePath = "";
		
		/*
		 * Let the player know what we've done
		 */
		
		cmd.getPlayer().sendMessage("[Dungeonator][Editor] Started new DungeonChunk at "+cmd.getChunk().getWorld().getName()+":"+cmd.getChunk().getX()+","+cmd.getChunk().getZ());
	}
	
	/**
	 * Command: Load DungeonChunk
	 * 
	 * This will activate the editor with a specified chunk loaded.
	 *
	 * @param cmd the command event instance
	 */
	public void cmdLoad(DCommandEvent cmd)
	{
		// Get the path and filename, or use defaults if no values were specified
		String path = cmd.getNamedArgString("path", Dungeonator.TileFolderPath);
		String name = cmd.getNamedArgString("name", "")+".nbt";
		
		if(name.isEmpty())
		{
			cmd.getPlayer().sendMessage("No file specified.");
		}
		else
		{
			// Load the chunk
			this.load(path, name);
		}
	}
	
	/**
	 * Command: Cancel Edit
	 * 
	 * This will cancel the editing process.
	 *
	 * @param cmd the command event instance
	 */
	public void cmdCancel(DCommandEvent cmd)
	{
		this.cancel();
		cmd.getPlayer().sendMessage("[Dungeonator][Editor] Cancelled edit operation.");
	}
	
	/**
	 * Command: Save DungeonChunk
	 * 
	 * This will save the current DungeonChunk. The editor will remain active.
	 *
	 * @param p the player triggering the command
	 * @param args the args
	 */
	public void cmdSave(DCommandEvent cmd)
	{
		String path = Dungeonator.TileFolderPath;
		String name = "Unnamed-"+System.currentTimeMillis();
		
		// Check for active path and name
		if(!activePath.equals("")) { path = activePath; }
		if(!activeFile.equals("")) { name = activeFile; }
		
		// Get the specified path and filename, or use defaults if no values were specified
		// If the editor is active and a file has already been saved, the default will be the last saved name and path
		// Active path and name can be reset by resetting or restarting the editor 
		path = cmd.getNamedArgString("path", path);
		name = cmd.getNamedArgString("name", name);
		
		// Check for library save command
		boolean saveToLibrary = cmd.getNamedArgBool("library", false);
		
		// Save the chunk
		this.save(path, name, saveToLibrary);
	}
	
	/**
	 * Command: Exits
	 * 
	 * Parent command for getting, setting, and removing exit data for the current chunk.
	 *
	 * @param p the p
	 * @param args the args
	 */
	public void cmdExits(DCommandEvent cmd)
	{
		// Handle reset first: remove all exits (N,NNE,ENE,E,ESE,SSE,S,SSW,WSW,W,WNW,NNW,U,D)
		if(cmd.getNamedArgBool("reset", false) == true)
		{
			editor.sendMessage("Resetting doorways.");
			room.resetDoorways();
		}
		
		// Handle delete: remove specific exits
		String[] doorways = cmd.getNamedArgStringList("delete", null);
		if(doorways != null)
		{
			StringBuffer removedDoorways = new StringBuffer();
			
			// Loop through the list and remove the specified doorways
			for(String value : doorways)
			{
				// Validate the doorway name
				byte doorway = Direction.getDirectionFromString(value);
				
				if(doorway != -1)
				{
					removedDoorways.append(value);
					removedDoorways.append(",");
					room.setDoorway(doorway, false);
				}
			}
			
			removedDoorways.setLength(Math.max(removedDoorways.length()-1, 0));
			
			editor.sendMessage("Removed doorways: "+removedDoorways.toString());
		}
		
		// Handle add
		doorways = cmd.getNamedArgStringList("add", null);
		if(doorways != null)
		{
			StringBuffer addedDoorways = new StringBuffer();
			
			// Loop through the list and add the specified doorways
			for(String value : doorways)
			{
				// Validate the doorway name
				byte doorway = Direction.getDirectionFromString(value);
				
				if(doorway != -1)
				{
					addedDoorways.append(value);
					addedDoorways.append(",");
					room.setDoorway(doorway, true);
				}
			}
			
			addedDoorways.setLength(Math.max(addedDoorways.length()-1,0));
			
			editor.sendMessage("Added doorways: "+addedDoorways.toString());
		}
		
		// List exits after everything else, so the user knows exactly what exits are active
		Vector<DungeonRoomDoorway> finalDoorways = room.getDoorways();
		StringBuffer sb = new StringBuffer("Doorways: ");
		
		for(DungeonRoomDoorway doorway : finalDoorways)
		{
			sb.append(Direction.getDirectionName(doorway.getDirection()));
			sb.append(",");
		}
		sb.setLength(Math.max(sb.length()-1, 0));
		
		editor.sendMessage(sb.toString());
	}
	
	/**
	 * Manage theme setting and previewing for the room
	 * 
	 * @param cmd
	 */
	public void cmdTheme(DCommandEvent cmd)
	{
		// Handle set theme list
		String[] themes = cmd.getNamedArgStringList("set", null);
		if(themes != null)
		{
			// Set theme list
			room.resetThemes();
			for(String s : themes)
			{
				room.addTheme(s);
			}
		}
		
		// Handle add themes
		themes = cmd.getNamedArgStringList("add", null);
		if(themes != null)
		{
			// Add themes to list
			for(String s : themes)
			{
				room.addTheme(s);
			}
		}
		
		// Handle remove themes
		themes = cmd.getNamedArgStringList("remove", null);
		if(themes != null)
		{
			// Remove themes from list
			for(String s : themes)
			{
				room.removeTheme(s);
			}
		}
		
		// Handle set default theme
		String defaultTheme = cmd.getNamedArgString("default", null);
		if(themes != null)
		{
			// Set default theme
			room.setDefaultTheme(defaultTheme);
		}
		
		// Handle preview
		String newThemeName = cmd.getNamedArgString("preview", null);
		if(newThemeName != null)
		{
			// Make sure the theme exists before we proceed
			if(!themeManager.themeExists(newThemeName))
			{
				editor.sendMessage("The specified theme does not exist.");
				return;
			}
			
			// Don't waste time converting to the currently active theme
			if(newThemeName.equals(activeTheme))
			{
				editor.sendMessage("Theme is already active.");
				return;
			}
			
			// Notify the editor
			editor.sendMessage("Converting room from theme '"+activeTheme+"' to '"+newThemeName);
			
			// Convert blocks
			Block b = null;
			ThemeMaterialTranslation mt = null;
			int i = 0;
			for(int x = 0; x < 16; x++)
			{
				for(int z = 0; z < 16; z++)
				{
					for(int y = 0; y < 8; y++)
					{
						// Get the block handle
						b = this.chunk.getHandle().getBlock(x, y+this.editor_y, z);
						// Get the new material, if any
						mt = themeManager.getFullTranslation(activeTheme, newThemeName, new ThemeMaterialTranslation((byte) b.getTypeId(), b.getData()));
						if(mt != null && mt.type > 0)
						{
							// Set block type and basic data value
							b.setTypeIdAndData(mt.type, (byte)mt.sub, false);
							i++;
						}
					}
				}
			}
			
			// Set active theme
			activeTheme = newThemeName;
			
			editor.sendMessage("Material translation complete, "+i+" blocks converted.");
		}
		
		// List the final themes
		editor.sendMessage("Allowed Themes: "+room.getThemeCSV());
	}
	
	/**
	 * Flatten the specified chunk.
	 *
	 * @param c the chunk to flatten
	 */
	public void flattenChunk(Chunk c, Material floorMaterial)
	{
		byte[] blocks = new byte[32768];
		
		Arrays.fill(blocks, (byte)0);
		
		int pos = 0;
		for(int x = 0; x < 16; x++)
		{
			for(int z = 0; z < 16; z++)
			{
				// Add layer below floor
				pos = (x & 0xF) << 11 | (z & 0xF) << 7 | (this.editor_y-1 & 0x7F);
				blocks[pos] = 7;
				// Add floor layer
				pos = (x & 0xF) << 11 | (z & 0xF) << 7 | (this.editor_y & 0x7F);
				blocks[pos] = (byte)(floorMaterial.getId() & 0xFF);
			}
		}
		
		((CraftChunk)c).getHandle().b = blocks;
		
		// DEBUG: Try to force lighting recalc
		((CraftChunk)c).getHandle().initLighting(); // Redo SKYLIGHT
		
		c.getWorld().refreshChunk(c.getX(), c.getZ());
		for(Entity e : c.getEntities())
		{
			if(!(e instanceof Player))
			{
				e.remove();
			}
		}
	}
	
	/**
	 * Checks for unsaved changes.
	 *
	 * @return true, if there are unsaved changes
	 */
	public boolean hasUnsavedChanges()
	{
		return hasUnsavedChanges;
	}
}
