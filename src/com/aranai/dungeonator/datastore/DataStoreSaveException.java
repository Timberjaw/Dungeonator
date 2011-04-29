package com.aranai.dungeonator.datastore;

// TODO: Auto-generated Javadoc
/**
 * A SaveException is thrown when a data storage save operation fails.
 * The exception includes details on the failure reason and the data
 * used in the unsuccessful operation.
 */
public class DataStoreSaveException extends DataStoreException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -7272930944577054224L;
	
	/**
	 * Instantiates a new save exception with default entries.
	 */
	public DataStoreSaveException()
	{
		super();
	}
	
	/**
	 * Instantiates a new save exception with a specified reason for failure
	 * and the save location at which the failure occurred.
	 *
	 * @param reason the reason for failure
	 * @param saveLocation the save location
	 */
	public DataStoreSaveException(String reason, String saveLocation)
	{
		super(reason, saveLocation);
	}


}