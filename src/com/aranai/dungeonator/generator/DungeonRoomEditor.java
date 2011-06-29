package com.aranai.dungeonator.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.text.html.HTML.Tag;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftChunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jnbt.ByteArrayTag;
import org.jnbt.ByteTag;
import org.jnbt.CompoundTag;
import org.jnbt.NBTInputStream;
import org.jnbt.NBTOutputStream;

import com.aranai.dungeonator.event.DCommandEvent;
import com.aranai.dungeonator.Dungeonator;
import com.aranai.dungeonator.dungeonchunk.DungeonChunk;
import com.aranai.dungeonator.dungeonchunk.DungeonRoom;

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
	
	/** The active file. */
	private String activeFile;
	
	/** The editing player. */
	private Player editor;
	
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
		
		this.room = new DungeonRoom(this.chunk);
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
			
			// Add blocks to chunk
			for(int x = 0; x < 16; x++)
			{
				for(int z = 0; z < 16; z++)
				{
					for(int y = 0; y < 8; y++)
					{
						this.chunk.getHandle().getBlock(x, y+8, z).setTypeId(blocks[DungeonMath.getRoomPosFromCoords(x, y, z)]);
					}
				}
			}
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
			
			
		}
	}
	
	/**
	 * Save the currently active room with an specified path and
	 * specified name.
	 *
	 * @param path the path
	 * @param name the name
	 */
	public void save(String path, String name)
	{
		// Notify the editor that we are saving the file
		editor.sendMessage("Saving to: " + path + name);
		
		/*
		 * Example NBT Format:
		 * 
		 * CompoundTag("DungeonChunkSchematic"):
		 *  - ByteTag("type"): byte								Type ID for the DungeonChunk
		 * 	- ByteTag("exits"): byte							Exits from the chunk
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
		
		HashMap<String,org.jnbt.Tag> tags = new HashMap<String,org.jnbt.Tag>();
		
		// Room type ID
		tags.put("type", new ByteTag("type", (byte) 0));
		
		// Room exits
		tags.put("exits", new ByteTag("exits", (byte) 0));
		
		// Blocks
		ByteArrayTag blocks = new ByteArrayTag("blocks", this.room.getRawBlocks());
		tags.put("blocks", blocks);
		
		CompoundTag schematic = new CompoundTag("DungeonRoomSchematic", tags);
		
		try {
			// Create file output stream
			OutputStream output = new FileOutputStream(path+name);
			
			// Create NBT output stream
			NBTOutputStream os = new NBTOutputStream(output);
			
			// Write the room to the file
			os.writeTag(schematic);
			
			// Close the NBT output stream
			os.close();
			
			// Close the file output stream
			output.flush();
			output.close();
		} catch (IOException e) { e.printStackTrace(); }
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
		else if(cmd.getCmd().equals("exits"))
		{
			this.cmdExits(cmd);
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
		// Get the path and filename, or use defaults if no values were specified
		String path = cmd.getNamedArgString("path", Dungeonator.TileFolderPath);
		String name = cmd.getNamedArgString("name", "Unnamed-"+System.currentTimeMillis())+".nbt";
		
		// Save the chunk
		this.save(path, name);
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
		// Get, set, or remove an exit
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
		((CraftChunk)c).getHandle().b(); // Redo SKYLIGHT
		
		c.getWorld().refreshChunk(c.getX(), c.getZ());
		for(Entity e : c.getEntities())
		{
			if(!(e instanceof Player))
			{
				e.remove();
			}
		}
	}
}
