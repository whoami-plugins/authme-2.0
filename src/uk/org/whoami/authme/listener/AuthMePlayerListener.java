package uk.org.whoami.authme.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerMoveEvent;
import uk.org.whoami.authme.cache.auth.PlayerCache;
import uk.org.whoami.authme.settings.Settings;

public class AuthMePlayerListener extends PlayerListener {
    
    private Settings settings;

    public AuthMePlayerListener(Settings settings) {
        this.settings = settings;
    }
    
    @Override
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled() || event.getPlayer() == null) {
            return;
        }
        
        Player player = event.getPlayer();
        if (PlayerCache.getInstance().isAuthenticated(player.getName().toLowerCase())) {
            return;
        }

        String cmd = event.getMessage().split(" ")[0];
        if (cmd.equalsIgnoreCase("/login") || cmd.equalsIgnoreCase("/register")) {
            return;
        }

        event.setMessage("/notloggedin");
        event.setCancelled(true);
    }

    @Override
    public void onPlayerChat(PlayerChatEvent event) {
        if (event.isCancelled() || event.getPlayer() == null || settings.isChatAllowed()) {
            return;
        }
        
        Player player = event.getPlayer();
        if (PlayerCache.getInstance().isAuthenticated(player.getName().toLowerCase())) {
            return;
        }
        
        event.setMessage("/notloggedin");
        event.setCancelled(true);
    }
    
    @Override
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.isCancelled() || event.getPlayer() == null || settings.isMovementAllowed()) {
            return;
        }
        
        Player player = event.getPlayer();
        if (PlayerCache.getInstance().isAuthenticated(player.getName().toLowerCase())) {
            return;
        }
        
        event.setCancelled(true);
    }
    
    @Override
    public void onPlayerLogin(PlayerLoginEvent event) {
        if(event.getResult() != Result.ALLOWED || event.getPlayer() == null) {
            return;
        }
    }
}
