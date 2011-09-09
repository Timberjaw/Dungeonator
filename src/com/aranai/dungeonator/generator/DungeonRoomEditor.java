package com.aranai.dungeonator.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
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
	
	/** The editing player. */
	private Player editor;
	
	/** Test library */
	private Vector<String> testLibrary;
	
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
		testLibrary = new Vector<String>();
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
			this.start(true, true);
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
						this.chunk.getHandle().getBlock(x, y+8, z).setTypeIdAndData(blocks[DungeonMath.getRoomPosFromCoords(x, y, z)], (byte)0, false);
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
						this.chunk.getHandle().getBlock(x, y+8, z).setData(blockData[DungeonMath.getRoomPosFromCoords(x, y, z)], false);
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
						Block b = this.chunk.getHandle().getBlock(x, y+8, z);
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
		 * CompoundTag("DungeonChunkSchematic"):
		 *  - ByteTag("type"): byte								Type ID for the DungeonChunk
		 * 	- ByteArrayTag("exits"): byte[]						Exits from the chunk
		 * 	- CompoundTag("widgetSpawns"):						Potential widget spawn locations for the chunk
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
		
		// Blocks
		tags.put("blocks", new ByteArrayTag("blocks", this.room.getRawBlocks()));
		
		// Block data values
		tags.put("blockData", new ByteArrayTag("blockData", this.room.getRawBlockData()));
		
		CompoundTag schematic = new CompoundTag("DungeonRoomSchematic", tags);
		
		try {
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
			
			// Set active file and path
			activeFile = name;
			activePath = path;
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
		else if(cmd.getCmd().equals("teststack"))
		{
			this.cmdTestStack(cmd);
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
	
	public void cmdTestStack(DCommandEvent cmd)
	{
		// Test the active room library by generating a chunk (stack) of rooms near the player
		int x = cmd.getChunk().getX();
		int z = cmd.getChunk().getZ()+1;
		int y = 8;
		
		Chunk tmpChunk = cmd.getPlayer().getWorld().getChunkAt(x, z);
		
		this.flattenChunk(tmpChunk, Material.SAND);
		
		byte[] blocks;
		//byte[] blockData;
		
		Hashtable<String,byte[]> tmpRoomBlocks = new Hashtable<String,byte[]>();
		
		for(String s : this.testLibrary)
		{
			// Load the file
			CompoundTag schematic = null;
			try {
				// Open file input stream
				FileInputStream fis = new FileInputStream(Dungeonator.TileFolderPath+File.separator+s+".nbt");
				
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
				tmpRoomBlocks.put(s, ((ByteArrayTag)schematic.getValue().get("blocks")).getValue());
			}
		}
		
		for(int i = 0; i < 15; i++)
		{
			// Get a room from the active library
			String roomName = this.testLibrary.get(
					(int) (Math.random() * this.testLibrary.size())
			);
			cmd.getPlayer().sendMessage("Selecting room '" + roomName + "' for Y:"+y);
			
			blocks = tmpRoomBlocks.get(roomName);
			//blockData = tmpRoom.getRawBlockData();
			
			// Copy blocks to chunk
			for(int x2 = 0; x2 < 16; x2++)
			{
				for(int z2 = 0; z2 < 16; z2++)
				{
					for(int y2 = 0; y2 < 8; y2++)
					{
						tmpChunk.getBlock(x2, y2+y, z2).setTypeId(blocks[DungeonMath.getRoomPosFromCoords(x2, y2, z2)]);
					}
				}
			}
			
			// Update y coordinate
			y += 8;
		}
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
				pos = (x & 0xF) << 11 | (z & 0xF) << 7 | (7 & 0x7F);
				blocks[pos] = 7;
				pos = (x & 0xF) << 11 | (z & 0xF) << 7 | (8 & 0x7F);
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
