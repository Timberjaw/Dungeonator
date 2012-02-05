package com.aranai.dungeonator.datastore;

/**
 * A DataStoreDeleteException is thrown when a data storage delete operation
 * fails. The exception includes details on the failure reason and the data
 * used in the unsuccessful operation.
 */
public class DataStoreDeleteException extends DataStoreException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -7272930944577054224L;
	
	/**
	 * Instantiates a new delete exception with default entries.
	 */
	public DataStoreDeleteException()
	{
		super();
	}
	
	/**
	 * Instantiates a new delete exception with a specified reason for failure
	 * and the delete location at which the failure occurred.
	 *
	 * @param reason the reason for failure
	 * @param saveLocation the delete location
	 */
	public DataStoreDeleteException(String reason, String deleteLocation)
	{
		super(reason, deleteLocation);
	}


}