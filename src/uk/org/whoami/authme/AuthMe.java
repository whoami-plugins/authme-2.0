package uk.org.whoami.authme;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import uk.org.whoami.authme.cache.auth.PlayerAuth;
import uk.org.whoami.authme.cache.auth.PlayerCache;
import uk.org.whoami.authme.cache.inventory.LimboPlayer;
import uk.org.whoami.authme.cache.inventory.LimboCache;
import uk.org.whoami.authme.datasource.CacheDataSource;
import uk.org.whoami.authme.datasource.DataSource;
import uk.org.whoami.authme.datasource.FileDataSource;
import uk.org.whoami.authme.datasource.MySQLDataSource;
import uk.org.whoami.authme.listener.AuthMeBlockListener;
import uk.org.whoami.authme.listener.AuthMeEntityListener;
import uk.org.whoami.authme.listener.AuthMePlayerListener;
import uk.org.whoami.authme.security.PasswordSecurity;
import uk.org.whoami.authme.settings.Messages;
import uk.org.whoami.authme.settings.Settings;
import uk.org.whoami.authme.task.MessageTask;
import uk.org.whoami.authme.task.TimeoutTask;

public class AuthMe extends JavaPlugin {

    private DataSource database;
    private Settings settings;
    private Messages m;
    private PasswordSecurity pws;

