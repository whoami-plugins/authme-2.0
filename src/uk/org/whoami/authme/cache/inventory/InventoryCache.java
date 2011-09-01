package uk.org.whoami.authme.cache.inventory;

import java.util.HashMap;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class InventoryCache {
    
    private static InventoryCache singleton = null;
    private HashMap<String,Inventory> cache;

    private InventoryCache() {
        this.cache = new HashMap<String,Inventory>();
    }
    
    public void addInventory(Player player) {
        ItemStack[] inv = player.getInventory().getContents();
        ItemStack[] arm = player.getInventory().getArmorContents();
        
        cache.put(player.getName().toLowerCase(), new Inventory(inv,arm));
    }
    
    public void deleteInventory(String name) {
        cache.remove(name);
    }
        
    public Inventory getInventory(String name) {
        return cache.get(name);
    }
    
    public boolean hasInventory(String name) {
        return cache.containsKey(name);
    }
    
    public static InventoryCache getInstance() {
        if(singleton == null) {
            singleton = new InventoryCache();
        }
        return singleton;
    }
}
