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
import java.util.logging.Level;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Furnace;
import org.bukkit.craftbukkit.CraftChunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockVector;
import org.bukkit.block.Sign;
import org.jnbt.ByteArrayTag;
import org.jnbt.ByteTag;
import org.jnbt.CompoundTag;
import org.jnbt.IntTag;
import org.jnbt.ListTag;
import org.jnbt.LongTag;
import org.jnbt.NBTInputStream;
import org.jnbt.NBTOutputStream;
import org.jnbt.StringTag;
import org.jnbt.Tag;

import com.aranai.dungeonator.event.DCommandEvent;
import com.aranai.dungeonator.Direction;
import com.aranai.dungeonator.Dungeonator;
import com.aranai.dungeonator.amt.ThemeManager;
import com.aranai.dungeonator.amt.ThemeMaterialTranslation;
import com.aranai.dungeonator.datastore.DataStoreAssetException;
import com.aranai.dungeonator.dungeonchunk.DungeonChunk;
import com.aranai.dungeonator.dungeonchunk.DungeonRoom;
import com.aranai.dungeonator.dungeonchunk.DungeonRoomDoorway;
import com.aranai.dungeonator.dungeonchunk.DungeonRoomSet;
import com.aranai.dungeonator.dungeonchunk.DungeonWidget;
import com.aranai.dungeonator.dungeonchunk.DungeonWidget.Size;
import com.aranai.dungeonator.dungeonchunk.DungeonWidgetNode;
import com.aranai.dungeonator.dungeonchunk.DungeonWidgetNode.AttachmentFace;

/**
 * Handles in-game editing of resources. Supports manual construction and
 * cuboid operations. The editing operation is destructive, and should be
 * performed on test maps or an editor-specific map only. The editor
 * automatically generates doorway hints and chunk boundaries to assist in
 * the editing process.
 * 
 * Meta data tagging commands are provided for specifying doorways, internal
 * pathing, widget regions, and so forth.
 */
public class DungeonEditor {
	
	/** Edit Modes */
	private static enum EditMode {
		ROOM, ROOM_SET, WIDGET
	};
	
	/** Dungeonator instance */
	private Dungeonator dungeonator;
	
	/** Flag: editor is active. */
	private boolean isActive;
	
	/** The active edit mode */
	private EditMode mode = EditMode.ROOM;
	
	/** Flag: editor has unsaved changes. */
	private boolean hasUnsavedChanges;
	
	/** The origin chunk. */
	private DungeonChunk chunk;
	
	/** The full chunk list. */
	private DungeonChunk[][] chunks;
	
	/** The origin room. */
	private DungeonRoom room;
	
	/** The active room set */
	private DungeonRoomSet roomSet;
	
	/** The full room list. */
	private DungeonRoom[][][] rooms;
	
	/** The active widget */
	private DungeonWidget widget;
	
	/** The active path. */
	private String activePath;
	
	/** The active file. */
	private String activeFile;
	
	/** The active widget path. */
	private String activeWidgetPath;
	
	/** The active widget file. */
	private String activeWidgetFile;
	
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
	
	/** The set name */
	private String setName = "";
	
	/**
	 * Instantiates the dungeon chunk editor.
	 */
	public DungeonEditor(Dungeonator d)
	{
		dungeonator = d;
		chunk = null;
		isActive = false;
		hasUnsavedChanges = false;
		activeFile = "";
		activePath = "";
		activeWidgetFile = "";
		activeWidgetPath = Dungeonator.WidgetFolderPath;
		activeTheme = "DEFAULT";
		testLibrary = new Vector<String>();
		themeManager = d.getThemeManager();
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
		
		if(cmd.getCmd().equals("mode"))
		{
			this.cmdMode(cmd);
		}
		else if(cmd.getCmd().equals("new"))
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
		else if(cmd.getCmd().equals("widget"))
		{
			this.cmdWidget(cmd);
		}
		else if(cmd.getCmd().equals("node"))
		{
			this.cmdNode(cmd);
		}
	}
	
	/**
	 * Command: Set/Check Edit Mode
	 * 
	 * This will set and/or display the edit mode.
	 */
	public void cmdMode(DCommandEvent cmd)
	{
		/*
		 * Get 'set' arg to change the edit mode
		 */
		String newMode = cmd.getNamedArgString("set", null);
		
		if(newMode != null)
		{
			if(newMode.equalsIgnoreCase("room"))
			{
				mode = EditMode.ROOM;
			}
			else if(newMode.equalsIgnoreCase("room_set"))
			{
				mode = EditMode.ROOM_SET;
			}
			else if(newMode.equalsIgnoreCase("widget"))
			{
				mode = EditMode.WIDGET;
			}
		}
		
		/*
		 * Print the active edit mode
		 */
		
		editor.sendMessage("Edit Mode: "+mode);
	}
	
