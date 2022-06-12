package com.mith.EmojiChat;

import com.google.common.base.CharMatcher;
import com.mith.EmojiChat.tasks.AnvilTask;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

class EmojiChatListener implements Listener {
	/**
	 * EmojiChat main class instance.
	 */
	private final EmojiChat plugin;
	
	/**
	 * Creates the EmojiChat listener class with the main class instance.
	 *
	 * @param plugin The EmojiChat main class instance.
	 */
	EmojiChatListener(EmojiChat plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	void onChat(AsyncPlayerChatEvent event) {
		if (!event.getPlayer().hasPermission("emojichat.use.chat")){
			if(!event.getPlayer().hasPermission("emojichat.use")){
				return;
			}
		}
		System.out.println(event.getFormat());
		String message = event.getMessage();
		
		// Checks if the user disabled shortcuts via /emojichat toggle
		if (plugin.getEmojiHandler().hasShortcutsOn(event.getPlayer())) {
			message = plugin.getEmojiHandler().translateShorthand(message);
		}

		if (!event.getPlayer().hasPermission("emojichat.use.chat")){
			if(!event.getPlayer().hasPermission("emojichat.use")){
				return;
			}
		}

		// Replace shortcuts with emojis
		message = plugin.getEmojiHandler().toEmoji(event.getPlayer(), message);
		
		// If the message contains a disabled character
		if (!event.getPlayer().hasPermission("emojichat.bypass") && plugin.getEmojiHandler().containsDisabledCharacter(message)) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.RED + "Oops! You can't use disabled emoji characters!");
			return;
		}

		event.setMessage(message);
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	void onSignChange(SignChangeEvent event) {
		if (!event.getPlayer().hasPermission("emojichat.use.sign")){
			if(!event.getPlayer().hasPermission("emojichat.use")){
				return;
			}
		}

		
		if (!plugin.getConfig().getBoolean("emojis-on-signs"))
			return;
		
		for (int i = 0; i < 4; i++) {
			String line = event.getLine(i);
			
			// Checks if the user disabled shortcuts via /emojichat toggle
			if (plugin.getEmojiHandler().hasShortcutsOn(event.getPlayer())) {
				line = plugin.getEmojiHandler().translateShorthand(line);
			}

			line = plugin.getEmojiHandler().toEmojiFromSign(event.getPlayer(),line);

			// If the message contains a disabled character
			if (!event.getPlayer().hasPermission("emojichat.bypass") && plugin.getEmojiHandler().containsDisabledCharacter(line)) {
				event.setCancelled(true);
				event.getPlayer().sendMessage(ChatColor.RED + "Oops! You can't use disabled emoji characters!");
				return;
			}
			
			event.setLine(i, line);
		}
	}

	@EventHandler
	public void onAnvilGUIClick(InventoryClickEvent event) {
		if (event.getInventory().getType() != InventoryType.ANVIL || !(event.getWhoClicked() instanceof Player)) {
			return;
		}

		Player player = (Player)event.getWhoClicked();

		if (!player.hasPermission("emojichat.use.anvil")){
			if(!player.hasPermission("emojichat.use")){
				return;
			}
		}

		AnvilInventory inv = (AnvilInventory)event.getInventory();
		AnvilTask task = AnvilTask.getTask(inv);

		if (task == null)
			task = new AnvilTask(inv, player);
		if (event.getRawSlot() == 2) {
			ItemStack translatedItem = plugin.getEmojiHandler()
						.translateAnvilTaskToAnvilInvetory(player,inv,task);
			event.setCurrentItem(translatedItem);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	void onCommandPreProcess(PlayerCommandPreprocessEvent event) {
		if (!event.getPlayer().hasPermission("emojichat.use.command")){
			if(!event.getPlayer().hasPermission("emojichat.use")){
				return;
			}
		}

		Player player = event.getPlayer();

		if (!plugin.getConfig().getBoolean("emojis-in-commands")) // Feature is disabled
			return;
		
		String command = event.getMessage();
		
		// Checks if the user disabled shortcuts via /emojichat toggle
		if (plugin.getEmojiHandler().hasShortcutsOn(event.getPlayer())) {
			command = plugin.getEmojiHandler().translateShorthand(command);
		}

		// Replace shortcuts with emojis
		command = plugin.getEmojiHandler().toEmoji(player, command);

		// If the message contains a disabled character
		if (!event.getPlayer().hasPermission("emojichat.bypass") && plugin.getEmojiHandler().containsDisabledCharacter(command)) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.RED + "Oops! You can't use disabled emoji characters!");
			return;
		}
		
		event.setMessage(command);
	}
	
	@EventHandler
	void onInventoryClick(InventoryClickEvent event) {
		if (event.getView().getTitle().contains("Emoji List")) {
			event.setCancelled(true);
			Player player =(Player) event.getWhoClicked();
			if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.BLACK_STAINED_GLASS_PANE
					&& event.getCurrentItem().hasItemMeta()
					&& event.getCurrentItem().getItemMeta().hasDisplayName()) { // Make sure the item clicked is a page change item
				try {
					int currentPage = Integer.parseInt(event.getView().getTitle().split(" ")[3]) - 1; // Get the page number from the title
					
					if (event.getCurrentItem().getItemMeta().getDisplayName().contains("<-")) { // Back button
						event.getWhoClicked().openInventory(plugin.emojiChatGui.getInventory(player,currentPage - 1));
					} else { // Next button
						event.getWhoClicked().openInventory(plugin.emojiChatGui.getInventory(player,currentPage + 1));
					}
				} catch (Exception e) { // Something happened, not sure what, so just reset their page to 0
					event.getWhoClicked().openInventory(plugin.emojiChatGui.getInventory(player,0));
				}
			}
		}
	}
}
