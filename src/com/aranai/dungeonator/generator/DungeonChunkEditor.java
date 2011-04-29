package com.aranai.dungeonator.generator;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.aranai.dungeonator.DungeonChunk;

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
	
	/** The is active. */
	private boolean isActive;
	
	/** The has unsaved changes. */
	private boolean hasUnsavedChanges;
	
	/** The chunk. */
	private DungeonChunk chunk;
	
	/** The active file. */
	private String activeFile;
	
	/**
	 * Instantiates the dungeon chunk editor.
	 */
	public DungeonChunkEditor()
	{
		chunk = null;
		isActive = false;
		hasUnsavedChanges = false;
		activeFile = "";
	}
	
	/**
	 * Starts the editing operation. The specified chunk and adjacent chunks
	 * will be flattened. Doorway and border hints will be generated.
	 */
	public void start(DungeonChunk c)
	{
		chunk = c;
		
		// Flatten current chunk and adjacent chunks
		// Set up adjacent chunks (borders and doorway hinting)
		// Fill in current chunk if existing data is provided
		
		isActive = true;
	}
	
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
	public void onCommand(Player p, String command, String[] args)
	{
		if(command.equalsIgnoreCase("new"))
		{
			this.cmdNew(args);
		}
		else if(command.equalsIgnoreCase("load"))
		{
			this.cmdLoad(args);
		}
		else if(command.equalsIgnoreCase("cancel"))
		{
			this.cmdCancel();
		}
		else if(command.equalsIgnoreCase("save"))
		{
			this.cmdSave(args);
		}
		else if(command.equalsIgnoreCase("exits"))
		{
			this.cmdExits(args);
		}
	}
	
	/**
	 * Command: New DungeonChunk
	 * 
	 * This will activate the editor with a blank chunk.
	 *
	 * @param args the args
	 */
	public void cmdNew(String[] args)
	{
		
	}
	
	/**
	 * Command: Load DungeonChunk
	 * 
	 * This will activate the editor with a specified chunk loaded.
	 *
	 * @param args the args
	 */
	public void cmdLoad(String[] args)
	{
		
	}
	
	/**
	 * Command: Cancel Edit
	 * 
	 * This will cancel the editing process.
	 */
	public void cmdCancel()
	{
		
	}
	
	/**
	 * Command: Save DungeonChunk
	 * 
	 * This will save the current DungeonChunk. The editor will remain active.
	 *
	 * @param args the args
	 */
	public void cmdSave(String[] args)
	{
		
	}
	
	/**
	 * Command: Exits
	 * 
	 * Parent command for getting, setting, and removing exit data for the current chunk.
	 *
	 * @param args the args
	 */
	public void cmdExits(String[] args)
	{
		// Get, set, or remove an exit
	}
}
