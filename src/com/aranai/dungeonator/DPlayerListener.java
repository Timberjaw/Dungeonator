package com.aranai.dungeonator;

import java.util.Arrays;
import java.util.LinkedList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftChunk;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;

import com.aranai.dungeonator.event.DCommandEvent;

public class DPlayerListener extends PlayerListener {
	private Dungeonator plugin;
	
	public DPlayerListener(Dungeonator instance)
	{
		plugin = instance;
	}
	
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args)
	{
		if(command.getName().equalsIgnoreCase("editor") || command.getName().equalsIgnoreCase("edit"))
		{
			// Pass command to editor
			if(args.length > 0)
			{
				String editCommand = args[0];
				LinkedList<String> editArgList = new LinkedList<String>();
				for(String a : args) { editArgList.add(a); }
				plugin.getChunkEditor().onCommand(new DCommandEvent((Player)sender, editCommand, editArgList.toArray(new String[editArgList.size()])));
			}
			else
			{
				// TODO: Show basic edit help
			}
			
			return true;
		}
		
		if(command.getName().equalsIgnoreCase("flattenate"))
		{
			plugin.flattenOn = (plugin.flattenOn) ? false : true;
		}
		
		if(command.getName().equalsIgnoreCase("flatten"))
		{
			Player p = (Player)sender;
			
			CraftChunk c = (CraftChunk)p.getWorld().getChunkAt(p.getLocation());
			
			if(plugin.py == 1000)
			{
				plugin.py = p.getLocation().getBlockY()-3;
			}
			
			for(int y = 0; y < 128; y++)
			{
				for(int x = 0; x < 16; x++)
				{
					for(int z = 0; z < 16; z++)
					{
						if(y == plugin.py)
						{
							c.getBlock(x, y, z).setType(Material.BEDROCK);
						}
						else
						{
							c.getBlock(x, y, z).setType(Material.AIR);
						}
					}
				}
			}
			
			Location newLocation = p.getLocation();
			newLocation.setY(plugin.py + 2);
			p.teleport(newLocation);
		}
		
		if(command.getName().equalsIgnoreCase("tile"))
		{
			Player p = (Player)sender;
			CraftChunk c = (CraftChunk)p.getWorld().getChunkAt(p.getLocation());
			
			System.out.println("[TILE "+c.getX()+","+c.getZ());
			
			byte dirt = (byte) Material.DIRT.getId();
			byte maxX = 0;
			byte maxZ = 0;
			byte maxY = 0;
			
			// Place a tile in the current chunk
			byte[][][] tile = new byte[128][16][16];
			
			// Zerofill
			for(int y = 0; y < 128; y++)
			{
				for(int x = 0; x < 16; x++)
				{
					Arrays.fill(tile[y][x], (byte)0);
				}
			}
			
			// Build walls
			tile[1][0][0] = dirt;
			tile[1][0][1] = dirt;
			tile[1][1][0] = dirt;
			tile[2][0][0] = dirt;
			tile[2][0][1] = dirt;
			tile[2][1][0] = dirt;
			
			maxX = (byte)1;
			maxZ = (byte)1;
			maxY = (byte)2;
			
			for(int y = 0; y <= maxY; y++)
			{
				for(int x = 0; x <= maxX; x++)
				{
					for(int z = 0; z <= maxZ; z++)
					{
						c.getBlock(x, y, z).setTypeId(tile[y][x][z]);
					}
				}
			}
		}
		
		return true;
	}
}
