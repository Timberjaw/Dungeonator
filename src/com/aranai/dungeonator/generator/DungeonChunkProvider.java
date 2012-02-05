package com.aranai.dungeonator.generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.util.BlockVector;
import org.bukkit.util.noise.SimplexOctaveGenerator;
import org.jnbt.CompoundTag;
import org.jnbt.ListTag;
import org.jnbt.Tag;

import com.aranai.dungeonator.Dungeonator;
import com.aranai.dungeonator.datastore.DataStoreAssetException;
import com.aranai.dungeonator.dungeonchunk.DungeonChunk;
import com.aranai.dungeonator.dungeonchunk.DungeonRoom;
import com.aranai.dungeonator.dungeonchunk.DungeonRoomSet;
import com.aranai.dungeonator.dungeonchunk.DungeonRoomType;
import com.aranai.dungeonator.dungeonchunk.DungeonWidget;
import com.aranai.dungeonator.dungeonchunk.DungeonWidgetNode;

import net.minecraft.server.Block;
import net.minecraft.server.Chunk;
import net.minecraft.server.ChunkPosition;
import net.minecraft.server.EnumCreatureType;
import net.minecraft.server.IChunkProvider;
import net.minecraft.server.IProgressUpdate;
import net.minecraft.server.NibbleArray;

public class DungeonChunkProvider implements IChunkProvider {

	private World world;
	private Dungeonator dungeonator;
	
	private HashMap<String,DungeonRoom[]> roomCache = new HashMap<String,DungeonRoom[]>();
	
	@SuppressWarnings("unused")
	private static HashSet<Byte> blocksWithData;
	
	// Noise generator attributes
	private SimplexOctaveGenerator octave;
	private double amplitude;
	private double frequency;
	private int octaves;
	
	public DungeonChunkProvider(World world, long i) {
		this.world = world;
		
		// Build the list of special blocks (blocks with data values) that require special attention
		// This includes things like torches, furnaces, steps, wool; anything with an orientation or multiple states
		byte[] blocks = {6,17,18,23,25,27,28,29,31,33,34,35,43,44,50,53,54,61,62,64,65,66,67,69,71,75,76,77,84,86,90,91,92,95,96,97,98};
		ArrayList<Byte> tmpList = new ArrayList<Byte>();
		for(byte b : blocks) { tmpList.add(b); }
		DungeonChunkProvider.blocksWithData = new HashSet<Byte>(tmpList);
		
		// Set up the simplex octave generator for themes and biomes
		octaves = 10;
		amplitude = 1.0;
		frequency = 1.0;
		octave = new SimplexOctaveGenerator(world, octaves);
	}
	
	public void setInstance(Dungeonator instance)
	{
		this.dungeonator = instance;
	}
	
	public double getNoise(int x, int y, int z)
	{
		return octave.noise(x, y, z, amplitude, frequency);
	}

	/* (non-Javadoc)
	 * @see net.minecraft.server.IChunkProvider#getChunkAt(int, int)
	 */
	@Override
	public Chunk getChunkAt(int x, int y) {
		// Purpose is unclear. Passes control to another method with identical args.
		System.out.println("Call to getChunkAt("+x+","+y+")");
		return this.getOrCreateChunk(x, y);
	}