    @Override
    public void onEnable() {
        this.settings = Settings.getInstance();
        if (settings.getDataSource().equals("file")) {
            try {
                database = new FileDataSource();
            } catch (IOException ex) {
                ConsoleLogger.showError(ex.getMessage());
                this.getServer().getPluginManager().disablePlugin(this);
                return;
            }
        } else if (settings.getDataSource().equals("mysql")) {
            try {
                database = new MySQLDataSource();
            } catch (ClassNotFoundException ex) {
                ConsoleLogger.showError(ex.getMessage());
                this.getServer().getPluginManager().disablePlugin(this);
                return;
            } catch (SQLException ex) {
                ConsoleLogger.showError(ex.getMessage());
                this.getServer().getPluginManager().disablePlugin(this);
                return;
            }
        } else {
            ConsoleLogger.showError("Unknown database type");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (settings.isCachingEnabled()) {
            database = new CacheDataSource(database);
        }

        m = Messages.getInstance();

        try {
            pws = new PasswordSecurity(settings.getPasswordHash());
        } catch (NoSuchAlgorithmException ex) {
            ConsoleLogger.showError(ex.getMessage());
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        AuthMePlayerListener playerListener = new AuthMePlayerListener(this, database);
        AuthMeBlockListener blockListener = new AuthMeBlockListener(database);
        AuthMeEntityListener entityListener = new AuthMeEntityListener(database);

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, playerListener,
                Priority.Lowest, this);
        pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener,
                Priority.Lowest, this);
        pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener,
                Priority.Lowest, this);
        pm.registerEvent(Event.Type.PLAYER_LOGIN, playerListener,
                Priority.Lowest, this);
        pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener,
                Priority.Lowest, this);
        pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener,
                Priority.Lowest, this);
        pm.registerEvent(Event.Type.PLAYER_KICK, playerListener,
                Priority.Lowest, this);
        pm.registerEvent(Event.Type.PLAYER_PICKUP_ITEM, playerListener,
                Priority.Lowest, this);
        pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener,
                Priority.Lowest, this);
        pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener,
                Priority.Lowest, this);
        pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener,
                Priority.Lowest, this);
        pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener,
                Priority.Lowest, this);
        pm.registerEvent(Event.Type.ENTITY_TARGET, entityListener,
                Priority.Lowest, this);
        ConsoleLogger.info("Authme " + this.getDescription().getVersion() + " enabled");
    }

    @Override
    public void onDisable() {
        database.close();
        ConsoleLogger.info("Authme " + this.getDescription().getVersion() + " disabled");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase("authme")) {
            if (args.length == 0) {
                sender.sendMessage("Usage: /authme reload|register playername password|changepassword playername password|unregister playername");
                return true;
            }

            if (!sender.hasPermission("authme.admin." + args[0].toLowerCase())) {
                sender.sendMessage(m._("no_perm"));
                return true;
            }

            if (args[0].equalsIgnoreCase("reload")) {
                database.reload();
                settings.reload();
                m.reload();
            } else if (args[0].equalsIgnoreCase("register")) {
                if (args.length != 3) {
                    sender.sendMessage("Usage: /authme register playername password");
                    return true;
                }

                String name = args[1].toLowerCase();
                String hash = pws.getHash(args[2]);

                if (database.isAuthAvailable(name)) {
                    sender.sendMessage(m._("user_regged"));
                    return true;
                }

                PlayerAuth auth = new PlayerAuth(name, hash, "198.18.0.1");
                database.saveAuth(auth);
                sender.sendMessage(m._("registered"));
                ConsoleLogger.info(args[1] + " registered");
            } else if (args[0].equalsIgnoreCase("changepassword")) {
                if (args.length != 3) {
                    sender.sendMessage("Usage: /authme changepassword playername newpassword");
                    return true;
                }

                String name = args[1].toLowerCase();
                String hash = pws.getHash(args[2]);

                PlayerAuth auth = new PlayerAuth(name, hash, "198.18.0.1");
                database.updatePassword(auth);
                sender.sendMessage("pwd_changed");
                ConsoleLogger.info(args[0] + "'s password changed");
            } else if (args[0].equalsIgnoreCase("unregister")) {
                if (args.length != 2) {
                    sender.sendMessage("Usage: /authme unregister playername");
                    return true;
                }

                String name = args[1].toLowerCase();

                PlayerCache.getInstance().removePlayer(name);
                database.removeAuth(name);
                sender.sendMessage("unregistered");
                ConsoleLogger.info(args[1] + " unregistered");
            } else {
                sender.sendMessage("Usage: /authme reload|register playername password|changepassword playername password|unregister playername");
            }
            return true;
        }

        if (!(sender instanceof Player)) {
            return true;
        }

        if (!sender.hasPermission("authme." + label.toLowerCase())) {
            sender.sendMessage(m._("no_perm"));
            return true;
        }

        Player player = (Player) sender;
        String name = player.getName().toLowerCase();
        String ip = player.getAddress().getAddress().getHostAddress();

        if (label.equalsIgnoreCase("register")) {
            if (PlayerCache.getInstance().isAuthenticated(name)) {
                player.sendMessage(m._("logged_in"));
                return true;
            }

            if (!settings.isRegistrationEnabled()) {
                player.sendMessage(m._("reg_disabled"));
                return true;
            }

            if (args.length == 0) {
                player.sendMessage(m._("usage_reg"));
                return true;
            }

            if (database.isAuthAvailable(player.getName().toLowerCase())) {
                player.sendMessage(m._("user_regged"));
                return true;
            }

            String hash = pws.getHash(args[0]);
            PlayerAuth auth = new PlayerAuth(name, hash, ip);
            database.saveAuth(auth);
            PlayerCache.getInstance().addPlayer(auth);
            LimboPlayer limbo = LimboCache.getInstance().getLimboPlayer(name);
            if (limbo != null) {
                player.getInventory().setContents(limbo.getInventory());
                player.getInventory().setArmorContents(limbo.getArmour());
                this.getServer().getScheduler().cancelTask(limbo.getTimeoutTaskId());
                LimboCache.getInstance().deleteLimboPlayer(name);
            }
            player.sendMessage(m._("registered"));
            ConsoleLogger.info(player.getDisplayName() + " registered");
            return true;
        }

        if (label.equalsIgnoreCase("login")) {
            if (PlayerCache.getInstance().isAuthenticated(name)) {
                player.sendMessage(m._("logged_in"));
                return true;
            }

            if (args.length == 0) {
                player.sendMessage(m._("usage_log"));
                return true;
            }

            if (!database.isAuthAvailable(player.getName().toLowerCase())) {
                player.sendMessage(m._("user_unknown"));
                return true;
            }

            String hash = database.getAuth(name).getHash();
            if (pws.comparePasswordWithHash(args[0], hash)) {
                PlayerAuth auth = new PlayerAuth(name, hash, ip);
                database.updateIP(auth);
                PlayerCache.getInstance().addPlayer(auth);
                LimboPlayer limbo = LimboCache.getInstance().getLimboPlayer(name);
                if (limbo != null) {
                    player.getInventory().setContents(limbo.getInventory());
                    player.getInventory().setArmorContents(limbo.getArmour());
                    player.teleport(limbo.getLoc());
                    this.getServer().getScheduler().cancelTask(limbo.getTimeoutTaskId());
                    LimboCache.getInstance().deleteLimboPlayer(name);
                }
                player.sendMessage(m._("login"));
                ConsoleLogger.info(player.getDisplayName() + " logged in!");
            } else {
                ConsoleLogger.info(player.getDisplayName() + " used the wrong password");
                if (settings.isKickOnWrongPasswordEnabled()) {
                    player.kickPlayer(m._("wrong_pwd"));
                } else {
                    player.sendMessage(m._("wrong_pwd"));
                }
            }
            return true;
        }

        if (label.equalsIgnoreCase("changepassword")) {
            if (!PlayerCache.getInstance().isAuthenticated(name)) {
                player.sendMessage(m._("not_logged_in"));
                return true;
            }

            if (args.length != 2) {
                player.sendMessage(m._("Usage: /changepassword oldPassword newPassword"));
                return true;
            }

            String hashnew = pws.getHash(args[1]);
            if (pws.comparePasswordWithHash(args[0], PlayerCache.getInstance().getAuth(name).getHash())) {
                PlayerAuth auth = new PlayerAuth(name, hashnew, ip);
                database.updatePassword(auth);
                PlayerCache.getInstance().updatePlayer(auth);
                player.sendMessage(m._("pwd_changed"));
                ConsoleLogger.info(player.getDisplayName() + " changed his password");
            } else {
                player.sendMessage(m._("wrong_pwd"));
            }
            return true;
        }

        if (label.equalsIgnoreCase("logout")) {
            if (!PlayerCache.getInstance().isAuthenticated(name)) {
                player.sendMessage(m._("not_logged_in"));
                return true;
            }

            PlayerCache.getInstance().removePlayer(player.getName().toLowerCase());

            LimboCache.getInstance().addLimboPlayer(player);
            player.getInventory().setArmorContents(new ItemStack[0]);
            player.getInventory().setContents(new ItemStack[36]);

            int delay = settings.getRegistrationTimeout() * 20;
            int interval = settings.getWarnMessageInterval() * 20;
            BukkitScheduler sched = this.getServer().getScheduler();
            int id = sched.scheduleSyncDelayedTask(this, new TimeoutTask(this, name), delay);
            sched.scheduleSyncDelayedTask(this, new MessageTask(this,name,m._("login_msg"),interval));
            LimboCache.getInstance().getLimboPlayer(name).setTimeoutTaskId(id);

            player.sendMessage(m._("logout"));
            ConsoleLogger.info(player.getDisplayName() + " logged out");
            return true;
        }

        if (label.equalsIgnoreCase("unregister")) {
            if (!PlayerCache.getInstance().isAuthenticated(name)) {
                player.sendMessage(m._("not_logged_in"));
                return true;
            }

            if (args.length != 1) {
                player.sendMessage(m._("usage_unreg"));
                return true;
            }

            if (pws.comparePasswordWithHash(args[0], PlayerCache.getInstance().getAuth(name).getHash())) {
                PlayerCache.getInstance().removePlayer(player.getName().toLowerCase());

                LimboCache.getInstance().addLimboPlayer(player);
                player.getInventory().setArmorContents(new ItemStack[0]);
                player.getInventory().setContents(new ItemStack[36]);
                database.removeAuth(name);

                int delay = settings.getRegistrationTimeout() * 20;
                int interval = settings.getWarnMessageInterval() * 20;
                BukkitScheduler sched = this.getServer().getScheduler();
                int id = sched.scheduleSyncDelayedTask(this, new TimeoutTask(this, name), delay);
                sched.scheduleSyncDelayedTask(this, new MessageTask(this, name, m._("reg_msg"), interval));
                LimboCache.getInstance().getLimboPlayer(name).setTimeoutTaskId(id);

                player.sendMessage("unregistered");
                ConsoleLogger.info(player.getDisplayName() + " unregistered himself");
            } else {
                player.sendMessage(m._("wrong_pwd"));
            }
            return true;
        }
        return false;
    }
}
