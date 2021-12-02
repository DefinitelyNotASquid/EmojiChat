package com.mith.EmojiChat.tasks;


import java.util.HashMap;

import com.mith.EmojiChat.EmojiChat;
import org.bukkit.entity.Player;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.scheduler.BukkitRunnable;

public class AnvilTask
        extends BukkitRunnable
{
    private static HashMap<AnvilInventory, AnvilTask> anvilTasks = new HashMap();

    private AnvilInventory inv;
    private Player player;

    public AnvilTask(AnvilInventory inv, Player player) {
        this.inv = inv;
        this.player = player;
        anvilTasks.put(inv, this);
        runTaskTimer(EmojiChat.getPlugin(), 1L, 3L);
    }


    public void run() {
        if (this.inv.getViewers().size() == 0){
            cancel();
        }
        EmojiChat.getPlugin().getEmojiHandler().translateAnvilTaskToAnvilInvetory(this.player, this.inv, this);
    }


    public static AnvilTask getTask(AnvilInventory inv) { return (AnvilTask)anvilTasks.get(inv); }
}