	/**
	 * Command: New DungeonChunk
	 * 
	 * This will activate the editor with a blank chunk.
	 *
	 * @param cmd the command
	 */
	public void cmdNew(DCommandEvent cmd)
	{
		/*
		 * Get 'flatten' and 'hint' args
		 */
		
		boolean flatten = cmd.getNamedArgBool("flatten", true);
		boolean hint = cmd.getNamedArgBool("hint", true);
		
		/*
		 * Get 'set' arg
		 */
		
		boolean setActive = cmd.getNamedArgBool("set",  false);
		
		/*
		 * Get set dimensions and name
		 */
		
		int setX = 1,setY = 1,setZ = 1;
		String setTitle = "";
		
		if(setActive)
		{
			// Change the edit mode
			setMode(EditMode.ROOM_SET);
			
			setName = cmd.getNamedArgString("setname", "Unnamed-Room-Set-"+System.currentTimeMillis());
			setTitle = cmd.getNamedArgString("settitle", "Unnamed Room Set");
			setX = Math.max(1, cmd.getNamedArgInt("setx", 1));
			setY = Math.max(1, cmd.getNamedArgInt("sety", 1));
			setZ = Math.max(1, cmd.getNamedArgInt("setz", 1));
		}
		else
		{
			setMode(EditMode.ROOM);
		}
		
		// Create a room set whether or not one is active; it will be used for the dimensions of the editor regardless
		roomSet = new DungeonRoomSet(setName, setTitle, setX, setY, setZ);
		
		/*
		 * Start the editor
		 */
		
		int playerY = cmd.getPlayer().getLocation().getBlockY();
		int roundedY = playerY - (playerY % 8); // Round the player's y coordinate to the nearest multiple of 8
		this.editor_y = Math.max(Math.min(roundedY, 112-(setY*8)), 8); // Force the floor to be placed no lower than 8 and no higher than (112-(setY*8))
		
		// Set the editing chunks
		this.chunks = new DungeonChunk[roomSet.getSizeX()][roomSet.getSizeZ()];
		for(int x = 0; x < roomSet.getSizeX(); x++)
		{
			for(int z = 0; z < roomSet.getSizeZ(); z++)
			{
				this.chunks[x][z] = new DungeonChunk(this.editor.getWorld().getChunkAt(cmd.getChunk().getX()+x, cmd.getChunk().getZ()+z));
			}
		}
		
		// Set the origin chunk (lowest X,Y,Z)
		this.chunk = this.chunks[0][0];
		
		// Flatten and hint the editing region
		this.start(flatten, hint, (mode == EditMode.ROOM_SET));
		
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
		String name = cmd.getNamedArgString("name", "");
		
		// Get the room set flag
		boolean loadSet = cmd.getNamedArgBool("set",  false);
		
		if(loadSet) { setMode(EditMode.ROOM_SET); } else { setMode(EditMode.ROOM); }
		
		if(name.isEmpty())
		{
			cmd.getPlayer().sendMessage("No file specified.");
		}
		else
		{
			// Set the chunk
			chunk = new DungeonChunk(cmd.getChunk());
			
			// Load the room
			this.loadRoom(path, name, loadSet);
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
		if(!activePath.equals("") && mode != EditMode.ROOM_SET) { path = activePath; }
		if(!activeFile.equals("")) { name = activeFile; }
		
		// Get the specified path and filename, or use defaults if no values were specified
		// If the editor is active and a file has already been saved, the default will be the last saved name and path
		// Active path and name can be reset by resetting or restarting the editor 
		path = cmd.getNamedArgString("path", path);
		name = cmd.getNamedArgString("name", name);
		
		// If a set is active, alter the path
		// Format: /tiles/sets/{name}/
		if(mode == EditMode.ROOM_SET)
		{
			path = path + "sets" + File.separator + name + File.separator;
			roomSet.setName(name);
		}
		
		// Check for library save command
		boolean saveToLibrary = cmd.getNamedArgBool("library", false);
		
		// Save the chunk
		this.saveRoom(path, name, saveToLibrary);
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
		DungeonRoom room = this.getRoomFromCommand(cmd);
		
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
		DungeonRoom room = this.getRoomFromCommand(cmd);
		
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
						b = room.getDungeonChunk().getHandle().getBlock(x, y+this.editor_y, z);
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
	 * Manage loading, saving, and editing of widgets
	 *
	 * @param cmd
	 */
	public void cmdWidget(DCommandEvent cmd)
	{
		String widgetCmd = cmd.getArg(1);
		
		// Check command
		if(widgetCmd == null)
		{
			editor.sendMessage("No widget command specified.");
			return;
		}
		
		widgetCmd = widgetCmd.toLowerCase();
		
		if(widgetCmd.equals("new"))
		{
			// New widget
			cmdWidgetNew(cmd);
		}
		else if(widgetCmd.equals("load"))
		{
			// Load widget
			cmdWidgetLoad(cmd);
		}
		else if(widgetCmd.equals("save"))
		{
			// Save widget
			cmdWidgetSave(cmd);
		}
		else if(widgetCmd.equals("edit"))
		{
			// Set bounds, size class, origin
			cmdWidgetEdit(cmd);
		}
		else if(widgetCmd.equals("themes"))
		{
			// Set themes
			cmdWidgetTheme(cmd);
		}
		else if(widgetCmd.equals("move"))
		{
			// Move widget
			cmdWidgetMove(cmd);
		}
	}
	
	public void cmdWidgetNew(DCommandEvent cmd)
	{
		// TODO: Get node ID (optional - use current position if unavailable)
		
		// Reset widget path
		activeWidgetPath = Dungeonator.WidgetFolderPath;
		
		// Get size class (optional)
		Size size = Size.GetByName(cmd.getNamedArgString("size", Size.TINY.getName()));
		if(size == null)
		{
			editor.sendMessage("Invalid widget size class. Valid sizes are: tiny, small, medium, large, huge.");
			return;
		}
		
		// Get position (optional)
		BlockVector position = cmd.getNamedArgVectorInt("pos", editor.getLocation().toVector().toBlockVector());
		
		// Get attachment face (optional)
		AttachmentFace face = AttachmentFace.GetFaceByName(cmd.getNamedArgString("face", "up"));
		if(face == null)
		{
			editor.sendMessage("Invalid attachment face. Valid faces are: up, down, north, east, south, west.");
			return;
		}
		
		// Hints
		boolean hint = cmd.getNamedArgBool("hint", true);
		
		// Clear
		boolean clear = cmd.getNamedArgBool("clear", true);
		
		Material hintMat = Material.OBSIDIAN;
		Material mat = Material.AIR;
		
		if(clear)
		{
			for(int x = -1; x <= size.bound(); x++)
			{
				for(int y = -1; y <= size.bound(); y++)
				{
					for(int z = -1; z <= size.bound(); z++)
					{
						mat = Material.AIR;
						
						if(hint)
						{
							if(
								(x == -1 && z == -1 && y == -1)
								|| (x == size.bound() && z == -1 && y == -1)
								|| (x == -1 && z == size.bound() && y == -1)
								|| (x == size.bound() && z == size.bound() && y == -1)
								|| (x == -1 && z == -1 && y == size.bound())
								|| (x == size.bound() && z == -1 && y == size.bound())
								|| (x == -1 && z == size.bound() && y == size.bound())
								|| (x == size.bound() && z == size.bound() && y == size.bound())
								|| (y == -1 && x > -1 && x < size.bound() && z > -1 && z < size.bound())
							)
							{
								mat = hintMat;
							}
						}
						
						cmd.getChunk().getBlock(
								position.getBlockX()+x, position.getBlockY()+y, position.getBlockZ()+z
						).setType(mat);
					}
				}
			}
		}
		
		// Create widget
		widget = new DungeonWidget(size);
		widget.setLocation(new DungeonRoom(new DungeonChunk(cmd.getChunk()), 1), position);
		
		// Set edit mode
		setMode(EditMode.WIDGET);
	}
	
	public void cmdWidgetLoad(DCommandEvent cmd)
	{
		// TODO: allow load into active room node
		
		DungeonWidget tmp = new DungeonWidget();
		
		// Args: position (optional)
		BlockVector position = cmd.getNamedArgVectorInt("pos", editor.getLocation().toVector().toBlockVector());
		tmp.setLocation(null, position);
		
		// Args: path(optional), name (optional)
		String path = cmd.getNamedArgString("path", activeWidgetPath);
		String name = cmd.getNamedArgString("name", activeWidgetFile);
		
		// Load asset
		try {
			tmp.loadAsset(path, name);
			widget = tmp;
		} catch (DataStoreAssetException e) {
			editor.sendMessage("Could not load widget from "+e.getLocation()+". Error: "+e.getReason());
			e.printStackTrace();
			return;
		}
		
		// Add blocks to world
		byte[] blocks = widget.getRawBlocks();
		byte[] blockData = widget.getRawBlockData();
		
		Size size = widget.getSize();
		int bound = size.bound();
		int pos = 0;
		BlockVector location = widget.getPosition();
		
		for(int x = 0; x < bound; x++)
		{
			for(int y = 0; y < bound; y++)
			{
				for(int z = 0; z < bound; z++)
				{
					pos = DungeonMath.getWidgetPosFromCoords(x, y, z, size);
					cmd.getChunk().getBlock(x+location.getBlockX(), y+location.getBlockY(), z+location.getBlockZ()).setTypeIdAndData(blocks[pos], blockData[pos], true);
					Dungeonator.getLogger().info("Added block of ID "+blocks[pos]+" to "+(x+location.getBlockX())+","+(y+location.getBlockY())+","+(z+location.getBlockZ()));
				}
			}
		}
		
		// TODO: Tile entities
		
		// Set widget room
		widget.setRoom(new DungeonRoom(new DungeonChunk(cmd.getChunk()), 1));
		
		editor.sendMessage("Loaded widget from "+path+File.separator+name+" at "+location+".");
	}
	
	public void cmdWidgetSave(DCommandEvent cmd)
	{
		// Args: path(optional), name (optional)
		String path = cmd.getNamedArgString("path", activeWidgetPath);
		String name = cmd.getNamedArgString("name", activeWidgetFile);
		
		// Save asset
		try {
			widget.saveAsset(path, name);
			editor.sendMessage("Saved widget to "+path+File.separator+name+".");
		} catch (DataStoreAssetException e) {
			editor.sendMessage("Could not save widget to "+e.getLocation()+". Error: "+e.getReason());
			e.printStackTrace();
		}
	}
	
	public void cmdWidgetEdit(DCommandEvent cmd)
	{
		// TODO
	}
	
	public void cmdWidgetTheme(DCommandEvent cmd)
	{
		// TODO
	}
	
	public void cmdWidgetMove(DCommandEvent cmd)
	{
		// TODO
	}
	
	/**
	 * Commands for widget node management.
	 *
	 * @param cmd the cmd
	 */
	public void cmdNode(DCommandEvent cmd)
	{
		String nodeCmd = cmd.getArg(1);
		
		// Check command
		if(nodeCmd == null)
		{
			editor.sendMessage("No node command specified.");
			return;
		}
		
		nodeCmd = nodeCmd.toLowerCase();
		
		if(nodeCmd.equals("add"))
		{
			// Add node in room
			cmdWidgetNodeAdd(cmd);
		}
		else if(nodeCmd.equals("edit") || nodeCmd.equals("move"))
		{
			// Edit/move node in room
			cmdWidgetNodeEdit(cmd);
		}
		else if(nodeCmd.equals("autotarget"))
		{
			// Automatically position and face a widget node based on where the editor is pointing
			cmdWidgetNodeAutoTarget(cmd);
		}
		else if(nodeCmd.equals("delete"))
		{
			// Delete node in room
			cmdWidgetNodeDelete(cmd);
		}
		else if(nodeCmd.equals("info"))
		{
			// Get info on node(s)
			cmdWidgetNodeInfo(cmd);
		}
	}
	
	public void cmdWidgetNodeAdd(DCommandEvent cmd)
	{
		// Get size class (optional)
		Size sizeClass = Size.GetByName(cmd.getNamedArgString("size", Size.TINY.getName()));
		if(sizeClass == null)
		{
			editor.sendMessage("Invalid widget size class. Valid sizes are: tiny, small, medium, large, huge.");
			return;
		}
		
		// Get position (required)
		BlockVector position = cmd.getNamedArgVectorInt("pos", null);
		if(position == null || position.getBlockX() < 0 || position.getBlockY() < 0 || position.getBlockZ() < 0)
		{
			editor.sendMessage("Invalid or missing widget position.");
			return;
		}
		
		// TODO: check bounds against room/set bounds
		
		// Get attachment face (optional)
		AttachmentFace face = AttachmentFace.GetFaceByName(cmd.getNamedArgString("face", "up"));
		if(face == null)
		{
			editor.sendMessage("Invalid attachment face. Valid faces are: up, down, north, east, south, west.");
			return;
		}
		
		// Create new widget node
		DungeonRoom room = this.getRoomFromCommand(cmd);
		DungeonWidgetNode node = new DungeonWidgetNode(sizeClass, position, face);
		room.addNode(node);
		
		editor.sendMessage("Added "+node.getSize().getName()+" widget node to room "+room+" at "+position+" with node ID "+node.getNodeID());
	}
	
	public void cmdWidgetNodeEdit(DCommandEvent cmd)
	{
		DungeonRoom room = this.getRoomFromCommand(cmd);
		
		// Get node ID
		int id = cmd.getNamedArgInt("id", -1);
		if(id < 0 || id >= room.getNodes().size())
		{
			editor.sendMessage("Invalid or missing node ID. Specify ID with id:X. This room has "+room.getNodes().size()+" nodes.");
			return;
		}
		
		DungeonWidgetNode node = room.getNode(id);
		
		// Get size class (optional)
		Size sizeClass = Size.GetByName(cmd.getNamedArgString("size", Size.TINY.getName()));
		if(sizeClass == null)
		{
			editor.sendMessage("Invalid widget size class. Valid sizes are: tiny, small, medium, large, huge.");
			return;
		}
		
		// Get position (optional)
		BlockVector position = cmd.getNamedArgVectorInt("pos", node.getPosition());
		if(position.getBlockX() < 0 || position.getBlockY() < 0 || position.getBlockZ() < 0)
		{
			editor.sendMessage("Invalid node position.");
			return;
		}
		
		// TODO: check bounds against room/set bounds
		
		// Get attachment face (optional)
		AttachmentFace face = AttachmentFace.GetFaceByName(cmd.getNamedArgString("face", "up"));
		if(face == null)
		{
			editor.sendMessage("Invalid attachment face. Valid faces are: up, down, north, east, south, west.");
			return;
		}
		
		// Set new information
		node.setPosition(position);
		node.setSize(sizeClass);
		node.setAttachmentFace(face);
		
		editor.sendMessage("Updated node "+id+".");
	}
	
	public void cmdWidgetNodeAutoTarget(DCommandEvent cmd)
	{
		// Get node ID (required)
		int id = cmd.getNamedArgInt("id", -1);
		if(id < 0 || id >= room.getNodes().size())
		{
			editor.sendMessage("Invalid or missing node ID. Specify ID with id:X. This room has "+room.getNodes().size()+" nodes.");
			return;
		}
		
		DungeonWidgetNode node = room.getNode(id);
				
		// Get targeted block
		Block block = editor.getTargetBlock(null, 100);
		
		// TODO: ensure that block is within room/set bounds
		
		// Update node
		BlockVector pos = block.getLocation().toVector().toBlockVector();
		
		pos.setX(pos.getBlockX() - room.getX()*16);
		pos.setY(pos.getBlockY() - room.getY()*8);
		pos.setZ(pos.getBlockZ() - room.getZ()*16);
		
		node.setPosition(pos);
		
		editor.sendMessage("Updated node "+id+" position to "+pos);
	}
	
	public void cmdWidgetNodeDelete(DCommandEvent cmd)
	{
		DungeonRoom room = this.getRoomFromCommand(cmd);
		
		// Get node ID
		int id = cmd.getNamedArgInt("id", -1);
		if(id < 0 || id >= room.getNodes().size())
		{
			editor.sendMessage("Invalid or missing node ID. Specify ID with id:X. This room has "+room.getNodes().size()+" nodes.");
			return;
		}
		
		// Remove node
		room.removeNode(id);
		
		editor.sendMessage("Removed node.");
	}
	
	public void cmdWidgetNodeInfo(DCommandEvent cmd)
	{
		// Get node ID
		int id = cmd.getNamedArgInt("id", -1);
		if(id >= room.getNodes().size())
		{
			editor.sendMessage("Invalid node ID. This room has "+room.getNodes().size()+" nodes.");
			return;
		}
		
		for(DungeonWidgetNode n : this.getRoomFromCommand(cmd).getNodes())
		{
			if(id <= 0 || n.getNodeID() == id)
			{
				editor.sendMessage("Node "+n.getNodeID()+" ["+n.getSize().getName()+"] "+n.getPosition());
			}
		}
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
	public void start(boolean flatten, boolean hint, boolean set)
	{
		if(flatten)
		{
			/*
			 * Flatten (setX+2)*setY*(setZ+2) chunks total
			 */
			
			editor.sendMessage("Flattening chunks...");
			
			Material mat;
			
			// Flatten all chunks in the set region
			// By default this will be one chunk because no set is specified
			for(int fX = chunk.getX()-1; fX <= chunk.getX()+roomSet.getSizeX()+1; fX++)
			{
				for(int fZ = chunk.getZ()-1; fZ <= chunk.getZ()+roomSet.getSizeZ()+1; fZ++)
				{
					// Set the floor material based on whether the chunk is inside the set or is a border chunk
					if(fX >= chunk.getX() && fX <= chunk.getX()+roomSet.getSizeX() && fZ >= chunk.getZ() && fZ <= chunk.getZ()+roomSet.getSizeZ())
					{
						// Part of set
						mat = Material.COBBLESTONE;
					}
					else
					{
						// Border chunk
						mat = Material.STONE;
					}
					
					this.flattenChunk(chunk.getWorld().getChunkAt(fX, fZ), mat);
				}
			}
			
			/*
			 * Teleport player to avoid killing them
			 */
			
			editor.teleport(new Location(editor.getWorld(), editor.getLocation().getX(), this.editor_y+2, editor.getLocation().getZ()));
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
			
			for(int y = this.editor_y; y < (this.editor_y + (roomSet.getSizeY() * 8)); y++)
			{
				w.getBlockAt(blockX-1, y, blockZ-1).setType(Material.OBSIDIAN);
				w.getBlockAt(blockX-1, y, blockZ+(roomSet.getSizeZ()*16)).setType(Material.OBSIDIAN);
				w.getBlockAt(blockX+(roomSet.getSizeX()*16), y, blockZ+(roomSet.getSizeZ()*16)).setType(Material.OBSIDIAN);
				w.getBlockAt(blockX+(roomSet.getSizeX()*16), y, blockZ-1).setType(Material.OBSIDIAN);
			}
			
			/*
			 * Add doorway hints
			 */
			
			editor.sendMessage("Adding doorway hints...");
			
			for(int sy = 0; sy < roomSet.getSizeY(); sy++)
			{
				// Add lintels on the X axis
				for(int x = blockX; x < blockX + (roomSet.getSizeX()*16); x++)
				{
					w.getBlockAt(x, this.editor_y+4+(sy*8), blockZ-1).setType(Material.OBSIDIAN);
					w.getBlockAt(x, this.editor_y+4+(sy*8), blockZ+(roomSet.getSizeZ()*16)).setType(Material.OBSIDIAN);
				}
				
				// Add lintels on the Z axis
				for(int z = blockZ; z < blockZ + (roomSet.getSizeZ()*16); z++)
				{
					w.getBlockAt(blockX-1, this.editor_y+4+(sy*8), z).setType(Material.OBSIDIAN);
					w.getBlockAt(blockX+(roomSet.getSizeX()*16), this.editor_y+4+(sy*8), z).setType(Material.OBSIDIAN);
				}
				
				// Handle columns
				for(int sx = 0; sx < roomSet.getSizeX(); sx++)
				{
					for(int sz = 0; sz < roomSet.getSizeZ(); sz++)
					{
						// Only include rooms on the edge of the volume
						if(sx == 0 || sz == 0 || sx == (roomSet.getSizeX()-1) || sz == (roomSet.getSizeZ()-1))
						{
							int tmpStartX = blockX+(sx*16);
							int tmpStartY = this.editor_y+(sy*8);
							int tmpStartZ = blockZ+(sz*16);
							
							// Add vertical columns
							for(int y = tmpStartY; y < tmpStartY+4; y++)
							{
								// Z Min-Wall
								// Add only if sz is 0
								if(sz == 0)
								{
									w.getBlockAt(tmpStartX+0, y, tmpStartZ-1).setType(Material.OBSIDIAN);
									w.getBlockAt(tmpStartX+4, y, tmpStartZ-1).setType(Material.OBSIDIAN);
									w.getBlockAt(tmpStartX+5, y, tmpStartZ-1).setType(Material.OBSIDIAN);
									w.getBlockAt(tmpStartX+10, y, tmpStartZ-1).setType(Material.OBSIDIAN);
									w.getBlockAt(tmpStartX+11, y, tmpStartZ-1).setType(Material.OBSIDIAN);
									w.getBlockAt(tmpStartX+15, y, tmpStartZ-1).setType(Material.OBSIDIAN);
								}
								
								// Z Max-Wall
								// Add only if sz is max
								if(sz == (roomSet.getSizeZ()-1))
								{
									w.getBlockAt(tmpStartX+0, y, tmpStartZ+16).setType(Material.OBSIDIAN);
									w.getBlockAt(tmpStartX+4, y, tmpStartZ+16).setType(Material.OBSIDIAN);
									w.getBlockAt(tmpStartX+5, y, tmpStartZ+16).setType(Material.OBSIDIAN);
									w.getBlockAt(tmpStartX+10, y, tmpStartZ+16).setType(Material.OBSIDIAN);
									w.getBlockAt(tmpStartX+11, y, tmpStartZ+16).setType(Material.OBSIDIAN);
									w.getBlockAt(tmpStartX+15, y, tmpStartZ+16).setType(Material.OBSIDIAN);
								}
								
								// X Min-Wall
								// Add only if sx is 0
								if(sx == 0)
								{
									w.getBlockAt(tmpStartX-1, y, tmpStartZ+0).setType(Material.OBSIDIAN);
									w.getBlockAt(tmpStartX-1, y, tmpStartZ+4).setType(Material.OBSIDIAN);
									w.getBlockAt(tmpStartX-1, y, tmpStartZ+5).setType(Material.OBSIDIAN);
									w.getBlockAt(tmpStartX-1, y, tmpStartZ+10).setType(Material.OBSIDIAN);
									w.getBlockAt(tmpStartX-1, y, tmpStartZ+11).setType(Material.OBSIDIAN);
									w.getBlockAt(tmpStartX-1, y, tmpStartZ+15).setType(Material.OBSIDIAN);
								}
								
								// X Max-Wall
								// Add only if sx is max
								if(sx == (roomSet.getSizeX()-1))
								{
									w.getBlockAt(tmpStartX+16, y, tmpStartZ+0).setType(Material.OBSIDIAN);
									w.getBlockAt(tmpStartX+16, y, tmpStartZ+4).setType(Material.OBSIDIAN);
									w.getBlockAt(tmpStartX+16, y, tmpStartZ+5).setType(Material.OBSIDIAN);
									w.getBlockAt(tmpStartX+16, y, tmpStartZ+10).setType(Material.OBSIDIAN);
									w.getBlockAt(tmpStartX+16, y, tmpStartZ+11).setType(Material.OBSIDIAN);
									w.getBlockAt(tmpStartX+16, y, tmpStartZ+15).setType(Material.OBSIDIAN);
								}
							}
						}
					}
				}
			}
		}
		
		/*
		 * Mark editor as active
		 */
		
		isActive = true;
		hasUnsavedChanges = true;
		
		// Initialize rooms list
		this.rooms = new DungeonRoom[roomSet.getSizeX()][roomSet.getSizeY()][roomSet.getSizeZ()];
		
		for(int x = 0; x < roomSet.getSizeX(); x++)
		{
			for(int y = 0; y < roomSet.getSizeY(); y++)
			{
				for(int z = 0; z < roomSet.getSizeZ(); z++)
				{
					this.rooms[x][y][z] = new DungeonRoom(this.chunks[x][z], y+1);
				}
			}
		}
		
		// Set origin room
		this.room = this.rooms[0][0][0];
	}
	
	/**
	 * Load.
	 *
	 * @param file the file
	 */
	public void loadRoom(String path, String name, boolean isRoomSet)
	{
		CompoundTag schematic = null;
		
		// Handle the room set
		if(isRoomSet)
		{
			path = path + "sets" + File.separator + name + File.separator;
			loadRoomSet(path, name+".nbt");
		}
		
		// Append file extension
		name = name + ".nbt";
		
		// Notify the editor that we are loading the file
		editor.sendMessage("Loading: " + path + name);
		
		// If the editor is not started, start it
		if(!this.isActive || isRoomSet)
		{
			String setString = "false";
			if(isRoomSet) { setString = "true"; }
			
			// Create a blank room set if one doesn't exist yet
			if(roomSet == null) { roomSet = new DungeonRoomSet(name, name, 1, 1, 1); }
			
			this.cmdNew(new DCommandEvent(editor, "new", new String[]{
					"flatten:true", "hint:true", "set:"+setString, "setx:"+roomSet.getSizeX(), "sety:"+roomSet.getSizeY(), "setz:"+roomSet.getSizeZ()}
			));
		}
		
		// Reinitialize the room array
		rooms = new DungeonRoom[roomSet.getSizeX()][roomSet.getSizeY()][roomSet.getSizeZ()];
		
		// Loop through rooms and load each one
		for(int setX = 0; setX < roomSet.getSizeX(); setX++)
		{
			for(int setY = 0; setY < roomSet.getSizeY(); setY++)
			{
				for(int setZ = 0; setZ < roomSet.getSizeZ(); setZ++)
				{
					// Load the file
					try {
						// Open file input stream
						String tmpName = name;
						if(mode == EditMode.ROOM_SET) { tmpName = setX+"."+setY+"."+setZ+".nbt"; }
						
						FileInputStream fis = new FileInputStream(path+File.separator+tmpName);
						
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
					
					// Get temporary chunk handle
					DungeonChunk tmpChunk = new DungeonChunk(dungeonator.getServer().getWorld(chunk.getWorldName()).getChunkAt(chunk.getX()+setX, chunk.getZ()+setZ));
					
					// Initialize room
					rooms[setX][setY][setZ] = new DungeonRoom(tmpChunk, setY+1);
					DungeonRoom tmpRoom = rooms[setX][setY][setZ];
					
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
									tmpChunk.getHandle().getBlock(x, y+this.editor_y, z).setTypeIdAndData(blocks[DungeonMath.getRoomPosFromCoords(x, y, z)], (byte)0, false);
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
									tmpChunk.getHandle().getBlock(x, y+this.editor_y, z).setData(blockData[DungeonMath.getRoomPosFromCoords(x, y, z)], false);
								}
							}
						}
						
						// Handle tile entities
						if(schematic.getValue().containsKey("tileEntities"))
						{
							Map<String,org.jnbt.Tag> tileEntities = ((CompoundTag)schematic.getValue().get("tileEntities")).getValue();
							
							for(org.jnbt.Tag t : tileEntities.values())
							{
								tmpChunk.addTileEntityFromTag(t, this.editor_y);
							}
						}
						
						// Load doorways
						int exitCount = 0;
						tmpRoom.resetDoorways();
						byte[] exits = ((ByteArrayTag)schematic.getValue().get("exits")).getValue();
						
						for(byte i = 0; i < exits.length; i++)
						{
							if(exits[i] != 0)
							{
								tmpRoom.setDoorway(i, true);
								exitCount++;
							}
						}
						
						// Load themes
						StringTag themeTag = (StringTag)schematic.getValue().get("themes");
						if(themeTag != null)
						{
							tmpRoom.resetThemes();
							String themes = themeTag.getValue();
							for(String s : themes.split(","))
							{
								tmpRoom.addTheme(s);
							}
						}
						
						// Load default theme
						StringTag defaultThemeTag = (StringTag)schematic.getValue().get("defaultTheme");
						if(defaultThemeTag != null)
						{
							tmpRoom.setDefaultTheme(defaultThemeTag.getValue());
						}
						
						// Semi-debug: notify the user of how many doorways were added
						editor.sendMessage("Adding "+exitCount+" doorways.");
					}
				}
			}
			
			// Set filename and path
			activeFile = name;
			activePath = path;
		}
	}
	
