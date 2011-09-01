package uk.org.whoami.authme.listener;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityTargetEvent;
import uk.org.whoami.authme.cache.auth.PlayerCache;

public class AuthMeEntityListener extends EntityListener {
    
    @Override
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Entity entity = event.getEntity();
        if(!(entity instanceof Player)) {
            return;
        }

        Player player = (Player) entity;
        if (!PlayerCache.getInstance().isAuthenticated(player.getName().toLowerCase())) {
            event.setCancelled(true);
        }  
    }
    
    @Override
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Entity entity = event.getEntity();
        if(!(entity instanceof Player)) {
            return;
        }

        Player player = (Player) entity;
        if (PlayerCache.getInstance().isAuthenticated(player.getName().toLowerCase())) {
            event.setCancelled(true);
        }   
    }
}
