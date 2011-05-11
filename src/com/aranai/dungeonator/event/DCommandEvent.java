package com.aranai.dungeonator.event;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;

/**
 * The Class DCommandEvent.
 * 
 * Provides a standard set of attributes associated with Dungeonator command input.
 */
public class DCommandEvent {
	
		/** The player who triggered the command. */
		private Player player;
		
		/** The command. */
		private String cmd;
		
		/** Additional arguments. */
		private String[] args;
		
		/** The chunk in which the player is located. */
		private Chunk chunk;

		/**
		 * Instantiates a new DCommandEvent.
		 *
		 * @param p the player who triggered the command
		 * @param cmd the command
		 * @param args additional arguments
		 */
		public DCommandEvent(Player p, String cmd, String[] args)
		{
			this.player = p;
			this.cmd = cmd.toLowerCase();	// Convert to lowercase now so we don't have to later
			this.args = args;
			
			// Get the chunk in which the player is located
			this.chunk = p.getWorld().getChunkAt(p.getLocation());
		}
		
		/**
		 * Gets the player.
		 *
		 * @return the player
		 */
		public Player getPlayer()
		{
			return this.player;
		}
		
		/**
		 * Gets the command.
		 *
		 * @return the command
		 */
		public String getCmd()
		{
			return this.cmd;
		}
		
		/**
		 * Gets the args.
		 *
		 * @return the args
		 */
		public String[] getArgs()
		{
			return this.args;
		}
		
		/**
		 * Gets the chunk.
		 *
		 * @return the chunk
		 */
		public Chunk getChunk()
		{
			return this.chunk;
		}
}