	/**
	 * Load a room set from file.
	 */
	public void loadRoomSet(String path, String name)
	{
		CompoundTag schematic = null;
		
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
			CompoundTag setTag = ((CompoundTag)schematic.getValue().get("set"));
			
			// Get Title
			String title = ((StringTag)setTag.getValue().get("title")).getValue();
			
			// Get bounds
			int x = ((IntTag)setTag.getValue().get("x")).getValue();
			int y = ((IntTag)setTag.getValue().get("y")).getValue();
			int z = ((IntTag)setTag.getValue().get("z")).getValue();
			
			roomSet = new DungeonRoomSet(name, title, x, y, z, chunk.getX(), editor_y, chunk.getZ());
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
	 * Save the currently active room or room set with an specified path and
	 * specified name.
	 *
	 * @param path the path
	 * @param name the name
	 */
	public void saveRoom(String path, String name, boolean saveToLibrary)
	{
		// Make sure the editor is active
		if(!isActive && (mode == EditMode.ROOM || mode == EditMode.ROOM_SET))
		{
			editor.sendMessage("Editor is not active or mode is not set correctly. Mode is: "+mode);
			return;
		}
		
		// Notify the editor that we are saving the file
		editor.sendMessage("Saving '" + name + "' to: " + path);
		
		/*
		 * Save set file
		 */
		
		if(mode == EditMode.ROOM_SET)
		{
			try {
				this.saveRoomSet(path, name, saveToLibrary);
			} catch (IOException e) {
				// Print a stack trace and fail fast
				e.printStackTrace();
				return;
			}
		}
		
		/*
		 * Save room file(s)
		 */
		
		for(int x = 0; x < roomSet.getSizeX(); x++)
		{
			for(int y = 0; y < roomSet.getSizeY(); y++)
			{
				for(int z = 0; z < roomSet.getSizeZ(); z++)
				{
					// If this is part of a set, alter the name to match the coords
					// Format: X.Y.Z (file extension will be added automatically)
					String tmpName = name;
					
					if(mode == EditMode.ROOM_SET)
					{
						tmpName = x+"."+y+"."+z;
						rooms[x][y][z].setRoomSet(roomSet);
					}
					
					try {
						this.saveRoom(path, tmpName, rooms[x][y][z], saveToLibrary);
					} catch (IOException e) {
						// Print a stack trace and fail fast
						e.printStackTrace();
						return;
					}
				}
			}
		}
	}
	
	public void saveRoomSet(String path, String name, boolean saveToLibrary) throws IOException
	{
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
		
		// Set tags
		HashMap<String,org.jnbt.Tag> setTags = new HashMap<String,org.jnbt.Tag>();
		
		// Title
		setTags.put("title", new StringTag("title", roomSet.getTitle()));
		
		// Dimensions X,Y,Z
		setTags.put("x", new IntTag("x", roomSet.getSizeX()));
		setTags.put("y", new IntTag("y", roomSet.getSizeY()));
		setTags.put("z", new IntTag("z", roomSet.getSizeZ()));
		
		// Add set tags to schematic
		tags.put("set", new CompoundTag("set", setTags));
		
		// Build final schematic tag
		CompoundTag schematic = new CompoundTag("DungeonRoomSchematic", tags);
		
		// Create the set folder
		if (!new File(path).exists()) {
			try {
				(new File(path)).mkdirs();
			} catch (Exception e) {
				Dungeonator.getLogger().log(Level.SEVERE, "[Dungeonator]: Unable to create room set folder at: "+path);
				return;
			}
		}
		
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
		
		if(saveToLibrary)
		{
			dungeonator.getDataManager().saveLibraryRoomSet(roomSet);
		}
	}
	
	public void saveRoom(String path, String name, DungeonRoom room, boolean saveToLibrary) throws IOException
	{
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
			HashMap<String,org.jnbt.Tag> innerTags = new HashMap<String,org.jnbt.Tag>();
			
			// Add common elements
			innerTags.put("x", new IntTag("x", b.getX() & 0xF));
			innerTags.put("y", new IntTag("y", (b.getY() & 0x7)));
			innerTags.put("z", new IntTag("z", b.getZ() & 0xF));
			
			// Sign Entity
			if(b instanceof Sign)
			{
				// Set type as sign
				entityTags.put("type", new StringTag("type", "sign"));
				
				// Save text lines 1-4
				Sign s = (Sign)b;
				innerTags.put("line1", new StringTag("line1", s.getLine(0)));
				innerTags.put("line2", new StringTag("line2", s.getLine(1)));
				innerTags.put("line3", new StringTag("line3", s.getLine(2)));
				innerTags.put("line4", new StringTag("line4", s.getLine(3)));
			}
			
			// Chest Entity
			if(b instanceof Chest)
			{
				// Set type as chest
				entityTags.put("type", new StringTag("type", "chest"));
				
				// Save item stacks
				Chest c = (Chest)b;
				ItemStack[] stacks = c.getInventory().getContents();
				innerTags.put("stacks", new ListTag("stacks", CompoundTag.class, getInventoryList(stacks)));
			}
			
			// Dispenser Entity
			if(b instanceof Dispenser)
			{
				// Set type as chest
				entityTags.put("type", new StringTag("type", "dispenser"));
				
				// Save item stacks
				Dispenser c = (Dispenser)b;
				ItemStack[] stacks = c.getInventory().getContents();
				innerTags.put("stacks", new ListTag("stacks", CompoundTag.class, getInventoryList(stacks)));
			}
			
			// Furnace Entity
			if(b instanceof Furnace)
			{
				// Set type as chest
				entityTags.put("type", new StringTag("type", "furnace"));
				
				// Save item stacks
				Furnace c = (Furnace)b;
				ItemStack[] stacks = c.getInventory().getContents();
				innerTags.put("stacks", new ListTag("stacks", CompoundTag.class, getInventoryList(stacks)));
			}
			
			// Add tile entity tag to entity list tag
			entityTags.put("data", new CompoundTag("data", innerTags));
			if(entityTags.containsKey("type"))
			{
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
		byte[] blockData = room.getRawBlockData();
		byte[] blocks = room.getRawBlocks();
		
		for(String s : room.getThemes())
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
				else
				{
					tmpBlocks[i] = blocks[i];
					tmpBlockData[i] = blockData[i];
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
	
	public Vector<Tag> getInventoryList(ItemStack[] stacks)
	{
		Vector<Tag> list = new Vector<Tag>();
		
		int i = 0;
		for(ItemStack s : stacks)
		{
			if(s != null)
			{
				HashMap<String,org.jnbt.Tag> stackTags = new HashMap<String,org.jnbt.Tag>();
				
				// Item position
				stackTags.put("pos", new IntTag("pos", i));
				// Item type
				stackTags.put("type", new IntTag("type", s.getTypeId()));
				// Stack amount
				stackTags.put("amount", new IntTag("amount", s.getAmount()));
				// Damage value for item
				stackTags.put("damage", new IntTag("damage", s.getDurability()));
				// Data value for item
				stackTags.put("data", new IntTag("data", s.getData().getData()));
				
				// TODO: Enchantments
				
				CompoundTag ct = new CompoundTag("", stackTags);
				list.add(ct);
			}
			i++;
		}
		
		return list;
	}
	
	/**
	 * Get room from command.
	 *
	 * @param cmd the dungeon command
	 * @return the room specified by the coordinates given, or the origin room by default
	 */
	public DungeonRoom getRoomFromCommand(DCommandEvent cmd)
	{
		// Get room coordinate within a room set (or 0,0,0 if no room is specified)
		int[] coords = {0,0,0};
		if(mode == EditMode.ROOM_SET)
		{
			int[] tmpCoords = cmd.getNamedArgIntList("room", null);
			
			if(tmpCoords != null && tmpCoords.length == 3)
			{
				coords = tmpCoords;
			}
		}
		
		// Select the room to apply the exit command to
		return this.rooms[coords[0]][coords[1]][coords[2]];
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
	
	/**
	 * Sets the edit mode.
	 *
	 * @param mode the new edit mode
	 */
	private void setMode(EditMode mode)
	{
		if(this.mode != mode)
		{
			this.mode = mode;
			editor.sendMessage("Changed edit mode to "+mode);
		}
	}
}
