package uk.org.whoami.authme.task;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import uk.org.whoami.authme.cache.auth.PlayerCache;

public class MessageTask implements Runnable {

    private JavaPlugin plugin;
    private String name;
    private String msg;
    private int interval;

    public MessageTask(JavaPlugin plugin, String name, String msg, int interval) {
        this.plugin = plugin;
        this.name = name;
        this.msg = msg;
        this.interval = interval;
    }

    @Override
    public void run() {
        if (PlayerCache.getInstance().isAuthenticated(name)) {
            return;
        }

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.getName().toLowerCase().equals(name)) {
                player.sendMessage(msg);
                
                BukkitScheduler sched = plugin.getServer().getScheduler();
                sched.scheduleSyncDelayedTask(plugin, this, interval*20);
            }
        }
    }
}
