package com.aranai.dungeonator.event;

import java.util.Vector;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;

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
		 * Gets a single arg
		 * 
		 * @return the arg, or null if the index is invalid
		 */
		public String getArg(int index)
		{
			if(index < args.length)
			{
				return args[index];
			}
			
			return null;
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
		 * Gets a boolean value list from a named argument
		 *
		 * @param name the name of the argument to look for
		 * @param defaultValue the default value to use if the argument was not specified
		 * @return the resulting boolean value list
		 */
		public boolean[] getNamedArgBoolList(String name, boolean[] defaultValue)
		{
			String[] value = this.getNamedArgList(name);
			
			if(value != null)
			{
				// Initialize a bool array to store the parsed results
				boolean[] boolValues = new boolean[value.length];
				
				for(int i = 0; i < value.length; i++)
				{
					if(value[i].equalsIgnoreCase("true"))
					{
						boolValues[i] = true;
					}
					else if(value[i].equalsIgnoreCase("false"))
					{
						boolValues[i] = false;
					}
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
		 * Gets the int value list from a named argument
		 *
		 * @param name the name of the argument to look for
		 * @param defaultValue the default value to use if the named argument was not specified
		 * @return the resulting int value list
		 */
		public int[] getNamedArgIntList(String name, int[] defaultValue)
		{
			String[] value = this.getNamedArgList(name);
			
			if(value != null)
			{
				// Initialize an int array to store the result of the parser
				int[] intValues = new int[value.length];
				
				// Attempt to parse an integer value from the value
				try
				{
					for(int i = 0; i < value.length; i++)
					{
						int floatValue = Integer.parseInt(value[i]);
						intValues[i] = floatValue;
					}
					
					return intValues;
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
		 * Gets the float value list from a named argument
		 *
		 * @param name the name of the argument to look for
		 * @param defaultValue the default value to use if the named argument was not specified
		 * @return the resulting float value list
		 */
		public float[] getNamedArgFloatList(String name, float[] defaultValue)
		{
			String[] value = this.getNamedArgList(name);
			
			if(value != null)
			{
				// Initialize a float array to store the result of the parser
				float[] floatValues = new float[value.length];
				
				// Attempt to parse a float value from the value
				try
				{
					for(int i = 0; i < value.length; i++)
					{
						float floatValue = Float.parseFloat(value[i]);
						floatValues[i] = floatValue;
					}
					
					return floatValues;
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
		 * Gets a list of string values from a named argument
		 *
		 * @param name the name of the argument to look for
		 * @param defaultValue the default value to use if the argument was not specified
		 * @return the resulting string value list
		 */
		public String[] getNamedArgStringList(String name, String[] defaultValue)
		{
			// Get the list as a simple string first
			String[] value = this.getNamedArgList(name);
			
			if(value != null)
			{
				// Split the string
				return value;
			}
			
			return defaultValue;
		}
		
		/**
		 * Gets a vector (int) coordinate from a named argument
		 * 
		 * @param name the name of the argument to look for
		 * @param defaultValue the default value to use if the argument was not specified
		 * @return the resulting vector coordinate
		 */
		public BlockVector getNamedArgVectorInt(String name, BlockVector defaultValue)
		{
			// Get int list
			int[] intList = getNamedArgIntList(name, new int[0]);
			
			if(intList.length == 3)
			{
				return new BlockVector(intList[0],intList[1],intList[2]);
			}
			
			return defaultValue;
		}
		
		/**
		 * Gets the raw string value list of a named argument.
		 *
		 * @param name the name of the argument to look for
		 * @return the resulting raw string value list, or null if the named argument was not specified
		 */
		private String[] getNamedArgList(String name)
		{
			String value = this.getNamedArg(name);
			
			if(value != null)
			{
				// Return the split string
				return value.split(",");
			}
			
			return null;
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
