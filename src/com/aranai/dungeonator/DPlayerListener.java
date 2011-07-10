package com.aranai.dungeonator;

import java.util.LinkedList;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerRespawnEvent;

import com.aranai.dungeonator.event.DCommandEvent;

public class DPlayerListener extends PlayerListener {
	private Dungeonator plugin;
	
	public DPlayerListener(Dungeonator instance)
	{
		plugin = instance;
	}
	
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args)
	{
		/*
		 * Editor Commands
		 */
		if(command.getName().equalsIgnoreCase("edit"))
		{
			// Pass command to editor
			if(args.length > 0)
			{
				// Get base command, like 'save', 'load', etc
				String editCommand = args[0];
				
				// Strip out first arg so we can pass the rest
				LinkedList<String> editArgList = new LinkedList<String>();
				for(String a : args) { editArgList.add(a); }
				
				// Create the command event and hand it off to the DungeonRoomEditor
				plugin.getChunkEditor().onCommand(new DCommandEvent((Player)sender, editCommand, editArgList.toArray(new String[editArgList.size()])));
			}
			else
			{
				// List available commands
				sender.sendMessage("Available commands: load, new, save, cancel");
			}
		}
				
		return true;
	}
	
	public void onPlayerRespawn(PlayerRespawnEvent event)
	{
		event.setRespawnLocation(new Location(event.getRespawnLocation().getWorld(), 0, 5, 0));
	}
}
