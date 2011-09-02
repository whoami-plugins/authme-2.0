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
import org.bukkit.scheduler.BukkitScheduler;
import uk.org.whoami.authme.cache.auth.PlayerAuth;
import uk.org.whoami.authme.cache.auth.PlayerCache;
import uk.org.whoami.authme.cache.inventory.LimboPlayer;
import uk.org.whoami.authme.cache.inventory.LimboCache;
import uk.org.whoami.authme.datasource.DataSource;
import uk.org.whoami.authme.settings.Messages;
import uk.org.whoami.authme.settings.Settings;
import uk.org.whoami.authme.task.MessageTask;
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
        Player player = event.getPlayer();
        String name = player.getName().toLowerCase();

        if (PlayerCache.getInstance().isAuthenticated(name)) {
            return;
        }

        if (!data.isAuthAvailable(name)) {
            if (!settings.isForcedRegistrationEnabled()) {
                return;
            }
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

        Player player = event.getPlayer();
        String name = player.getName().toLowerCase();
        
        if (PlayerCache.getInstance().isAuthenticated(name)) {
            return;
        }

        if (data.isAuthAvailable(name)) {
            player.sendMessage(m._("login_msg"));
        } else {
            if (!settings.isForcedRegistrationEnabled()) {
                return;
            }
            if (settings.isChatAllowed()) {
                return;
            }
            player.sendMessage(m._("reg_msg"));
        }
        event.setCancelled(true);
    }

    @Override
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.isCancelled() || event.getPlayer() == null) {
            return;
        }

        Player player = event.getPlayer();
        String name = player.getName().toLowerCase();
        
        if (PlayerCache.getInstance().isAuthenticated(name)) {
            return;
        }

        if (!data.isAuthAvailable(name)) {
            if (!settings.isForcedRegistrationEnabled()) {
                return;
            }

            if (settings.isMovementAllowed()) {
                return;
            }
        }
        event.setTo(event.getFrom());

        //event.setCancelled(true);
    }

    @Override
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (event.getResult() != Result.ALLOWED || event.getPlayer() == null) {
            return;
        }

        Player player = event.getPlayer();
        String name = player.getName().toLowerCase();

        //Remove doubles from premises
        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            if (onlinePlayer.getName().equals(player.getName())) {
                event.disallow(Result.KICK_OTHER, m._("same_nick"));
            }
        }

        if (settings.isKickNonRegisteredEnabled()) {
            if (!data.isAuthAvailable(name)) {
                event.disallow(Result.KICK_OTHER, m._("reg_only"));
                return;
            }
        }
    }

    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (event.getPlayer() == null) {
            return;
        }

        Player player = event.getPlayer();
        String name = player.getName().toLowerCase();
        String ip = player.getAddress().getAddress().getHostAddress();

        if (PlayerCache.getInstance().isAuthenticated(name)) {
            return;
        }

        if (data.isAuthAvailable(name)) {
            if (settings.isSessionsEnabled()) {
                PlayerAuth auth = data.getAuth(name);
                if (auth.getNickname().equals(name) && auth.getIp().equals(ip)) {
                    PlayerCache.getInstance().addPlayer(auth);
                    player.sendMessage(m._("valid_session"));
                    return;
                }
            }
        } else {
            if (!settings.isForcedRegistrationEnabled()) {
                return;
            }
        }

        LimboCache.getInstance().addLimboPlayer(player);
        player.getInventory().setArmorContents(new ItemStack[0]);
        player.getInventory().setContents(new ItemStack[36]);
        player.teleport(player.getWorld().getSpawnLocation());

        String msg = data.isAuthAvailable(name) ? m._("login_msg") : m._("reg_msg");
        int time = settings.getRegistrationTimeout() * 20;
        int msgInterval = settings.getWarnMessageInterval();
        BukkitScheduler sched = plugin.getServer().getScheduler();
        int id = sched.scheduleSyncDelayedTask(plugin, new TimeoutTask(plugin, name), time);
        sched.scheduleSyncDelayedTask(plugin, new MessageTask(plugin, name, msg, msgInterval));
        LimboCache.getInstance().getLimboPlayer(name).setTimeoutTaskId(id);
    }

    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (event.getPlayer() == null) {
            return;
        }
        Player player = event.getPlayer();
        String name = player.getName().toLowerCase();
        if (LimboCache.getInstance().hasLimboPlayer(name)) {
            LimboPlayer limbo = LimboCache.getInstance().getLimboPlayer(name);
            player.getInventory().setArmorContents(limbo.getArmour());
            player.getInventory().setContents(limbo.getInventory());
            player.teleport(limbo.getLoc());
            plugin.getServer().getScheduler().cancelTask(limbo.getTimeoutTaskId());
            LimboCache.getInstance().deleteLimboPlayer(name);
        }
        PlayerCache.getInstance().removePlayer(name);
    }

    @Override
    public void onPlayerKick(PlayerKickEvent event) {
        if (event.getPlayer() == null) {
            return;
        }

        Player player = event.getPlayer();
        String name = player.getName().toLowerCase();
        if (LimboCache.getInstance().hasLimboPlayer(name)) {
            LimboPlayer limbo = LimboCache.getInstance().getLimboPlayer(name);
            player.getInventory().setArmorContents(limbo.getArmour());
            player.getInventory().setContents(limbo.getInventory());
            player.teleport(limbo.getLoc());
            plugin.getServer().getScheduler().cancelTask(limbo.getTimeoutTaskId());
            LimboCache.getInstance().deleteLimboPlayer(name);
        }
        PlayerCache.getInstance().removePlayer(name);
    }

    @Override
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (event.isCancelled() || event.getPlayer() == null) {
            return;
        }

        Player player = event.getPlayer();
        String name = player.getName().toLowerCase();
        
        if (PlayerCache.getInstance().isAuthenticated(player.getName().toLowerCase())) {
            return;
        }
        
        if(!data.isAuthAvailable(name)) {
            if (!settings.isForcedRegistrationEnabled()) {
                return;
            }
        }
        
        event.setCancelled(true);
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.isCancelled() || event.getPlayer() == null) {
            return;
        }

        Player player = event.getPlayer();
        String name = player.getName().toLowerCase();
        
        if (PlayerCache.getInstance().isAuthenticated(player.getName().toLowerCase())) {
            return;
        }
        
        if(!data.isAuthAvailable(name)) {
            if (!settings.isForcedRegistrationEnabled()) {
                return;
            }
        }
        
        event.setCancelled(true);
    }
}