	/* (non-Javadoc)
	 * @see net.minecraft.server.IChunkProvider#getChunkAt(net.minecraft.server.IChunkProvider, int, int)
	 */
	@Override
	public void getChunkAt(IChunkProvider arg0, int arg1, int arg2) {
		/*
		 * Chunk decoration phase
		 */
		
		String hash = "stack-"+arg1+"-"+arg2;
		
		long startTime = System.currentTimeMillis();
		
		// Get active rooms for chunk
		DungeonChunk dc = new DungeonChunk(this.world.getChunkAt(arg1, arg2), DungeonRoomType.BASIC_TILE, arg1, arg2);
		
		DungeonRoom[] rooms = null;
		if(roomCache.containsKey(hash))
		{
			//System.out.println("Cache hit for "+hash);
			rooms = roomCache.get(hash);
		}
		else
		{
			//System.out.println("Not in cache for "+hash);
			rooms = dungeonator.getDataManager().getRoomsForChunk(dc);
			
			if(rooms == null)
			{
				System.out.println("No rooms for "+arg1+","+arg2);
				return;
			}
		}
		
		int pos = 0;
		for(int r = 0; r < rooms.length; r++)
		{
			byte[] blocks = rooms[r].getRawBlocks();
			byte[] data = rooms[r].getRawBlockData();
			
			// Set data values
			for(int x = 0; x < 16; x++)
			{
				for(int z = 0; z < 16; z++)
				{
					for(int y = 0; y < 8; y++)
					{
						pos = DungeonMath.getRoomPosFromCoords(x, y, z);
						
						// Torches
						if(blocks[pos] == Block.TORCH.id)
						{
							dc.getHandle().getBlock(x, y + (r * 8), z).setTypeId(0);
							dc.getHandle().getBlock(x, y + (r * 8), z).setTypeIdAndData(Block.TORCH.id, data[pos], true);
						}
					}
				}
			}
			
			// Handle tile entities
			CompoundTag schematic = rooms[r].getSchematic();
			if(schematic != null)
			{
				if(schematic.getValue().containsKey("tileEntities"))
				{
					Map<String,org.jnbt.Tag> tileEntities = ((CompoundTag)schematic.getValue().get("tileEntities")).getValue();
					
					for(org.jnbt.Tag t : tileEntities.values())
					{
						dc.addTileEntityFromTag(t, r*8);
					}
				}
			}
			else
			{
				System.out.println("Unexpected NULL schematic.");
			}
			
			// Populate widgets
			if(schematic != null && schematic.getValue().containsKey("nodes"))
			{
				// Add nodes
				List<Tag> nodes = ((ListTag)schematic.getValue().get("nodes")).getValue();
				int nodesLoaded = 0;
				
				for(Tag t : nodes)
				{
					CompoundTag ct = (CompoundTag)t;
					try { rooms[r].addNodeFromTag(ct); } catch (DataStoreAssetException e) { e.printStackTrace(); }
				}
				
				// Get random widgets
				for(DungeonWidgetNode node : rooms[r].getNodes())
				{
					DungeonWidget w = dungeonator.getDataManager().getRandomWidget(node.getSize());
					
					if(w != null)
					{
						// Add widget
						BlockVector tmpPos = node.getPosition();
						w.setPosition(tmpPos);
						byte[] tmpRawBlocks = w.getRawBlocks();
						byte[] tmpRawBlockData = w.getRawBlockData();
						
						for(int x = 0; x < w.getSize().bound(); x++)
						{
							for(int y = 0; y < w.getSize().bound(); y++)
							{
								for(int z = 0; z < w.getSize().bound(); z++)
								{
									pos = DungeonMath.getWidgetPosFromCoords(x, y, z, w.getSize());
									dc.getHandle().getBlock(
										x+tmpPos.getBlockX(),
										y+tmpPos.getBlockY()+(r*8),
										z+tmpPos.getBlockZ()
									).setTypeIdAndData(tmpRawBlocks[pos], tmpRawBlockData[pos], true);
								}
							}
						}
					}
				}
			}
		}
		
		// Remove from cache; we shouldn't need it again
		if(roomCache.containsKey(hash))
		{
			roomCache.remove(hash);
		}
		
		System.out.println("Decoration Time {"+arg0+","+arg1+"}: "+((System.currentTimeMillis()-startTime))+" ms");
	}

