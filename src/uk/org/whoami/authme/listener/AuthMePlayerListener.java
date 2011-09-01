package uk.org.whoami.authme.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import uk.org.whoami.authme.cache.auth.PlayerAuth;
import uk.org.whoami.authme.cache.auth.PlayerCache;
import uk.org.whoami.authme.cache.inventory.Inventory;
import uk.org.whoami.authme.cache.inventory.InventoryCache;
import uk.org.whoami.authme.datasource.DataSource;
import uk.org.whoami.authme.settings.Messages;
import uk.org.whoami.authme.settings.Settings;
import uk.org.whoami.authme.task.TimeoutTask;

public class AuthMePlayerListener extends PlayerListener {

    private Settings settings;
    private Messages m;
    private JavaPlugin plugin;
    private DataSource data;

    public AuthMePlayerListener(JavaPlugin plugin, DataSource data) {
        this.settings = Settings.getInstance();
        this.m = Messages.getInstance();
        this.data = data;
        this.plugin = plugin;
    }

    @Override
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled() || event.getPlayer() == null) {
            return;
        }

        if (!settings.isForcedRegistrationEnabled()) {
            return;
        }

        Player player = event.getPlayer();
        if (PlayerCache.getInstance().isAuthenticated(player.getName().toLowerCase())) {
            return;
        }

        String cmd = event.getMessage().split(" ")[0];
        if (cmd.equalsIgnoreCase("/login") || cmd.equalsIgnoreCase("/register")) {
            return;
        }

        event.setMessage("/notloggedin");
        event.setCancelled(true);
    }

    @Override
    public void onPlayerChat(PlayerChatEvent event) {
        if (event.isCancelled() || event.getPlayer() == null) {
            return;
        }

        if (!settings.isForcedRegistrationEnabled()) {
            return;
        }

        if (settings.isChatAllowed()) {
            return;
        }

        Player player = event.getPlayer();
        if (PlayerCache.getInstance().isAuthenticated(player.getName().toLowerCase())) {
            return;
        }

        if (data.isAuthAvailable(player.getName().toLowerCase())) {
            player.sendMessage(m._("Please login with \"/login password\""));
        } else {
            player.sendMessage(m._("Please register with \"/register password\""));
        }

        event.setMessage("/notloggedin");
        event.setCancelled(true);
    }

    @Override
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.isCancelled() || event.getPlayer() == null) {
            return;
        }

        if (!settings.isForcedRegistrationEnabled()) {
            return;
        }

        if (settings.isMovementAllowed()) {
            return;
        }

        Player player = event.getPlayer();
        if (PlayerCache.getInstance().isAuthenticated(player.getName().toLowerCase())) {
            return;
        }
        
        event.setTo(event.getFrom());
                
        //event.setCancelled(true);
    }

    @Override
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (event.getResult() != Result.ALLOWED || event.getPlayer() == null) {
            return;
        }

        if (!settings.isForcedRegistrationEnabled()) {
            return;
        }

        Player player = event.getPlayer();
        if (PlayerCache.getInstance().isAuthenticated(player.getName().toLowerCase())) {
            return;
        }

        if (!settings.isKickNonRegisteredEnabled()) {
            return;
        }

        event.disallow(Result.KICK_BANNED, m._("Registered players only! Please visit http://example.com to register"));
    }

    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (event.getPlayer() == null) {
            return;
        }

        Player player = event.getPlayer();
        String name = player.getName().toLowerCase();
        String ip = player.getAddress().getAddress().getHostAddress();
        if (!settings.isForcedRegistrationEnabled() && !data.isAuthAvailable(name)) {
            return;
        }

        if (PlayerCache.getInstance().isAuthenticated(player.getName().toLowerCase())) {
            return;
        }

        if (settings.isSessionsEnabled()) {
            if (data.isAuthAvailable(name)) {
                PlayerAuth auth = data.getAuth(name);
                if (auth.getNickname().equals(name) && auth.getIp().equals(ip)) {
                    PlayerCache.getInstance().addPlayer(auth);
                    player.sendMessage(m._("Valid session detected: AutoLogin"));
                    return;
                }
            }
        }

        InventoryCache.getInstance().addInventory(player);
        player.getInventory().setArmorContents(new ItemStack[0]);
        player.getInventory().setContents(new ItemStack[36]);

        if (data.isAuthAvailable(name)) {
            player.sendMessage(m._("Please login with \"/login password\""));
        } else {
            player.sendMessage(m._("Please register with \"/register password\""));
        }

        int time = settings.getRegistrationTimeout() * 20;
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new TimeoutTask(plugin, name), time);
    }

    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (event.getPlayer() == null) {
            return;
        }
        Player player = event.getPlayer();
        String name = player.getName().toLowerCase();
        if (InventoryCache.getInstance().hasInventory(name)) {
            Inventory inv = InventoryCache.getInstance().getInventory(name);
            player.getInventory().setArmorContents(inv.getArmour());
            player.getInventory().setContents(inv.getInventory());
            InventoryCache.getInstance().deleteInventory(name);
        }
        PlayerCache.getInstance().removePlayer(player.getName().toLowerCase());
    }

    @Override
    public void onPlayerKick(PlayerKickEvent event) {
        if (event.getPlayer() == null) {
            return;
        }

        Player player = event.getPlayer();
        String name = player.getName().toLowerCase();
        if (InventoryCache.getInstance().hasInventory(name)) {
            Inventory inv = InventoryCache.getInstance().getInventory(name);
            player.getInventory().setArmorContents(inv.getArmour());
            player.getInventory().setContents(inv.getInventory());
            InventoryCache.getInstance().deleteInventory(name);
        }
        PlayerCache.getInstance().removePlayer(player.getName().toLowerCase());
    }

    @Override
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (event.isCancelled() || event.getPlayer() == null) {
            return;
        }

        Player player = event.getPlayer();
        if (PlayerCache.getInstance().isAuthenticated(player.getName().toLowerCase())) {
            return;
        }

        if (data.isAuthAvailable(player.getName().toLowerCase())) {
            player.sendMessage(m._("Please login with \"/login password\""));
        } else {
            player.sendMessage(m._("Please register with \"/register password\""));
        }
        event.setCancelled(true);
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.isCancelled() || event.getPlayer() == null) {
            return;
        }

        Player player = event.getPlayer();
        if (PlayerCache.getInstance().isAuthenticated(player.getName().toLowerCase())) {
            return;
        }

        if (data.isAuthAvailable(player.getName().toLowerCase())) {
            player.sendMessage(m._("Please login with \"/login password\""));
        } else {
            player.sendMessage(m._("Please register with \"/register password\""));
        }
        event.setCancelled(true);
    }
}
