package uk.org.whoami.authme.listener;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityTargetEvent;
import uk.org.whoami.authme.cache.auth.PlayerCache;
import uk.org.whoami.authme.datasource.DataSource;
import uk.org.whoami.authme.settings.Settings;

public class AuthMeEntityListener extends EntityListener {

    private DataSource data;
    private Settings settings = Settings.getInstance();

    public AuthMeEntityListener(DataSource data) {
        this.data = data;
    }

    @Override
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }

        Player player = (Player) entity;
        String name = player.getName().toLowerCase();

        if (PlayerCache.getInstance().isAuthenticated(name)) {
            return;
        }

        if (!data.isAuthAvailable(name)) {
            if (!settings.isForcedRegistrationEnabled()) {
                return;
            }
        }

        event.setCancelled(true);
    }

    @Override
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }

        Player player = (Player) entity;
        String name = player.getName().toLowerCase();

        if (PlayerCache.getInstance().isAuthenticated(name)) {
            return;
        }

        if (!data.isAuthAvailable(name)) {
            if (!settings.isForcedRegistrationEnabled()) {
                return;
            }
        }

        event.setCancelled(true);
    }
}