	@Override
	public Chunk getOrCreateChunk(int arg0, int arg1) {
		long startTime = System.currentTimeMillis();
		long startDbTime = 0;
		long dbTime = 0;
		
		// Get rooms from the data manager
		DungeonChunk dc = new DungeonChunk(null, DungeonRoomType.BASIC_TILE, arg0, arg1);
		dc.setWorld(world);
		
		startDbTime = System.currentTimeMillis();
		DungeonRoom[] rooms = dungeonator.getDataManager().getRoomsForNewChunk(dc);
		dbTime += (System.currentTimeMillis()-startDbTime);
		
		int roomCount = (rooms != null) ? rooms.length : 0;
		
		net.minecraft.server.World mw = ((CraftWorld)this.world).getHandle();
		
		//System.out.println("Call to getOrCreateChunk("+arg0+","+arg1+"), found "+roomCount+" rooms.");
		
		/*
		 * Initialize
		 */
		byte[] blocks = new byte[32768];
		NibbleArray dataNibble = new NibbleArray(32768, 7);	// No idea what that second parameter does
		
		/*
		 * Copy room data to chunk
		 */
		
		if(roomCount > 0)
		{
			for(int r = 0; r < rooms.length; r++)
			{
				// Set location
				rooms[r].setLocation(arg0, r, arg1);
				
				byte[] tmpBlocks = rooms[r].getRawBlocks();
				byte[] tmpData = rooms[r].getRawBlockData();
				
				for(int x = 0; x < 16; x++)
				{
					for(int z = 0; z < 16; z++)
					{
						for(int y = 0; y < 8; y++)
						{
							blocks[DungeonMath.getPosFromCoords(x, y+(8*r), z)] = tmpBlocks[DungeonMath.getRoomPosFromCoords(x, y, z)];
							
							// HACK: Add block data value to nibble array
							// Likely to break on MC update
							dataNibble.a(x, y+(8*r), z, tmpData[DungeonMath.getRoomPosFromCoords(x, y, z)]);
						}
					}
				}
			}
			
			// Save chunk to data store
			startDbTime = System.currentTimeMillis();
	        dungeonator.getDataManager().saveChunk(dc);
	        dbTime += (System.currentTimeMillis()-startDbTime);
			
			// Save rooms to data store
			startDbTime = System.currentTimeMillis();
	        dungeonator.getDataManager().saveRooms(rooms);
	        String hash = "stack-"+arg0+"-"+arg1;
	        //System.out.println("Adding hash "+hash);
	        roomCache.put(hash, rooms);
	        dbTime += (System.currentTimeMillis()-startDbTime);
		}
		else
		{
			/*
			 * Something went wrong; generate flat chunk
			 */
			
			System.out.println("No rooms available, flattening.");
		
			int pos = 0;
			for(int x = 0; x < 16; x++)
			{
				for(int z = 0; z < 16; z++)
				{
					pos = (x & 0xF) << 11 | (z & 0xF) << 7 | (0 & 0x7F);	// Converts X,Y,Z coordinates to an array index
																			// My math notes because this kicked my ass for a while:
																			// (x & 0xF) 	= x & 16				Bitwise AND
																			// x2 << 11 	= x2^11					Bitshift left (exponent)
																			// (z & 0xF) 	= z & 16				Bitwise AND
																			// z2 << 7 		= z2^7					Bitshift left (exponent)
																			// (0 & 0x7F) 	= 0 & 128 = 0			Bitwise AND with 127 (all 1's)
																			//										Does nothing here; would normally ensure that larger values were clipped to 8 bits
																			// x3 | z3 | 0 	= bitwise OR of X,Z,Y
					
					blocks[pos] = 7;										// Set to bedrock
					
					pos = (x & 0xF) << 11 | (z & 0xF) << 7 | (1 & 0x7F);
					blocks[pos] = 48;										// Set to mossy stone
				}
			}
		}
		
        Chunk chunk = new Chunk(mw, blocks, arg0, arg1);
        
        // HACK: Set chunk block data
        // 'g' is a NibbleArray in the chunk; it holds the chunk's block data values
        // Likely to break on MC updates, but what else is new
        chunk.g = dataNibble;
        
        chunk.initLighting();
        
        System.out.println("Time {"+arg0+","+arg1+"}: "+((System.currentTimeMillis()-startTime))+" ms, DB Time "+dbTime+" milliseconds");
        
        // Handle new room reservation
        reserveRooms(arg0,arg1);
        
        return chunk;
	}
	
