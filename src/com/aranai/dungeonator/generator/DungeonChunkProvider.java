package com.aranai.dungeonator.generator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;

import com.aranai.dungeonator.Dungeonator;
import com.aranai.dungeonator.dungeonchunk.DungeonChunk;
import com.aranai.dungeonator.dungeonchunk.DungeonRoom;
import com.aranai.dungeonator.dungeonchunk.DungeonRoomType;

import net.minecraft.server.Chunk;
import net.minecraft.server.IChunkProvider;
import net.minecraft.server.IProgressUpdate;
import net.minecraft.server.NibbleArray;

/*
 * See: https://github.com/Bukkit/mc-dev/blob/master/net/minecraft/server/ChunkProviderHell.java
 */

public class DungeonChunkProvider implements IChunkProvider {

	private World world;
	private Dungeonator dungeonator;
	
	private HashMap<String,DungeonRoom[]> roomCache = new HashMap<String,DungeonRoom[]>();
	
	private static HashSet<Byte> blocksWithData;
	
	public DungeonChunkProvider(World world, long i) {
		this.world = world;
		byte[] blocks = {6,17,18,23,25,27,28,29,31,33,34,35,43,44,50,53,54,61,62,64,65,66,67,69,71,75,76,77,84,86,90,91,92,95,96};
		DungeonChunkProvider.blocksWithData = new HashSet(Arrays.asList(blocks));
	}
	
	public void setInstance(Dungeonator instance)
	{
		this.dungeonator = instance;
	}

	@Override
	public Chunk getChunkAt(int x, int y) {
		// Purpose is unclear. Passes control to another method with identical args.
		System.out.println("Call to getChunkAt("+x+","+y+")");
		return this.getOrCreateChunk(x, y);
	}

	@Override
	public void getChunkAt(IChunkProvider arg0, int arg1, int arg2) {
		/*
		 * Chunk decoration phase
		 */
		
		String hash = "stack-"+arg1+"-"+arg2;
		
		long startTime = System.currentTimeMillis();
		
		// TODO Hand control to DungeonChunkGenerator
		
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
						if(DungeonChunkProvider.blocksWithData.contains(blocks[pos]))
						{
							//dc.getHandle().getBlock(x, y + (r * 8), z).setData(data[pos]);
						}
					}
				}
			}
			
			// TODO: Add tile entities
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
		
		// Get random rooms from the data manager
		DungeonChunk dc = new DungeonChunk(null, DungeonRoomType.BASIC_TILE, arg0, arg1);
		dc.setWorld(world);
		
		startDbTime = System.currentTimeMillis();
		DungeonRoom[] rooms = dungeonator.getDataManager().getRoomsForNewChunk(dc);
		dbTime += (System.currentTimeMillis()-startDbTime);
		
		int roomCount = (rooms != null) ? rooms.length : 0;
		
		// TODO: Hand control to DungeonChunkGenerator
		
		net.minecraft.server.World mw = ((CraftWorld)this.world).getHandle();
		
		//System.out.println("Call to getOrCreateChunk("+arg0+","+arg1+"), found "+roomCount+" rooms.");
		
		/*
		 * Initialize
		 */
		byte[] blocks = new byte[32768];
		NibbleArray dataNibble = new NibbleArray(32768);
		
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
        // 'e' is a NibbleArray in the chunk; it holds the chunk's block data values
        // Likely to break on MC updates, but what else is new
        chunk.e = dataNibble;
        
        chunk.initLighting();
        
        System.out.println("Time {"+arg0+","+arg1+"}: "+((System.currentTimeMillis()-startTime))+" ms, DB Time "+dbTime+" milliseconds");
        
        return chunk;
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

}
