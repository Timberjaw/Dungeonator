package com.aranai.dungeonator.generator;

import java.util.Arrays;

import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;

import com.aranai.dungeonator.Dungeonator;
import com.aranai.dungeonator.dungeonchunk.DungeonChunk;
import com.aranai.dungeonator.dungeonchunk.DungeonRoom;
import com.aranai.dungeonator.dungeonchunk.DungeonRoomType;

import net.minecraft.server.Chunk;
import net.minecraft.server.IChunkProvider;
import net.minecraft.server.IProgressUpdate;

/*
 * See: https://github.com/Bukkit/mc-dev/blob/master/net/minecraft/server/ChunkProviderHell.java
 */

public class DungeonChunkProvider implements IChunkProvider {

	private World world;
	private Dungeonator dungeonator;
	
	public DungeonChunkProvider(World world, long i) {
		this.world = world;
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
		
		long startTime = System.currentTimeMillis();
		
		// TODO Hand control to DungeonChunkGenerator
		
		// Get active rooms for chunk
		DungeonChunk dc = dungeonator.getDataManager().getChunk(this.world.getName(), arg1, arg2);
		dc.setHandle(this.world.getChunkAt(arg1, arg2));
		DungeonRoom[] rooms = dungeonator.getDataManager().getRoomsForChunk(dc);
		
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
						if(blocks[pos] > 0)
						{
							dc.getHandle().getBlock(x, y + (r * 8), z).setData(data[pos]);
						}
					}
				}
			}
			
			// TODO: Add tile entities
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
				
				for(int x = 0; x < 16; x++)
				{
					for(int z = 0; z < 16; z++)
					{
						for(int y = 0; y < 8; y++)
						{
							blocks[DungeonMath.getPosFromCoords(x, y+(8*r), z)] = tmpBlocks[DungeonMath.getRoomPosFromCoords(x, y, z)];
						}
					}
				}
			}
			
			// Save rooms to data store
			startDbTime = System.currentTimeMillis();
	        dungeonator.getDataManager().saveRooms(rooms);
	        dbTime += (System.currentTimeMillis()-startDbTime);
			
			// Save chunk to data store
			startDbTime = System.currentTimeMillis();
	        dungeonator.getDataManager().saveChunk(dc);
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
