package com.aranai.dungeonator;

import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldListener;

import com.aranai.dungeonator.generator.DungeonChunkProvider;

public class DWorldListener extends WorldListener {
	
	/** Dungeonator plugin instance. */
	private Dungeonator plugin;
	
	/** The DungeonChunkProvider instance, which handles the magic of generating chunks */
	private DungeonChunkProvider dcp;
	
	/**
	 * Instantiates a new world listener.
	 *
	 * @param plugin the Dungeonator plugin instance
	 */
	public DWorldListener(Dungeonator plugin)
	{
		this.plugin = plugin;
		
		// This defines the y value when flattening chunks
		// This includes the base height for the room editor
		// We use a default value of 70 so the player doesn't
		// have to climb so far out of the hole we put them in
		plugin.py = 70;
	}
	
	/* (non-Javadoc)
	 * @see org.bukkit.event.world.WorldListener#onWorldInit(org.bukkit.event.world.WorldInitEvent)
	 */
	public void onWorldInit(WorldInitEvent e)
	{
		World world = e.getWorld();
		
		// Initialize the DungeonChunkProvider and replace the world's current provider
		// We could use Bukkit's fancy world generator stuff for this, but we're not going to because
		this.dcp = new DungeonChunkProvider(world, 0);
		this.dcp.setInstance(plugin);
		((CraftWorld)world).getHandle().chunkProviderServer.chunkProvider = dcp;
	}
	
	/* (non-Javadoc)
	 * @see org.bukkit.event.world.WorldListener#onChunkLoad(org.bukkit.event.world.ChunkLoadEvent)
	 */
	public void onChunkLoad(ChunkLoadEvent e)
	{
		return;
	}
}
