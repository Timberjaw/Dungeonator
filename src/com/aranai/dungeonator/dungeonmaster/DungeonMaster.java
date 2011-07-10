package com.aranai.dungeonator.dungeonmaster;

import org.bukkit.entity.Player;

/**
 * The DungeonMaster is a special player with godlike powers and a disturbing lack of concern for player safety.
 * In more technical terms, this class contains functionality for one or more players to manipulate the Dungeonator world in a variety of ways.
 */
public class DungeonMaster {
	
	/** DM status. */
	private boolean isDmOnline;
	
	public DungeonMaster()
	{
		isDmOnline = false; 
	}
	
	/**
	 * Checks if a DM is online.
	 *
	 * @return true, if a DM is online
	 */
	public boolean isDmOnline()
	{
		return isDmOnline;
	}
	
	/**
	 * Checks if a particular player is a DM.
	 *
	 * @param player the player to check
	 * @return true, if the player is a DM
	 */
	public boolean isDm(Player player)
	{
		// Later on this will have special permission checks
		// For now, it behaves identically to isOp
		return (player.isOp());
	}
}