	public void reserveRooms(int x, int z)
	{
		// Check the noise value
		double value = getNoise(x, 0, z);
		
		// If noise value is above threshhold
		if(value > 0.0)
		{
			// Get 3 random sets
			Vector<DungeonRoomSet> sets = dungeonator.getDataManager().getRandomRoomSets(3);
			
			if(sets != null)
			{
				// Loop through the sets and get the maximum dimension from any set
				int maxDim = 0;
				for(DungeonRoomSet s : sets)
				{
					if(s.getSizeX() > maxDim) { maxDim = s.getSizeX(); }
					if(s.getSizeY() > maxDim) { maxDim = s.getSizeY(); }
					if(s.getSizeZ() > maxDim) { maxDim = s.getSizeZ(); }
				}
				
				// Compute 5 random scatter points within X chunks of the current chunk,
				// where X is the largest dimension of the largest set, divided by 1.5
				// Use a random direction for each point
				class ScatterPlot {
					public int x;
					public int z;
				};
				
				ScatterPlot[] points = new ScatterPlot[5];
				
				for(int i = 0; i < 5; i++)
				{
					points[i] = new ScatterPlot();
					int rand = Math.random() >= 0.5 ? 2 : -3;
					points[i].x = (rand*maxDim) + ((int)Math.random()*maxDim) + x;
					rand = Math.random() >= 0.5 ? 2 : -3;
					points[i].z = (rand*maxDim) + ((int)Math.random()*maxDim) + z;
				}
				
				// Loop through the points; for each point, check the sets in order to
				// see if they can be placed on the destination chunk; adjust Y up or
				// down as needed to avoid other reserved rooms
				for(ScatterPlot p : points)
				{
					// If chunk is generated, bail
					Dungeonator.getLogger().info("At {"+x+","+z+"}, checking point {"+p.x+","+p.z+"}");
					if(this.world.isChunkLoaded(p.x, p.z)) { Dungeonator.getLogger().info("Chunk is already generated."); continue; }
					
					Dungeonator.getLogger().info("Chunk is free.");
					
					// Get the largest block of unreserved room Y indexes, or an empty
					// array if no rooms are unreserved in the chunk
					Vector<Integer> unreserved = dungeonator.getDataManager().getUnreservedRooms(world.getName(), p.x, p.z);
					
					if(unreserved.size() > 0)
					{
						// Loop through the sets and compare the Y size to the available space
						for(DungeonRoomSet s : sets)
						{
							// If set is too large, bail
							if(s.getSizeY() > unreserved.size()) { Dungeonator.getLogger().info("Not enough height for room set."); continue; }
							
							// Tentatively set this point and the starting Y from the
							// unreserved list as our room set origin. Check adjacent chunks
							// to make sure the additional rooms are also available
							int originX = p.x;
							int originZ = p.z;
							int originY = unreserved.get(0);
							
							boolean fail = false;
							
							for(int sx = 0; sx < s.getSizeX(); sx++)
							{
								for(int sy = 0; sy < s.getSizeY(); sy++)
								{
									for(int sz = 0; sz < s.getSizeZ(); sz++)
									{
										// If the room is reserved, set failure status and bail
										if(dungeonator.getDataManager().isRoomReserved(world.getName(),originX+sx,originY+sy,originZ+sz))
										{
											fail = true;
											break;
										}
									}
									
									// If failure has occurred, bail
									if(fail) { break; }
								}
								
								// If failure has occurred, bail
								if(fail) { break; }
							}
							
							// All rooms are available; loop through the rooms again
							// and reserve them with the appropriate room library ID
							if(!fail)
							{
								for(int sx = 0; sx < s.getSizeX(); sx++)
								{
									for(int sy = 0; sy < s.getSizeY(); sy++)
									{
										for(int sz = 0; sz < s.getSizeZ(); sz++)
										{
											dungeonator.getDataManager().setRoomReservation(world.getName(), originX+sx, originY+sy, originZ+sz, s.getLibraryRoomID(sx,sy,sz));
										}
									}
								}
								
								// Log
								Dungeonator.getLogger().info("Reserved room set "+s+" at {"+originX+","+originY+","+originZ+"}");
								
								// Move the set to the end of the list
								sets.remove(s);
								sets.add(s);
								
								// Break the loop to go on to the next point
								break;
							}
						}
					}
				}
			}
		}
		
		System.out.println("Noise for {"+x+","+z+"}: "+value);
	}

	@Override
	public boolean isChunkLoaded(int arg0, int arg1) {
		// Always returns true; I believe this is present due to some
		// strangeness/sloppiness in how the IChunkProvider interface
		// is used for multiple purposes
		System.out.println("Call to isChunkLoader("+arg0+","+arg1+")");
		return true;
	}

	@Override
	public boolean saveChunks(boolean arg0, IProgressUpdate arg1) {
		// Does nothing in this implementation
		System.out.println("Call to saveChunks("+arg0+","+arg1+")");
		return true;
	}

	@Override
	public boolean unloadChunks() {
		// Does nothing in this implementation
		return false;
	}

	@Override
	public boolean canSave() {
		// Does nothing in this implementation
		return false;
	}

	@Override
	public List<?> a(EnumCreatureType arg0, int arg1, int arg2, int arg3) {
		return null;
	}

	@Override
	public ChunkPosition a(net.minecraft.server.World arg0, String arg1,
			int arg2, int arg3, int arg4) {
		return null;
	}

}
