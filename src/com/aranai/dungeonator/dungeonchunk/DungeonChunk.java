package com.aranai.dungeonator.dungeonchunk;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Furnace;
import org.bukkit.block.Sign;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.jnbt.CompoundTag;
import org.jnbt.IntTag;
import org.jnbt.ListTag;
import org.jnbt.StringTag;
import org.jnbt.Tag;

import org.bukkit.craftbukkit.v1_6_R3.CraftWorld;

import com.aranai.dungeonator.Direction;

/**
 * Stores and manipulates Dungeonator chunk information.
 */
public class DungeonChunk {
	
	/** The world. */
	private World world;
	
	/** X coordinate for the chunk. */
	private int x;
	
	/** Z coordinate for the chunk. */
	private int z;
	
	/** The handle for the chunk data */
	private Chunk chunk;
	
	/** The chunk ready status */
	private boolean ready;

	/** The neighboring chunks */
	private DungeonChunk[] neighbors = new DungeonChunk[4];
	
	/** List of items that can be placed randomly into chests */
	static Material[] ChestContents = {
	    Material.DIRT,
	    Material.APPLE,
	    Material.BAKED_POTATO,
	    Material.BONE,
	    Material.BOOK_AND_QUILL,
	    Material.BOW,
	    Material.BREAD,
	    Material.BOWL,
	    Material.BROWN_MUSHROOM,
	    Material.BUCKET,
	    Material.CAKE,
	    Material.COAL,
	    Material.COBBLESTONE,
	    Material.EGG,
	    Material.STONE_AXE
	};
	
	/**
	 * Instantiates a new dungeon chunk from an existing chunk
	 *
	 * @param chunk the chunk
	 */
	public DungeonChunk(Chunk chunk)
	{
		this(chunk, DungeonRoomType.BASIC_TILE, (chunk != null) ? chunk.getX() : 0, (chunk != null) ? chunk.getZ() : 0);
	}
	
	public DungeonChunk(Chunk chunk, DungeonRoomType type, int x, int z)
	{
		this.ready = false;
		if(chunk != null) {
			this.world = chunk.getWorld();
			this.ready = true;
		}
		this.x = x;
		this.z = z;
		this.chunk = chunk;
	}
	
	/*
	 * Checks whether the chunk is in ready state
	 */
	
	public boolean isReady()
	{
		return ready;
	}
	
	/* 
	 * Gets the native chunk handle for the DungeonChunk
	 */
	public Chunk getHandle()
	{
		return chunk;
	}
	
	/**
	 * Gets the chunk's parent world
	 *
	 * @return the parent world
	 */
	public World getWorld()
	{
		return world;
	}
	
	/**
	 * Sets the chunk's parent world
	 */
	public void setWorld(World newWorld)
	{
		world = newWorld;
	}
	
	/**
	 * Gets the name of the chunk's parent world.
	 *
	 * @return the world name
	 */
	public String getWorldName()
	{
		return world.getName();
	}
	
	/**
	 * Gets the x coordinate for the chunk.
	 *
	 * @return the x coordinate
	 */
	public int getX()
	{
		return x;
	}
	
	/**
	 * Gets the z coordinate for the chunk.
	 *
	 * @return the z coordinate
	 */
	public int getZ()
	{
		return z;
	}
	
	/* 
	 * Sets the native chunk handle for the DungeonChunk
	 */
	public void setHandle(Chunk handle)
	{
		chunk = handle;
	}
	
	/**
	 * Checks for an existing neighbor in the specified direction.
	 *
	 * @param direction the direction
	 * @return true, if successful
	 */
	public boolean hasNeighbor(byte direction) {
		if(this.isValidChunkDirection(direction))
		{
			return (neighbors[direction] != null);
		}
		
		return false;
	}
	
	/* 
	 * Gets the neighboring chunk in the specified direction
	 */
	public DungeonChunk getNeighbor(byte direction) {
		if(this.hasNeighbor(direction))
		{
			return neighbors[direction];
		}
		
		return null;
	}

	/* 
	 * Set a neighboring chunk in the specified direction
	 */
	public void setNeighbor(byte direction, DungeonChunk neighbor) {
		if(this.isValidChunkDirection(direction))
		{
			neighbors[direction] = (DungeonChunk)neighbor;
		}
	}
	
	/**
	 * Checks if the specified direction is a valid chunk direction (NESW)
	 *
	 * @param direction the direction to check
	 * @return true, if the direction is a valid chunk direction
	 */
	public boolean isValidChunkDirection(byte direction)
	{
		return (direction == Direction.N || direction == Direction.S || direction == Direction.E || direction == Direction.W);
	}
	
