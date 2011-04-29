package com.aranai.dungeonator;

/**
 * Specifies byte values for the 12 cardinal directions, starting at 0 for North and proceeding clockwise.
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
	
	/** North Side Directions */
	public static byte[] SIDE_NORTH = { NNW, N, NNE };
	
	/** East Side Directions */
	public static byte[] SIDE_EAST = { ENE, E, ESE };
	
	/** South Side Directions */
	public static byte[] SIDE_SOUTH = { SSW, S, SSE };
	
	/** West Side Directions */
	public static byte[] SIDE_WEST = { WSW, W, WNW };
	
	/** Cardinal Directions */
	public static byte[] CARDINALS = { N, E, S, W };
	
	/** Clockwise Direction Sequence */
	public static byte CLOCKWISE = 100;
	
	/** Counter-Clockwise Direction Sequence */
	public static byte COUNTER_CLOCKWISE = -100;
	
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
			if(direction < 3) { return SIDE_NORTH; }
			else if(direction < 6) { return SIDE_EAST; }
			else if(direction < 9) { return SIDE_SOUTH; }
			else { return SIDE_WEST; }
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
		if(start >= 0 && start <= 11)
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
