package com.aranai.dungeonator.datastore;

public class DataStoreException extends Exception {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 7977452700165549758L;

	/** The reason for failure. */
	private String reason;
	
	/** The location at which a failure occurred. Format will vary depending on data store type. */
	private String location;
	
	/**
	 * Instantiates a new data store exception with default entries.
	 */
	public DataStoreException()
	{
		this("Unknown Reason", "Unknown Location");
	}
	
	/**
	 * Instantiates a new data store exception with a specified reason for
	 * failure and the location at which the failure occurred.
	 *
	 * @param reason the reason
	 * @param location the location
	 */
	public DataStoreException(String reason, String location)
	{
		super();
		this.reason = reason;
		this.location = location;
	}
	
	/**
	 * Gets the reason for failure.
	 *
	 * @return the reason
	 */
	public String getReason()
	{
		return reason;
	}
	
	/**
	 * Gets the save location. The format will vary depending on the data store type.
	 *
	 * @return the save location
	 */
	public String getLocation()
	{
		return location;
	}
}
