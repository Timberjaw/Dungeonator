package com.aranai.dungeonator.generator;

import com.aranai.dungeonator.dungeonchunk.DungeonWidget;

/**
 * Contains helpful methods for doing Dungeonator math.
 */
public class DungeonMath {
	
	/**
	 * Gets a byte array position from X,Y,Z coordinates.
	 *
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 * @return the array position
	 */
	public static int getPosFromCoords(int x, int y, int z)
	{
		return (x & 0xF) << 11 | (z & 0xF) << 7 | (y & 0x7F);
	}
	
	/**
	 * Gets a byte array position within a room from X,Y,Z coords.
	 *
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 * @return the room byte array position from coords
	 */
	public static int getRoomPosFromCoords(int x, int y, int z)
	{
		return (x & 0xF) << 7 | (z & 0xF) << 3 | (y & 0x7);
	}
	
	/**
	 * Gets a byte array position within a widget from X,Y,Z coords and the widget's size class.
	 *
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 * @param size the size class of the widget
	 * @return the widget byte array position
	 */
	public static int getWidgetPosFromCoords(int x, int y, int z, DungeonWidget.Size size)
	{
		return x*size.bound()*size.bound() + y*size.bound() + z;
	}
}
