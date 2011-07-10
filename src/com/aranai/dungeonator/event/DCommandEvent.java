package com.aranai.dungeonator.event;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;

/**
 * The Class DCommandEvent.
 * 
 * Provides a standard set of attributes associated with Dungeonator command input: player, base command, additional arguments, and originating chunk.
 * Named arguments are supported with the format: "argument:value". Values can be of types String, integer, float, or boolean.
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
		 * Gets a boolean value from a named argument
		 *
		 * @param name the name of the argument to look for
		 * @param defaultValue the default value to use if the argument was not specified
		 * @return the resulting boolean value
		 */
		public boolean getNamedArgBool(String name, boolean defaultValue)
		{
			String value = this.getNamedArg(name);
			
			if(value != null)
			{
				if(value.equalsIgnoreCase("true"))
				{
					return true;
				}
				else if(value.equalsIgnoreCase("false"))
				{
					return false;
				}
			}
			
			return defaultValue;
		}
		
		/**
		 * Gets the int value from a named argument
		 *
		 * @param name the name of the argument to look for
		 * @param defaultValue the default value to use if the named argument was not specified
		 * @return the resulting int value
		 */
		public int getNamedArgInt(String name, int defaultValue)
		{
			String value = this.getNamedArg(name);
			
			if(value != null)
			{
				// Attempt to parse an integer value from the value
				try
				{
					int intValue = Integer.parseInt(value);
					return intValue;
				}
				catch(NumberFormatException e)
				{
					// Bad value; do nothing
				}
			}
			
			return defaultValue;
		}
		
		/**
		 * Gets the float value from a named argument
		 *
		 * @param name the name of the argument to look for
		 * @param defaultValue the default value to use if the named argument was not specified
		 * @return the resulting float value
		 */
		public float getNamedArgFloat(String name, float defaultValue)
		{
			String value = this.getNamedArg(name);
			
			if(value != null)
			{
				// Attempt to parse an integer value from the value
				try
				{
					float floatValue = Float.parseFloat(value);
					return floatValue;
				}
				catch(NumberFormatException e)
				{
					// Bad value; do nothing
				}
			}
			
			return defaultValue;
		}
		
		/**
		 * Gets a string value from a named argument
		 *
		 * @param name the name of the argument to look for
		 * @param defaultValue the default value to use if the argument was not specified
		 * @return the resulting string value
		 */
		public String getNamedArgString(String name, String defaultValue)
		{
			String value = this.getNamedArg(name);
			
			if(value != null)
			{
				return value;
			}
			
			return defaultValue;
		}
		
		/**
		 * Gets the raw string value of a named argument.
		 *
		 * @param name the name of the argument to look for
		 * @return the resulting raw string value, or null if the named argument was not specified
		 */
		private String getNamedArg(String name)
		{
			for(String s: this.args)
			{
				// Split the string into two components
				String[] arg = s.split(":", 2);
				
				if(arg[0].equalsIgnoreCase(name) && !arg[1].isEmpty())
				{
					return arg[1];
				}
			}
			
			return null;
		}
		
		/**
		 * Gets the chunk from which the command was triggered.
		 *
		 * @return the chunk
		 */
		public Chunk getChunk()
		{
			return this.chunk;
		}
}
