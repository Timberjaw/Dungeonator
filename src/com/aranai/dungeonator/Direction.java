package com.aranai.dungeonator;

import java.util.Hashtable;
import java.util.Map;

/**
 * Specifies byte values for the 12 cardinal directions, up, and down, starting at 0 for North and proceeding clockwise.
 * Also includes convenience arrays and functions for determining side from direction, directions from side,
 * and computing rotations. 
 */
public final class Direction {
	
	/** North */
	public static byte N = 0;
	
	/** East */
	public static byte E = 3;
	
	/** South */
	public static byte S = 6;
	
	/** West */
	public static byte W = 9;
	
	/** North by Northeast */
	public static byte NNE = 1;
	
	/** East by Northeast */
	public static byte ENE = 2;
	
	/** East by Southeast */
	public static byte ESE = 4;
	
	/** South by Southeast */
	public static byte SSE = 5;
	
	/** South by Southwest */
	public static byte SSW = 7;
	
	/** West by Southwest */
	public static byte WSW = 8;
	
	/** West by Northwest */
	public static byte WNW = 10;
	
	/** North by Northwest */
	public static byte NNW = 11;
	
	/** Up */
	public static byte UP = 12;
	
	/** Down */
	public static byte DOWN = 13;
	
	/** North Side Directions */
	public static byte[] SIDE_NORTH = { NNW, N, NNE };
	
	/** East Side Directions */
	public static byte[] SIDE_EAST = { ENE, E, ESE };
	
	/** South Side Directions */
	public static byte[] SIDE_SOUTH = { SSW, S, SSE };
	
	/** West Side Directions */
	public static byte[] SIDE_WEST = { WSW, W, WNW };
	
	/** Top side directions */
	public static byte[] SIDE_TOP = { UP };
	
	/** Bottom side directions */
	public static byte[] SIDE_BOTTOM = { DOWN };
	
	/** Cardinal Directions */
	public static byte[] CARDINALS = { N, E, S, W };
	
	/** Clockwise Direction Sequence */
	public static byte CLOCKWISE = 100;
	
	/** Counter-Clockwise Direction Sequence */
	public static byte COUNTER_CLOCKWISE = -100;
	
	/** Direction name/value list for easy lookup from user commands. */
	public static Hashtable<String,Byte> directionValues = new Hashtable<String,Byte>();
	
	/** Direction value/name list for easy lookup. Reverse table of directionValues. */
	public static Hashtable<Byte,String> directionNames = new Hashtable<Byte,String>();
	
	static {
		// Add directions to direction name list
		directionValues.put("n", Direction.N);
		directionValues.put("nne", Direction.NNE);
		directionValues.put("ene", Direction.ENE);
		directionValues.put("e", Direction.E);
		directionValues.put("ese", Direction.ESE);
		directionValues.put("sse", Direction.SSE);
		directionValues.put("s", Direction.S);
		directionValues.put("ssw", Direction.SSW);
		directionValues.put("wsw", Direction.WSW);
		directionValues.put("w", Direction.W);
		directionValues.put("wnw", Direction.WNW);
		directionValues.put("nnw", Direction.NNW);
		directionValues.put("u", Direction.UP);
		directionValues.put("d", Direction.DOWN);
		// Add direction aliases
		directionValues.put("north", Direction.N);
		directionValues.put("northnortheast", Direction.NNE);
		directionValues.put("eastnortheast", Direction.ENE);
		directionValues.put("east", Direction.E);
		directionValues.put("eastsoutheast", Direction.ESE);
		directionValues.put("southsoutheast", Direction.SSE);
		directionValues.put("south", Direction.S);
		directionValues.put("southsouthwest", Direction.SSW);
		directionValues.put("westsouthwest", Direction.WSW);
		directionValues.put("west", Direction.W);
		directionValues.put("westnorthwest", Direction.WNW);
		directionValues.put("northnorthwest", Direction.NNW);
		directionValues.put("up", Direction.UP);
		directionValues.put("down", Direction.DOWN);
		
		// Set up reverse lookup table for looking up direction names by value
		for(Map.Entry<String,Byte> entrySet : directionValues.entrySet())
		{
			directionNames.put(entrySet.getValue(), entrySet.getKey());
		}
	}
	
	/**
	 * Lookup a direction by its string name
	 *
	 * @param direction the string name for the direction
	 * @return the direction
	 */
	public static byte getDirectionFromString(String direction)
	{
		if(directionValues.containsKey(direction))
		{
			return directionValues.get(direction);
		}
		
		return -1;
	}
	
	/**
	 * Lookup a direction string by its value
	 *
	 * @param direction the byte value for the direction
	 * @return the direction name
	 */
	public static String getDirectionName(byte direction)
	{
		if(isValidDirection(direction))
		{
			return directionNames.get(direction);
		}
		
		return null;
	}
	
	/**
	 * Checks if a given byte value is a valid direction.
	 *
	 * @param direction the value to check
	 * @return true, if it is a valid direction
	 */
	public static boolean isValidDirection(byte direction)
	{
		return directionNames.containsKey(direction);
	}
	
	/**
	 * Gets the side to which the specified direction belongs.
	 *
	 * @param direction the direction
	 * @return the side the direction belongs to
	 */
	public static byte[] getSideFromDirection(byte direction)
	{
		if(direction >= 0 && direction <= 12)
		{
			if(direction < 3) 			{ return SIDE_NORTH; }
			else if(direction < 6) 		{ return SIDE_EAST; }
			else if(direction < 9) 		{ return SIDE_SOUTH; }
			else if(direction < 12) 	{ return SIDE_WEST; }
			else if(direction == 12) 	{ return SIDE_TOP; }
			else if(direction == 13) 	{ return SIDE_BOTTOM; }
		}
		
		return null;
	}
	
	/**
	 * Return a direction rotated a specified number of steps, either clockwise
	 * or counterclockwise, from a specified starting direction.
	 *
	 * @param start the starting direction
	 * @param direction the direction to rotate
	 * @param advance the number of steps to advance
	 * @return the resulting direction, or -1 if the input is invalid
	 */
	public static byte rotate(byte start, byte direction, int advance)
	{
		if(start >= 0 && start < 12)
		{
			if(direction == CLOCKWISE)
			{
				return (byte) ((start + advance) % 11);
			}
			else if(direction == COUNTER_CLOCKWISE)
			{
				return (byte) ((start - advance) % 11);
			}
		}
		
		return -1;
	}
}
