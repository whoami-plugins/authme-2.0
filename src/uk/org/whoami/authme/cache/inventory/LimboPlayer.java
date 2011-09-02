package uk.org.whoami.authme.cache.inventory;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public class LimboPlayer {
    
    private String name;
    private ItemStack[] inventory;
    private ItemStack[] armour;
    private Location loc;
    private int timeoutTaskId = -1;

    public LimboPlayer(String name, Location loc, ItemStack[] inventory, ItemStack[] armour) {
        this.name = name;
        this.loc = loc;
        this.inventory = inventory;
        this.armour = armour;
    }

    public String getName() {
        return name;
    }
    
    public Location getLoc() {
        return loc;
    }

    public ItemStack[] getArmour() {
        return armour;
    }

    public ItemStack[] getInventory() {
        return inventory;
    }

    public int getTimeoutTaskId() {
        return timeoutTaskId;
    }

    public void setTimeoutTaskId(int timeoutTaskId) {
        this.timeoutTaskId = timeoutTaskId;
    }
}
