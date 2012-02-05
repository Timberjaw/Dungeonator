package com.aranai.dungeonator.datastore;

public class DataStoreAssetException extends DataStoreException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -5556866735471841792L;

	public DataStoreAssetException(String reason, String location)
	{
		super(reason, location);
	}
}
