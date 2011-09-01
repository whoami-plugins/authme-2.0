package uk.org.whoami.authme.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;
import uk.org.whoami.authme.cache.auth.PlayerCache;
import uk.org.whoami.authme.datasource.DataSource;
import uk.org.whoami.authme.settings.Messages;

public class AuthMeBlockListener extends BlockListener {
    
    private DataSource data;
    private Messages m = Messages.getInstance();

    public AuthMeBlockListener(DataSource data) {
        this.data = data;
    }
        
    @Override
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled() || event.getPlayer() == null) {
            return;
        }
        
        Player player = event.getPlayer();
        if (PlayerCache.getInstance().isAuthenticated(player.getName().toLowerCase())) {
            return;
        }
        
        if(data.isAuthAvailable(player.getName().toLowerCase())) {
            player.sendMessage(m._("Please login with \"/login password\""));
        } else {
            player.sendMessage(m._("Please register with \"/register password\""));
        }
        
        event.setCancelled(true);
    }
    
    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled() || event.getPlayer() == null) {
            return;
        }
        
        Player player = event.getPlayer();
        if (PlayerCache.getInstance().isAuthenticated(player.getName().toLowerCase())) {
            return;
        }
        
        if(data.isAuthAvailable(player.getName().toLowerCase())) {
            player.sendMessage(m._("Please login with \"/login password\""));
        } else {
            player.sendMessage(m._("Please register with \"/register password\""));
        }
        
        event.setCancelled(true);
    }
}
