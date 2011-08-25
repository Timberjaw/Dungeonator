package com.aranai.dungeonator;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;

public class DEntityListener extends EntityListener {
	private Dungeonator plugin;
	
	public DEntityListener(Dungeonator instance)
	{
		plugin = instance;
	}
	
	public void onEntityDamage(EntityDamageEvent event)
	{
		/*
		 * If the editor is active, cancel the damage event
		 */
		if(plugin.getChunkEditor().isActive() && event.getEntity() instanceof Player)
		{
			event.setCancelled(true);
			return;
		}
		
		/*
		 * If the player is a DM, cancel the damage event
		 */
		if(event.getEntity() instanceof Player && plugin.getDungeonMaster().isDm((Player)event.getEntity()))
		{
			event.setCancelled(true);
			return;
		}
	}
}
