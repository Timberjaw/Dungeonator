package com.aranai.dungeonator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftChunk;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldListener;

import com.aranai.dungeonator.generator.DungeonChunkProvider;

public class DWorldListener extends WorldListener {
	
	/** Dungeonator plugin instance. */
	private Dungeonator plugin;
	
	private DungeonChunkProvider dcp;
	
	/** Debug field: flattened: flattened chunks */
	private HashSet<String> flattened = new HashSet<String>();
	
	/**
	 * Instantiates a new world listener.
	 *
	 * @param plugin the Dungeonator plugin instance
	 */
	public DWorldListener(Dungeonator plugin)
	{
		this.plugin = plugin;
		
		plugin.py = 0;
		
		World world = plugin.getServer().getWorlds().get(0);
		Chunk chunks[] = world.getLoadedChunks();
		
		String worldName = world.getName();
		
		this.dcp = new DungeonChunkProvider(world, 0);
		((CraftWorld)world).getHandle().chunkProviderServer.chunkProvider = dcp;
		
		for(int ci = 0; ci < chunks.length; ci++)
		{
			Chunk c = chunks[ci];
			
			String hash = worldName+"."+c.getX()+"."+c.getZ();
			flattened.add(hash);
			
			System.out.print("[F:"+c.getX()+","+c.getZ() + " - " + hash + "]");
			
			this.flatten(world, c);
			
			// Unload chunk
			plugin.getServer().getWorlds().get(0).unloadChunk(c.getX(), c.getZ());
			
			chunks[ci] = null;
			
			if((ci % 50) == 0) { System.gc(); }
		}
		
		// DEBUG: Add sand block near spawn
		/*
		world.getBlockAt(0,1,0).setType(Material.SAND);
		world.getBlockAt(1,1,0).setType(Material.SAND);
		world.getBlockAt(1,1,1).setType(Material.SAND);
		world.getBlockAt(0,1,1).setType(Material.SAND);
		world.getBlockAt(1,1,-1).setType(Material.SAND);
		world.getBlockAt(0,1,-1).setType(Material.SAND);
		world.getBlockAt(-1,1,-1).setType(Material.SAND);
		world.getBlockAt(-1,1,0).setType(Material.SAND);
		world.getBlockAt(-1,1,1).setType(Material.SAND);
		*/
		
		List<Entity> entities = world.getEntities();
		
		for(Entity e : entities)
		{
			e.remove();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.bukkit.event.world.WorldListener#onChunkLoad(org.bukkit.event.world.ChunkLoadEvent)
	 */
	public void onChunkLoad(ChunkLoadEvent e)
	{
		/*
		 * Until the Generator branch is merged, we're relegated to waiting for the server to generate and load a chunk,
		 * then immediately overwriting it with our own data. This is massively inefficient and I hate it.
		 */
		
		// HACK: Force re-lighting calculation
		// TODO: Don't hack
		((CraftChunk)e.getChunk()).getHandle().b(); // re-do SKYLIGHT
		
		return;
	}
	
	public void flatten(World w, Chunk c)
	{		
		byte[] blocks = new byte[32768];
		
		Arrays.fill(blocks, (byte)0);
		
		int pos = 0;
		for(int x = 0; x < 16; x++)
		{
			for(int z = 0; z < 16; z++)
			{
				pos = (x & 0xF) << 11 | (z & 0xF) << 7 | (plugin.py & 0x7F);
				blocks[pos] = 1;
			}
		}
		
		((CraftChunk)c).getHandle().b = blocks;
		
		// DEBUG: Try to force lighting recalc
		((CraftChunk)c).getHandle().b(); // Redo SKYLIGHT
		
		w.refreshChunk(c.getX(), c.getZ());
		for(Entity e : c.getEntities())
		{
			e.remove();
		}
	}
}
