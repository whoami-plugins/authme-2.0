package uk.org.whoami.authme.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;
import uk.org.whoami.authme.cache.auth.PlayerCache;
import uk.org.whoami.authme.datasource.DataSource;
import uk.org.whoami.authme.settings.Settings;

public class AuthMeBlockListener extends BlockListener {
    
    private DataSource data;
    private Settings settings = Settings.getInstance();    

    public AuthMeBlockListener(DataSource data) {
        this.data = data;
    }
        
    @Override
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled() || event.getPlayer() == null) {
            return;
        }
        
        Player player = event.getPlayer();
        String name = player.getName().toLowerCase();
        
        if (PlayerCache.getInstance().isAuthenticated(name)) {
            return;
        }
        
        if(data.isAuthAvailable(name)) {
            if (!settings.isForcedRegistrationEnabled()) {
                return;
            }
        }
               
        event.setCancelled(true);
    }
    
    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled() || event.getPlayer() == null) {
            return;
        }
        
        Player player = event.getPlayer();
        String name = player.getName().toLowerCase();
        
        if (PlayerCache.getInstance().isAuthenticated(player.getName().toLowerCase())) {
            return;
        }
        
        if(data.isAuthAvailable(name)) {
            if (!settings.isForcedRegistrationEnabled()) {
                return;
            }
        }
                
        event.setCancelled(true);
    }
}
