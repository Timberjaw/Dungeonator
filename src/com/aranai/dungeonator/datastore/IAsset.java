package com.aranai.dungeonator.datastore;

import org.jnbt.CompoundTag;
import org.jnbt.Tag;

/**
 * Defines an interface for loading and saving NBT data to and from classes.
 * A class that implements IAsset should be able to (mostly) load an instance
 * of itself from an NBT file given a filename, and save itself back to file.
 */
public interface IAsset {
	
	/**
	 * Saves an asset to NBT format on disk at the specified location.
	 *
	 * @param path the path
	 * @param filename the filename
	 * @return true, if successful
	 * @throws DataStoreAssetException the data store asset exception
	 */
	public boolean saveAsset(String path, String filename) throws DataStoreAssetException;
	
	/**
	 * Loads an asset from an NBT file into a class.
	 *
	 * @param path the path
	 * @param filename the filename
	 * @throws DataStoreAssetException the data store asset exception
	 */
	public void loadAsset(String path, String filename) throws DataStoreAssetException;
	
	/**
	 * Gets the raw asset tag from a class.
	 *
	 * @return the asset tag
	 * @throws DataStoreAssetException the data store asset exception
	 */
	public CompoundTag getAssetTag() throws DataStoreAssetException;
	
	/**
	 * Sets the asset tag.
	 *
	 * @throws DataStoreAssetException the data store asset exception
	 */
	public void setAssetTag(CompoundTag tag) throws DataStoreAssetException;
	
	/**
	 * Checks if the asset is loaded.
	 *
	 * @return true, if the asset is loaded
	 */
	public boolean isLoaded();
}
