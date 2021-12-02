package com.mith.EmojiChat;

import com.mith.EmojiChat.utils.EmojiHandler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class EmojiChat extends JavaPlugin {
	/**
	 * The emoji handler that stores emoji data.
	 */
	private EmojiHandler emojiHandler;
	private static EmojiChat plugin;
	/**
	 * The EmojiChat GUI.
	 */
	EmojiChatGui emojiChatGui;
	
	@Override
	public void onEnable() {
		if (!new File(getDataFolder(), "config.yml").exists()) { // If there's not a config, make one
			saveDefaultConfig();
		}
		plugin = this;
		emojiChatGui = new EmojiChatGui(this);
		emojiHandler = new EmojiHandler(this);
		
		// Register the chat listener
		Bukkit.getPluginManager().registerEvents(new EmojiChatListener(this), this);
		
		// Register the "emojichat" and "ec" commands
		EmojiChatCommand emojiChatCommand = new EmojiChatCommand(this);
		EmojiChatTabComplete emojiChatTabComplete = new EmojiChatTabComplete();
		getCommand("emojichat").setExecutor(emojiChatCommand);
		getCommand("emojichat").setTabCompleter(emojiChatTabComplete);
		getCommand("echat").setExecutor(emojiChatCommand);
		getCommand("echat").setTabCompleter(emojiChatTabComplete);
	}
	
	@Override
	public void onDisable() {
		emojiHandler.disable();
	}
	
	/**
	 * Gets the emoji handler.
	 *
	 * @return The emoji handler.
	 */
	public EmojiHandler getEmojiHandler() {
		return emojiHandler;
	}
	public static EmojiChat getPlugin() { return plugin; }
}
