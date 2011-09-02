package uk.org.whoami.authme.cache.inventory;

import java.util.HashMap;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class LimboCache {

    private static LimboCache singleton = null;
    private HashMap<String, LimboPlayer> cache;

    private LimboCache() {
        this.cache = new HashMap<String, LimboPlayer>();
    }

    public void addLimboPlayer(Player player) {
        String name = player.getName().toLowerCase();
        Location loc = player.getLocation();
        ItemStack[] inv = player.getInventory().getContents();
        ItemStack[] arm = player.getInventory().getArmorContents();

        cache.put(player.getName().toLowerCase(), new LimboPlayer(name, loc, inv, arm));
    }

    public void deleteLimboPlayer(String name) {
        cache.remove(name);
    }

    public LimboPlayer getLimboPlayer(String name) {
        return cache.get(name);
    }

    public boolean hasLimboPlayer(String name) {
        return cache.containsKey(name);
    }

    public static LimboCache getInstance() {
        if (singleton == null) {
            singleton = new LimboCache();
        }
        return singleton;
    }
}
