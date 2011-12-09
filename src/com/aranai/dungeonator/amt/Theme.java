package com.aranai.dungeonator.amt;

import java.util.HashMap;

/**
 * Handles room themes and Automatic Material Translation (AMT) for
 * conversion in the editor and generator.
 * 
 * @author Steven Richards
 *
 */
public class Theme {
	
	/** The theme's name */
	private String name;
	
	/** The material translation list */
	protected HashMap<ThemeMaterial,ThemeMaterialTranslation> translations;
	
	/** The reverse translation list */
	protected HashMap<String,ThemeMaterial> reverseTranslations;
	
	protected Theme()
	{
		// Set the default name
		name = "Unnamed";
		
		// Initialize the default material list
		translations = new HashMap<ThemeMaterial,ThemeMaterialTranslation>();
		reverseTranslations = new HashMap<String,ThemeMaterial>();
		ThemeMaterialTranslation tmpMat = new ThemeMaterialTranslation(-1, -1);
		for(ThemeMaterial material : ThemeMaterial.values())
		{
			// Set material to -1 indicating 'unused'
			setTranslation(material, tmpMat);
		}
	}
	
	protected Theme(String name)
	{
		this();
		this.setName(name);
	}
	
	/**
	 * Set the theme's name
	 * @param the name
	 */
	protected void setName(String name)
	{
		this.name = name.toUpperCase();
	}
	
	/**
	 * Get the theme's name
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Set a material translation for the theme
	 * @param the theme material
	 * @param the translation result to use for the material
	 */
	protected void setTranslation(ThemeMaterial material, ThemeMaterialTranslation mt)
	{
		translations.put(material, mt);
		reverseTranslations.put(mt.type+"."+mt.sub, material);
	}
	
	/**
	 * Retrieve a material translation for the theme
	 * @param the theme material
	 * @return the translation result to use for the material
	 */
	protected ThemeMaterialTranslation getTranslation(ThemeMaterial material)
	{
		return translations.get(material);
	}
	
	/**
	 * Retrieve a material based on the specified translation
	 * @param the translation
	 * @return the material
	 */
	protected ThemeMaterial getMaterial(ThemeMaterialTranslation translation)
	{
		String key = translation.type+"."+translation.sub;
		return reverseTranslations.get(key);
	}
}
