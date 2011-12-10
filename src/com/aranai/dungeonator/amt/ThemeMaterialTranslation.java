package com.aranai.dungeonator.amt;

/**
 * A ThemeMaterialTranslation represents the type and subtype ids
 * for a translation.
 * 
 * @author Steven Richards
 *
 */
public final class ThemeMaterialTranslation {
	
	/** The translation's type id */
	public final byte type;
	
	/** The translation's subtype id */
	public final byte sub;
	
	public ThemeMaterialTranslation(byte type, byte sub)
	{
		this.type = type;
		this.sub = sub;
	}
	
	public ThemeMaterialTranslation(int type, int sub)
	{
		this((byte)type, (byte)sub);
	}
	
	public String toString()
	{
		return "["+this.type+"."+this.sub+"]";
	}
}
