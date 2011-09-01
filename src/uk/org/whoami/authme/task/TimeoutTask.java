package uk.org.whoami.authme.task;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import uk.org.whoami.authme.cache.auth.PlayerCache;
import uk.org.whoami.authme.settings.Messages;

public class TimeoutTask implements Runnable {
    
    private JavaPlugin plugin;
    private String name;
    private Messages m = Messages.getInstance();
    
    public TimeoutTask(JavaPlugin plugin,String name) {
        this.plugin = plugin;
        this.name = name;
    }

    @Override
    public void run() {
        if(PlayerCache.getInstance().isAuthenticated(name)) {
            return;
        }
        
        for(Player player:plugin.getServer().getOnlinePlayers()) {
            if(player.getName().toLowerCase().equals(name)) {
                player.kickPlayer(m._("Login Timeout"));
                break;
            }
        }
    }
}
