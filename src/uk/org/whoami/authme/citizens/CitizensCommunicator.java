
package uk.org.whoami.authme.citizens;

import net.citizensnpcs.api.CitizensManager;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class CitizensCommunicator {

    public static boolean isNPC(Entity player) {
        PluginManager pm = player.getServer().getPluginManager();
        Plugin plugin = pm.getPlugin("Citizens");
        if (plugin != null) {
            return CitizensManager.isNPC(player);
        } else {
            return false;
        }
    }
}
