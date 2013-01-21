package com.aranai.dungeonator.amt;

import java.util.HashMap;
import java.util.Vector;

import net.minecraft.server.v1_4_R1.Block;

/**
 * Handles loading of themes and conversion between themes.
 * 
 * @author Steven Richards
 *
 */
public class ThemeManager {
	
	/** The theme library */
	private HashMap<String,Theme> themes;
	
	/** The indexed view of the library */
	private Vector<String> themeIndex;
	
	public ThemeManager()
	{
		themes = new HashMap<String,Theme>();
		themeIndex = new Vector<String>();
		
		// Build the starting theme library
		this.buildThemes();
	}
	
	/**
	 * Get the number of themes loaded
	 * @return the number of themes
	 */
	public int getThemeCount()
	{
		return themes.size();
	}
	
	/**
	 * Get the name of the theme matching the specified index value
	 * @param the index
	 * @return the name of the theme
	 */
	public String getThemeNameByIndex(int index)
	{
		if(themes.size() > index)
		{
			return themeIndex.elementAt(index);
		}
		
		return null;
	}
	
	/**
	 * Check whether a theme with the given name exists
	 * @param the name of the theme
	 * @return true if the theme exists
	 */
	public boolean themeExists(String theme)
	{
		return themes.containsKey(theme.toUpperCase());
	}
	
	/**
	 * Get a material translation for a theme
	 * @param the theme
	 * @param the material
	 * @return the translation
	 */
	public ThemeMaterialTranslation getTranslation(String theme, ThemeMaterial material)
	{
		// Get theme
		Theme t = themes.get(theme);
		if(t == null) { return null; }
		
		// Lookup material translation
		return t.getTranslation(material);
	}
	
	/**
	 * Get a material translation from one theme to another based on the raw material
	 * @param the name of the current theme
	 * @param the name of the new theme
	 * @param the current material translation
	 * @return the new material translation, or null if one of the themes is invalid
	 */
	public ThemeMaterialTranslation getFullTranslation(String fromTheme, String toTheme, ThemeMaterialTranslation fromMaterial)
	{
		// Get themes
		Theme from = themes.get(fromTheme.toUpperCase());
		Theme to = themes.get(toTheme.toUpperCase());
		if(from == null || to == null) { return null; }
		
		// Get material from starting translation
		ThemeMaterial material = from.getMaterial(fromMaterial);
		
		// Get new translation
		return to.getTranslation(material);
	}
	
	public ThemeMaterial getMaterial(String theme, ThemeMaterialTranslation translation)
	{
		// Get theme
		Theme t = themes.get(theme.toUpperCase());
		if(t == null) { return null; }
		
		return t.getMaterial(translation);
	}
	
	/**
	 * Add a theme to the library
	 * @param the theme
	 */
	public void registerTheme(Theme theme)
	{
		themes.put(theme.getName().toUpperCase(), theme);
		themeIndex.add(theme.getName().toUpperCase());
	}

	/**
	 * Build the starting theme library
	 */
	private void buildThemes()
	{
		// Add the standard themes to the manager
		
		Theme t = new Theme("DEFAULT");
		t.setTranslation(ThemeMaterial.FLOOR_1, new ThemeMaterialTranslation(Block.MOSSY_COBBLESTONE.id, 0));
		t.setTranslation(ThemeMaterial.FLOOR_2, new ThemeMaterialTranslation(Block.SMOOTH_BRICK.id, 1));
		t.setTranslation(ThemeMaterial.FLOOR_3, new ThemeMaterialTranslation(Block.SMOOTH_BRICK.id, 2));
		registerTheme(t);
		
		t = new Theme("WOOD");
		t.setTranslation(ThemeMaterial.FLOOR_1, new ThemeMaterialTranslation(Block.COBBLESTONE.id, 0));
		t.setTranslation(ThemeMaterial.FLOOR_2, new ThemeMaterialTranslation(Block.LOG.id, 1));
		t.setTranslation(ThemeMaterial.FLOOR_3, new ThemeMaterialTranslation(Block.LOG.id, 2));
		registerTheme(t);
	}
}
