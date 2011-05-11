package com.aranai.dungeonator.generator;

import com.aranai.dungeonator.event.DCommandEvent;
import com.aranai.dungeonator.Dungeonator;
import com.aranai.dungeonator.dungeonchunk.DungeonChunk;

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
public class DungeonChunkEditor {
	
	/** Dungeonator instance */
	private Dungeonator dungeonator;
	
	/** Flag: editor is active. */
	private boolean isActive;
	
	/** Flag: editor has unsaved changes. */
	private boolean hasUnsavedChanges;
	
	/** The chunk. */
	private DungeonChunk chunk;
	
	/** The active file. */
	private String activeFile;
	
	/**
	 * Instantiates the dungeon chunk editor.
	 */
	public DungeonChunkEditor(Dungeonator d)
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
	public void start(DungeonChunk c, boolean flatten, boolean hint)
	{
		if(flatten)
		{
			/*
			 * Flatten 9 chunks total: the selected chunk and the 8 surrounding chunks
			 */
		}
		
		if(hint)
		{
			/*
			 * Add corner hints
			 */
			
			/*
			 * Add doorway hints
			 */
		}
		
		/*
		 * Mark editor as active
		 */
		
		isActive = true;
		hasUnsavedChanges = true;
	}
	
	/**
	 * Load.
	 *
	 * @param file the file
	 */
	public void load(String file)
	{
		// Load the file into a DungeonChunk
		// Start editor on loaded data
	}
	
	/**
	 * Cancel the edit operation. The DungeonChunk will be reset, no data
	 * will be saved.
	 */
	public void cancel()
	{
		// Cancel the edit operation
		
		hasUnsavedChanges = false;
		isActive = false;
	}
	
	/**
	 * Save the currently active chunk with an inferred path and name.
	 */
	public void save()
	{
	}
	
	/**
	 * Save the currently active chunk with an inferred path and
	 * specified name.
	 *
	 * @param name the name
	 */
	public void save(String name)
	{
	}
	
	/**
	 * Save the currently active chunk with an specified path and
	 * specified name.
	 *
	 * @param path the path
	 * @param name the name
	 */
	public void save(String path, String name)
	{
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
		
		this.start(new DungeonChunk(cmd.getChunk()), flatten, hint);
		
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
	 * @param p the player triggering the command
	 * @param args the args
	 */
	public void cmdLoad(DCommandEvent cmd)
	{
		
	}
	
	/**
	 * Command: Cancel Edit
	 * 
	 * This will cancel the editing process.
	 *
	 * @param p the player triggering the command
	 */
	public void cmdCancel(DCommandEvent cmd)
	{
		
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
		 * 	- ByteArrayTag("blocks"): byte[]					Raw block data
		 */
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
}
