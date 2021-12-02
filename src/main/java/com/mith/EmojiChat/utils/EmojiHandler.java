package com.mith.EmojiChat.utils;

import com.mith.EmojiChat.tasks.AnvilTask;
import com.mith.EmojiChat.EmojiChat;
import com.mith.UnicodeApi.Objects.Unicode;
import com.mith.UnicodeApi.UnicodeApi;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;


public class EmojiHandler {

	/**
	 * Shortcuts for the emojis, if specified.
	 */
	private final HashMap<String, String> shortcuts;

	/**
	 * A list of users (by UUID) who turned shortcuts off.
	 */
	private List<UUID> shortcutsOff;
	/**
	 * EmojiChat main class instance.
	 */
	private final EmojiChat plugin;
	
	/**
	 * Creates the emoji handler with the main class instance.
	 *
	 * @param plugin The EmojiChat main class instance.
	 */
	public EmojiHandler(EmojiChat plugin) {
		this.plugin = plugin;

		shortcuts = new HashMap<>();
		shortcutsOff = new ArrayList<>();
		
		load(plugin);
	}

	/***
	 * Gets Emojis
	 * @param player
	 * @return
	 */
	public List<Unicode> getEmojis(Player player) {
		if(player.hasPermission("emojichat.bypass")){
			return UnicodeApi.getPlugin().instanceManager.getGlobalUnicode();
		}
		return UnicodeApi.getPlugin().instanceManager.getGlobalUnicodeFiltered();
	}
	
	/**
	 * Gets the {@link #shortcuts} map.
	 *
	 * @return The {@link #shortcuts} map.
	 */
	public HashMap<String, String> getShortcuts() {
		return shortcuts;
	}
	
	/**
	 * Checks if the specified player has emoji shortcuts off.
	 *
	 * @param player The player to check.
	 * @return True if the player has shortcuts off, false otherwise.
	 */
	public boolean hasShortcutsOn(Player player) {
		return !shortcutsOff.contains(player.getUniqueId());
	}
	
	/**
	 * Toggles emoji shortcut use on/off for the specified player.
	 *
	 * @param player The player to toggle emoji shortcuts on/off for.
	 */
	public void toggleShortcutsOn(Player player) {
		if (shortcutsOff.contains(player.getUniqueId())) {
			shortcutsOff.remove(player.getUniqueId());
		} else {
			shortcutsOff.add(player.getUniqueId());
		}
	}
	
	/**
	 * Loads the emoji shortcuts from the config.
	 *
	 * @param config The config to load emoji shortcuts from.
	 */
	private void loadShortcuts(FileConfiguration config) {
		for (String key : config.getConfigurationSection("shortcuts").getKeys(false)) { // Gets all of the headers/keys in the shortcuts section
			for (String shortcutListItem : config.getStringList("shortcuts." + key)) { // Gets all of the shortcuts for the key
				shortcuts.put(shortcutListItem, ":" + key + ":");
			}
		}
	}

	
	/**
	 * Clears the Shortcuts and shortcut user maps.
	 */
	public void disable() {
		shortcuts.clear();
		shortcutsOff.clear();
	}
	
	/**
	 * Loads the emoji handler data.
	 *
	 * @param plugin The EmojiChat main class instance.
	 */
	public void load(EmojiChat plugin) {
		disable();
		loadShortcuts(plugin.getConfig());
	}
	
	/**
	 * Converts the specified message's shortcuts (i.e. :100:) to emoji.
	 *
	 * @param message The message to convert.
	 * @return The converted message.
	 */
	public String toEmoji(Player player,String message) {
		for (Unicode key : getEmojis(player)) {
			message = message.replace(":"+key.getName()+":", ChatColor.RESET + key.getUnicodeCharacter());
		}
		return message;
	}

	/***
	 * Converts the specified line's shortcuts (i.e. :100:) to emoji from sign.
	 * @param player
	 * @param line
	 * @return
	 */
	public String toEmojiFromSign(Player player, String line) {
		for (Unicode key : getEmojis(player)) {
			line = line.replace(":" + key.getName() + ":", ChatColor.WHITE + "" + key.getUnicodeCharacter() + ChatColor.BLACK); // Sets the emoji color to white for correct coloring
		}
		return line;
	}

	public static String toColors(String input) {
		return input == null ? null : ChatColor.translateAlternateColorCodes('&', input);
	}

	public static String removeColors(String input) {
		return input == null ? null : ChatColor.stripColor(input.replaceAll("(&([A-FK-Oa-fk-or0-9]))", ""));
	}

	public ItemStack translateAnvilTaskToAnvilInvetory(Player player, AnvilInventory inv, AnvilTask task){
		ItemStack outputItem = inv.getItem(2);
		ItemStack inputItem = inv.getItem(0);
		if (inv.getType() != InventoryType.ANVIL) {
			return outputItem;
		}

		if (outputItem == null || !outputItem.hasItemMeta()){
			return outputItem;
		}

		if(inputItem == null){
			return outputItem;
		}

		String resultName = outputItem.getItemMeta().getDisplayName();
		ItemStack is = outputItem;
		ItemMeta im = outputItem.getItemMeta();


		im.setDisplayName(resultName);
		is.setItemMeta(im);
		is = toEmojiFromItem(is, player);

		// If the message contains a disabled character
		if (!player.hasPermission("emojichat.bypass") && plugin.getEmojiHandler().containsDisabledCharacter(is.getItemMeta().getDisplayName())) {
				inv.getViewers().clear();
				player.sendMessage(ChatColor.RED + "Oops! You can't use disabled emoji characters!");
				player.getOpenInventory().close();
				return outputItem;
		}

		return is;
	}

	/**
	 * Converts and item stack and changes its current name depending if it contains an emoji or not.
	 * @param item
	 * @return
	 */
	public ItemStack toEmojiFromItem(ItemStack item, Player player){
		ItemMeta itemMeta = item.getItemMeta();
		String ItemDisplayName = itemMeta.getDisplayName();

		if (plugin.getEmojiHandler().hasShortcutsOn(player)) {
			ItemDisplayName = plugin.getEmojiHandler().translateShorthand(ItemDisplayName);
		}

		for (Unicode key : getEmojis(player)) {
			ItemDisplayName = ItemDisplayName.replace(":"+key.getName()+":", ChatColor.WHITE + "" + key.getUnicodeCharacter() + ChatColor.BLACK); // Sets the emoji color to white for correct coloring
		}

		itemMeta.setDisplayName(toColors(ItemDisplayName));
		item.setItemMeta(itemMeta);
		return item;
	}

	/**
	 * Replaces shorthand ("shortcuts" in config) with correct emoji shortcuts.
	 *
	 * @param message The original message.
	 * @return The message with correct emoji shortcuts.
	 */
	public String translateShorthand(String message) {
		for (String key : plugin.getEmojiHandler().getShortcuts().keySet()) {
			message = message.replace(key, plugin.getEmojiHandler().getShortcuts().get(key));
		}
		return message;
	}
	
	/**
	 * Checks if the specified message contains a disabled character, if enabled.
	 *
	 * @param message The message to check.
	 * @return True if the message contains a disabled character, false otherwise.
	 */
	public boolean containsDisabledCharacter(String message) {
		for (Unicode disabledCharacter : UnicodeApi.getPlugin().instanceManager.getBlacklistUnicode()) {
			if (message.contains(disabledCharacter.getUnicodeCharacter())) { // Message contains a disabled character
				return true;
			}
		}
		return false;
	}
}
