package com.mith.EmojiChat;

import com.mith.UnicodeApi.Objects.Unicode;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.List;

class EmojiChatCommand implements CommandExecutor {
	/**
	 * EmojiChat main class instance.
	 */
	private final EmojiChat plugin;
	
	/**
	 * Creates the EmojiChat command class with the main class instance.
	 *
	 * @param plugin The EmojiChat main class instance.
	 */
	EmojiChatCommand(EmojiChat plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

		if (args.length < 1) {
			sender.sendMessage(ChatColor.AQUA + "EmojiChat v2.0.2 by Mithrillia");
			sender.sendMessage(ChatColor.AQUA + "Use " + ChatColor.GREEN + "/emojichat help" + ChatColor.AQUA + " for help.");
			return true;
		}
		
		// Sub commands
		switch (args[0].toLowerCase()) {
			case "help":
				if (!sender.hasPermission("emojichat.help")) {
					sender.sendMessage(ChatColor.RED + "You need " + ChatColor.GOLD + "emojichat.help" + ChatColor.RED + " to use this command.");
					return true;
				}
				
				sender.sendMessage(ChatColor.GOLD + "---------- " + ChatColor.RED + "EmojiChat Help"  + ChatColor.GOLD +  " ----------");
				sender.sendMessage(ChatColor.GOLD + "/emojichat help: " + ChatColor.WHITE + "Opens up this help menu.");
				sender.sendMessage(ChatColor.GOLD + "/emojichat toggle: " + ChatColor.WHITE + "Toggles emoji shortcuts on or off.");
				sender.sendMessage(ChatColor.GOLD + "/emojichat list: " + ChatColor.WHITE + "Lists all of the enabled emojis.");
				sender.sendMessage(ChatColor.GOLD + "/emojichat search {word to search} " + ChatColor.WHITE + "Searches for emojis which contain the search parameter.");
				sender.sendMessage(ChatColor.GOLD + "/emojichat browse " + ChatColor.WHITE + "Allows you to browse emoji's using a gui.");
				if(sender.hasPermission("emojichat.reload")){
					sender.sendMessage(ChatColor.GOLD + "/emojichat reload: " + ChatColor.WHITE + "Reloads the EmojiChat config.");
				}
				return true;
			case "reload":
				if (!sender.hasPermission("emojichat.reload")) {
					sender.sendMessage(ChatColor.RED + "You need " + ChatColor.GOLD + "emojichat.reload" + ChatColor.RED + " to use this command.");
					return true;
				}
				plugin.reloadConfig();
				plugin.getEmojiHandler().load(plugin);
				sender.sendMessage(ChatColor.GREEN + "EmojiChat config reloaded.");
				return true;
			case "toggle":
				if (!sender.hasPermission("emojichat.toggle")) {
					sender.sendMessage(ChatColor.RED + "You need " + ChatColor.GOLD + "emojichat.toggle" + ChatColor.RED + " to use this command.");
					return true;
				}
				
				if (sender instanceof Player) {
					plugin.getEmojiHandler().toggleShortcutsOn((Player) sender);
					sender.sendMessage(ChatColor.AQUA + "Emoji shortcuts are now " + (plugin.getEmojiHandler().hasShortcutsOn((Player) sender) ? ChatColor.GREEN + "enabled"  : ChatColor.RED + "disabled" ) + ChatColor.AQUA + ".");
				} else {
					sender.sendMessage(ChatColor.RED + "Oops, you have to be a player to toggle shortcuts.");
				}
				return true;
			case "browse":
				if (!sender.hasPermission("emojichat.browse")) {
					sender.sendMessage(ChatColor.RED + "You need " + ChatColor.GOLD + "emojichat.browse" + ChatColor.RED + " to use this command.");
					return true;
				}
				if (!(sender instanceof Player)) { // Send chat version if the sender isn't a player
					sender.sendMessage(ChatColor.RED + "Oops, you have to be a player to use the browse command");
				} else { // Send GUI version
					((Player) sender).openInventory(plugin.emojiChatGui.getInventory((Player)sender,0));
				}
				return true;
			case "list":
				if (!sender.hasPermission("emojichat.list")) {
					sender.sendMessage(ChatColor.RED + "You need " + ChatColor.GOLD + "emojichat.list" + ChatColor.RED + " to use this command.");
					return true;
				}

				Player player = (Player)sender;
				List<Unicode> playersAccess = plugin.getEmojiHandler().getEmojis(player);

				int pages = (int)Math.ceil(playersAccess.size()/15);

				if(args.length != 2){
					sender.sendMessage(ChatColor.RED + "Please specify a page to view, there are currently " + pages + " pages of Emoji's you have access to.");
					return true;
				}

				if(!StringUtils.isNumeric(args[1])){
					sender.sendMessage(ChatColor.RED + "Please specify a number to view the desired page, there are currently " + pages + " pages of Emoji's.");
					return true;
				}
				int filter = Integer.parseInt(args[1]);

				if(filter > pages || filter < 1){
					sender.sendMessage(ChatColor.RED + "Please specify a number between the range of 1 to "+ pages +".");
					return true;
				}
				//create our page pointer
				int pagePointer = (filter-1)*15;
				sender.sendMessage( ChatColor.AQUA + "----------[" + ChatColor.GRAY + "Emoji List" + ChatColor.AQUA + "]----------");

				for( int count = 0; count < 15; count++)
				{
					Unicode value = playersAccess.get(pagePointer);
					sender.sendMessage(ChatColor.RED + value.getName() + ChatColor.AQUA +  " || " + ChatColor.RESET + value.getUnicodeCharacter());
					pagePointer++;
					if(pagePointer >= playersAccess.size()-1)
						break;
				}
				sender.sendMessage( ChatColor.AQUA + "Displaying Page " + filter+" out of " + pages);
				return true;
			case "search":
				if (!sender.hasPermission("emojichat.search")) {
					sender.sendMessage(ChatColor.RED + "You need " + ChatColor.GOLD + "emojichat.search" + ChatColor.RED + " to use this command.");
					return true;
				}
				if(args.length != 2){
					sender.sendMessage(ChatColor.RED + "Invalid Search, ensure that you are using the command right. Eg. /emojichat search dog");
					return true;
				}
				if (args[1].toLowerCase().length() < 3){
					sender.sendMessage(ChatColor.RED + "Invalid Search, to avoid too many results specify at least 3 characters for a search. Eg. /emojichat search dog");
					return true;
				}

				sender.sendMessage(ChatColor.AQUA + "Searching for likely Emoji Candidates, limiting search to 15 results... ");
				int count = 0;
				Player playerSearch = (Player) sender;
				for (Unicode unicode : plugin.getEmojiHandler().getEmojis(playerSearch)) {
					if(unicode.getName().contains(args[1].toLowerCase()) || args[1].toLowerCase().contains(unicode.getName())){
						sender.sendMessage(ChatColor.AQUA + unicode.getName() + " " + ChatColor.RESET + unicode.getUnicodeCharacter());
						count++;
					}
					if(count > 15) break;
				}
				return true;
			default:
				sender.sendMessage(ChatColor.RED + "Unknown sub-command '" + args[0] + "'. Use " + ChatColor.GOLD + "/emojichat help" + ChatColor.RED + " for help.");
				return true;
		}
	}
}
