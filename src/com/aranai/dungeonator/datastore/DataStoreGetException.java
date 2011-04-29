package com.aranai.dungeonator.datastore;

/**
 * A DataStoreGetException is thrown when a data storage get operation
 * fails. The exception includes details on the failure reason and the data
 * used in the unsuccessful operation.
 */
public class DataStoreGetException extends DataStoreException {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -3121610073513221166L;

	/**
	 * Instantiates a new get exception with default entries.
	 */
	public DataStoreGetException()
	{
		super();
	}
	
	/**
	 * Instantiates a new get exception with a specified reason for failure
	 * and the get location at which the failure occurred.
	 *
	 * @param reason the reason for failure
	 * @param saveLocation the get location
	 */
	public DataStoreGetException(String reason, String getLocation)
	{
		super(reason, getLocation);
	}


}