package com.aranai.dungeonator.generator;

import java.util.Arrays;

import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;

import net.minecraft.server.Chunk;
import net.minecraft.server.IChunkProvider;
import net.minecraft.server.IProgressUpdate;

/*
 * See: https://github.com/Bukkit/mc-dev/blob/master/net/minecraft/server/ChunkProviderHell.java
 */

public class DungeonChunkProvider implements IChunkProvider {

	private World world;
	
	public DungeonChunkProvider(World world, long i) {
		this.world = world;
	}
	
	@Override
	public boolean b() {
		// No idea. Returns true in default implementation
		return true;
	}

	@Override
	public Chunk getChunkAt(int x, int y) {
		// TODO Auto-generated method stub
		System.out.println("Call to getChunkAt("+x+","+y+")");
		return this.getOrCreateChunk(x, y);
	}

	@Override
	public void getChunkAt(IChunkProvider arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
		// Seems to handle the second step of chunk generator: populating with foliage and such
		
		System.out.println("Call to getChunkAt("+arg0+","+arg1+","+arg2+")");
	}

	@Override
	public Chunk getOrCreateChunk(int arg0, int arg1) {
		net.minecraft.server.World mw = ((CraftWorld)this.world).getHandle();
		
		System.out.println("Call to getOrCreateChunk("+arg0+","+arg1+")");

		byte[] blocks = new byte[32768];
		
		Arrays.fill(blocks, (byte)0);
		
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
																		//										Does nothing here; world normally ensure that larger values were clipped to 8 bits
																		// x3 | z3 | 0 	= bitwise OR of X,Z,Y
				
				blocks[pos] = 7;										// Set to bedrock
				
				pos = (x & 0xF) << 11 | (z & 0xF) << 7 | (1 & 0x7F);
				blocks[pos] = 48;										// Set to mossy stone
			}
		}
		
        Chunk chunk = new Chunk(mw, blocks, arg0, arg1);
        
        return chunk;
	}

	@Override
	public boolean isChunkLoaded(int arg0, int arg1) {
		// TODO Auto-generated method stub
		System.out.println("Call to isChunkLoader("+arg0+","+arg1+")");
		return true;
	}

	@Override
	public boolean saveChunks(boolean arg0, IProgressUpdate arg1) {
		// TODO Auto-generated method stub
		System.out.println("Call to saveChunks("+arg0+","+arg1+")");
		return true;
	}

	@Override
	public boolean unloadChunks() {
		// TODO Auto-generated method stub
		//System.out.println("Call to unloadChunks()");
		return false;
	}

}
