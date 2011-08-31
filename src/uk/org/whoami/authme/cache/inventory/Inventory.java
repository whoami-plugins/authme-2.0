package uk.org.whoami.authme.cache.inventory;

import org.bukkit.inventory.ItemStack;

public class Inventory {

    private ItemStack[] inventory;
    private ItemStack[] armour;

    public Inventory(ItemStack[] inventory, ItemStack[] armour) {
        this.inventory = inventory;
        this.armour = armour;
    }

    public ItemStack[] getArmour() {
        return armour;
    }

    public ItemStack[] getInventory() {
        return inventory;
    }
}
