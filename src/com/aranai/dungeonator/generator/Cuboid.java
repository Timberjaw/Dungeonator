package com.aranai.dungeonator.generator;

import org.bukkit.Location;
import org.bukkit.World;

/**
 * The Cuboid class allows for cubic regions of variable size and thickness
 * to be specified at a given offset from the chunk origin. This is useful
 * for specifying walls, rooms, and other cubic volumes with minimal fuss.
 */
public class Cuboid {
	
	/** The x offset from the chunk origin. */
	private int offsetX;
	
	/** The y offset from the chunk origin. */
	private int offsetY;
	
	/** The z offset from the chunk origin. */
	private int offsetZ;
	
	/** The cuboid width. */
	private int sizeX;
	
	/** The cuboid height. */
	private int sizeY;
	
	/** The cuboid depth. */
	private int sizeZ;
	
	/** The cuboid wall thickness, or 0 if the cuboid is solid. */
	private int thickness;
	
	/**
	 * Instantiates a new cuboid with default values. This will not produce
	 * a usable cuboid, as it will have size 0.
	 */
	public Cuboid()
	{
		this(0, 0, 0);
	}
	
	/**
	 * Instantiates a new cuboid with size values.
	 *
	 * @param x the cuboid's width
	 * @param y the cuboid's height
	 * @param z the cuboid's depth
	 */
	public Cuboid(int x, int y, int z)
	{
		this(x, y, z, 0);
	}
	
	/**
	 * Instantiates a new cuboid with size and thickness values.
	 *
	 * @param x the cuboid's width
	 * @param y the cuboid's height
	 * @param z the cuboid's depth
	 * @param t the cuboid's wall thickness
	 */
	public Cuboid(int x, int y, int z, int t)
	{
		this(0, 0, 0, x, y, z, t);
	}
	
	/**
	 * Instantiates a new cuboid with size and offset values.
	 *
	 * @param offX the x offset from chunk origin
	 * @param offY the y offset from chunk origin
	 * @param offZ the z offset from chunk origin
	 * @param x the cuboid's width
	 * @param y the cuboid's height
	 * @param z the cuboid's depth
	 */
	public Cuboid(int offX, int offY, int offZ, int x, int y, int z)
	{
		this(offX, offY, offZ, x, y, z, 0);
	}
	
	/**
	 * Instantiates a new cuboid with all values.
	 *
	 * @param offX the x offset from chunk origin
	 * @param offY the y offset from chunk origin
	 * @param offZ the z offset from chunk origin
	 * @param x the cuboid's width
	 * @param y the cuboid's height
	 * @param z the cuboid's depth
	 * @param t the cuboid's wall thickness
	 */
	public Cuboid(int offX, int offY, int offZ, int x, int y, int z, int t)
	{
		offsetX = offX;
		offsetY = offY;
		offsetZ = offZ;
		sizeX = x;
		sizeY = y;
		sizeZ = z;
		thickness = t;
	}
	
	/**
	 * Checks if the cuboid is solid.
	 *
	 * @return true, if the cuboid is solid
	 */
	public boolean isSolid()
	{
		return (thickness == 0);
	}
	
	/**
	 * Gets the cuboid's wall thickness.
	 *
	 * @return the thickness
	 */
	private int getThickness()
	{
		return thickness;
	}
	
	/**
	 * Gets the x offset from chunk origin.
	 *
	 * @return the x offset
	 */
	public int getOffsetX()
	{
		return offsetX;
	}
	
	/**
	 * Gets the y offset from chunk origin.
	 *
	 * @return the y offset
	 */
	public int getOffsetY()
	{
		return offsetY;
	}
	
	/**
	 * Gets the z offset from chunk origin
	 *
	 * @return the z offset
	 */
	public int getOffsetZ()
	{
		return offsetZ;
	}
	
	/**
	 * Gets the offset as a location.
	 *
	 * @return the offset location
	 */
	public Location getOffsetLocation()
	{
		return new Location(null, offsetX, offsetY, offsetZ);
	}
	
	/**
	 * Gets the cuboid's width.
	 *
	 * @return the width
	 */
	public int getSizeX()
	{
		return sizeX;
	}
	
	/**
	 * Gets the cuboid's height.
	 *
	 * @return the height
	 */
	public int getSizeY()
	{
		return sizeY;
	}
	
	/**
	 * Gets the cuboid's depth.
	 *
	 * @return the depth
	 */
	public int getSizeZ()
	{
		return sizeZ;
	}
}