	public void addTileEntityFromTag(Tag t, int editor_y)
	{
		Map<String,org.jnbt.Tag> ct = ((CompoundTag)t).getValue();
		Map<String,org.jnbt.Tag> data = ((CompoundTag)ct.get("data")).getValue();
		StringTag typeTag = (StringTag)ct.get("type");
		if(typeTag == null) { return; }
		
		String type = typeTag.getValue();
		
		// Get block
		int x = ((IntTag)data.get("x")).getValue();
		int y = ((IntTag)data.get("y")).getValue();
		int z = ((IntTag)data.get("z")).getValue();
		Block b = this.getHandle().getBlock(x, y+editor_y, z);
		if(b == null) { System.out.println("Missing block at "+(x+(this.getX()*16))+", "+(y+editor_y)+","+(z+(this.getZ()*16))+"."); return; }
		
		if(b.getTypeId() == 0)
		{
		    System.out.println("Expected "+type+" at "+x+","+(y+editor_y)+","+z+", found AIR.");
		    return;
		}
		
		if(((CraftWorld)b.getWorld()).getTileEntityAt(x+(this.getX()*16), y+editor_y, z+(this.getZ()*16)) == null)
        {
		    System.out.println("Expected tile entity "+type+" at "+(x+(this.getX()*16))+", "+(y+editor_y)+","+(z+(this.getZ()*16))+", found null. Found "+
		    		((CraftWorld)b.getWorld()).getTileEntityAt(z+(this.getZ()*16), y+editor_y, x+(this.getX()*16))+" at swapped X/Z"
		    		);
            return;
        }
		
		BlockState bs = b.getState();
		
		if(type.equalsIgnoreCase("sign"))
		{
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
		
		if(type.equalsIgnoreCase("chest") || type.equalsIgnoreCase("furnace") || type.equalsIgnoreCase("dispenser"))
		{
			// Get item stacks
			if(bs instanceof Chest || bs instanceof Furnace || bs instanceof Dispenser)
			{
				InventoryHolder s = (InventoryHolder) bs;
				List<Tag> list = ((ListTag)data.get("stacks")).getValue();
				int totalItems = 0;
				//ItemStack[] stacks = new ItemStack[list.size()];
				for(Tag e : list)
				{
					Map<String, Tag> c = ((CompoundTag)e).getValue();
					
					// Position
					int item_pos = ((IntTag)c.get("pos")).getValue();
					// Type
					int item_type = ((IntTag)c.get("type")).getValue();
					// Amount
					int item_amount = ((IntTag)c.get("amount")).getValue();
					totalItems += item_amount;
					// Damage
					int item_damage = ((IntTag)c.get("damage")).getValue();
					// Data
					int item_data = ((IntTag)c.get("data")).getValue();
					
					// Enchantments
					// TODO
					
					// Add stack
					ItemStack is = new ItemStack(item_type, item_amount, (short)item_damage);
					is.setData(new MaterialData(item_type, (byte)item_data));
					s.getInventory().setItem(item_pos, is);
				}
				
				// Add some random stuff to the container
				if(totalItems == 0)
				{
				    Random r = new Random();
				    
				    int max = 0;
				    int maxItems = 0;
				    int luck = r.nextInt(20);
				    
				    // Determine max slots to fill
				    if(luck <= 2) {
				        // Unlucky
				    } else if(luck <= 10) {
				        // Meh
				        max = (int) (s.getInventory().getSize() / 4);
				        maxItems = r.nextInt(3);
				    } else if(luck <= 18) {
				        // Lucky
				        max = (int) (s.getInventory().getSize() / 2);
				        maxItems = r.nextInt(10);
				    } else {
				        // Very lucky
				        max = s.getInventory().getSize();
				        maxItems = r.nextInt(64);
				    }
				    
				    // Actual slots to fill
				    if(max > 0 && maxItems > 0)
				    {
    				    int slots = r.nextInt(max);
    				    
    				    for(int i = 0; i < slots; i++)
    				    {
    				        Material m = DungeonChunk.ChestContents[r.nextInt(DungeonChunk.ChestContents.length)];
    				        int items = Math.min(r.nextInt(maxItems) + 1, m.getMaxStackSize());
    				        ItemStack is = new ItemStack(m, items);
    				        s.getInventory().setItem(i, is);
    				    }
				    }
				}
				
				// Add the stacks to the object's inventory
				bs.update();
			}
		}
	}
	
	/**
	 * Gets a block array index from XYZ coordinates.
	 *
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 * @return the block array index
	 */
	public static final int getIndexFromCoords(int x, int y, int z)
	{
		return (x & 0xF) << 11 | (z & 0xF) << 7 | (y & 0xFF);
	}
}