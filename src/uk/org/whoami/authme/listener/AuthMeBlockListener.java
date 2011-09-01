package uk.org.whoami.authme.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;
import uk.org.whoami.authme.cache.auth.PlayerCache;

public class AuthMeBlockListener extends BlockListener {
    
    @Override
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled() || event.getPlayer() == null) {
            return;
        }
        
        Player player = event.getPlayer();
        if (PlayerCache.getInstance().isAuthenticated(player.getName().toLowerCase())) {
            return;
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
        
        event.setCancelled(true);
    }
}
