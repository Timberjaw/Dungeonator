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
	public final int type;
	
	/** The translation's subtype id */
	public final int sub;
	
	public ThemeMaterialTranslation(int type, int sub)
	{
		this.type = type;
		this.sub = sub;
	}
	
	public String toString()
	{
		return "["+this.type+"."+this.sub+"]";
	}
}
