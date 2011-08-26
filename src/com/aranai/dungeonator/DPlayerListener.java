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
		
		// Map test
		if(command.getName().equalsIgnoreCase("testmap"))
		{
			/*
			 * TODO: Update to use native API
			 */
			
			/*
			if (!(sender instanceof Player)) {
	            return false;
	        }
	        final Player player = (Player) sender;

	        ItemStack stack = player.getItemInHand();
	        if (stack.getType() != Material.MAP) {
	            sender.sendMessage("You must be holding a map");
	            return true;
	        }
	        final short mapId = stack.getDurability();

	        TextRenderer text = new TextRenderer();
	        CharacterSprite sword = CharacterSprite.make(
	            "   XX   ",
	            "   XX   ",
	            "   XX   ",
	            "XXXXXXXX",
	            "XXXXXXXX",
	            "   XX   ",
	            "   XX   ",
	            "   XX   ");
	        text.setChar('\u0001', sword);
	        
	        CharacterSprite heart = CharacterSprite.make(
		            "        ",
		            " XX  XX ",
		            "XXXXXXXX",
		            "XXXXXXXX",
		            " XXXXXX ",
		            "  XXXX  ",
		            "   XX   ",
		            "        ");
		    text.setChar('\u0002', heart);
	        
	        MapInfo info = mapi.loadMap(player.getWorld(), mapId);
	        info.setPosition(1 << 16, 1 << 16);
	        info.setData(new byte[128 * 128]);
	        text.fancyRender(info, 10, 3, "§16;\u0002 §10;Heal II - Spell\n" +
	        	"§16;\u0001 §10;Heals one or more\n" + 
	        	"  targets instantly.\n" +
	            "§16;\u0001 Self Heal: §10;10\n" + 
	            "§16;\u0001 Party Heal: §10;20\n" + 
	            "§16;\u0001 Radius: §10;1 Room\n");
	        
	        mapi.saveMap(player.getWorld(), mapId, info);
	        mapi.sendMap(player, mapId, info.getData());
	        */
		}
				
		return true;
	}
	
	public void onPlayerRespawn(PlayerRespawnEvent event)
	{
		event.setRespawnLocation(new Location(event.getRespawnLocation().getWorld(), 0, 5, 0));
	}
}
