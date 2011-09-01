package uk.org.whoami.authme;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import uk.org.whoami.authme.cache.auth.PlayerAuth;
import uk.org.whoami.authme.cache.auth.PlayerCache;
import uk.org.whoami.authme.cache.inventory.Inventory;
import uk.org.whoami.authme.cache.inventory.InventoryCache;
import uk.org.whoami.authme.datasource.CacheDataSource;
import uk.org.whoami.authme.datasource.DataSource;
import uk.org.whoami.authme.datasource.FileDataSource;
import uk.org.whoami.authme.listener.AuthMeBlockListener;
import uk.org.whoami.authme.listener.AuthMeEntityListener;
import uk.org.whoami.authme.listener.AuthMePlayerListener;
import uk.org.whoami.authme.security.PasswordSecurity;
import uk.org.whoami.authme.settings.Messages;
import uk.org.whoami.authme.settings.Settings;

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
                ConsoleLogger.showError("Can't load database");
                this.getServer().getPluginManager().disablePlugin(this);
                return;
            }
        }
        
        if(settings.isCachingEnabled()) {
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

        AuthMePlayerListener playerListener = new AuthMePlayerListener(this,database);
        AuthMeBlockListener blockListener = new AuthMeBlockListener(database);
        AuthMeEntityListener entityListener = new AuthMeEntityListener();

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
        ConsoleLogger.info("Authme loaded");
    }

    @Override
    public void onDisable() {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }
        Player player = (Player) sender;
        String name = player.getName().toLowerCase();
        String ip = player.getAddress().getAddress().getHostAddress();

        if (label.equalsIgnoreCase("register")) {
            if (PlayerCache.getInstance().isAuthenticated(name)) {
                player.sendMessage(m._("Already logged in!"));
                return true;
            }

            if (!settings.isRegistrationEnabled()) {
                player.sendMessage(m._("Registration is disabled"));
                return true;
            }

            if (args.length == 0) {
                player.sendMessage(m._("Usage: /register password"));
                return true;
            }

            if (database.isAuthAvailable(player.getName().toLowerCase())) {
                player.sendMessage(m._("Username already registered"));
                return true;
            }

            String hash = pws.getHash(args[0]);
            PlayerAuth auth = new PlayerAuth(name, hash, ip);
            database.saveAuth(auth);
            PlayerCache.getInstance().addPlayer(auth);
            Inventory inv = InventoryCache.getInstance().getInventory(name);
            if (inv != null) {
                player.getInventory().setContents(inv.getInventory());
                player.getInventory().setArmorContents(inv.getArmour());
                InventoryCache.getInstance().deleteInventory(name);
            }

            return true;
        }

        if (label.equalsIgnoreCase("login")) {
            if (PlayerCache.getInstance().isAuthenticated(name)) {
                player.sendMessage(m._("Already logged in!"));
                return true;
            }

            if (args.length == 0) {
                player.sendMessage(m._("Usage: /login password"));
                return true;
            }

            if (!database.isAuthAvailable(player.getName().toLowerCase())) {
                player.sendMessage(m._("Username not registered"));
                return true;
            }

            String hash = database.getAuth(name).getHash();
            if (pws.comparePasswordWithHash(args[0], hash)) {
                PlayerAuth auth = new PlayerAuth(name, hash, ip);
                database.updateIP(auth);
                PlayerCache.getInstance().addPlayer(auth);
                Inventory inv = InventoryCache.getInstance().getInventory(name);
                if (inv != null) {
                    player.getInventory().setContents(inv.getInventory());
                    player.getInventory().setArmorContents(inv.getArmour());
                    InventoryCache.getInstance().deleteInventory(name);
                }
            }
        }

        if (label.equalsIgnoreCase("changepassword")) {
            if (!PlayerCache.getInstance().isAuthenticated(name)) {
                player.sendMessage(m._("Not logged in!"));
                return true;
            }

            if (args.length != 2) {
                player.sendMessage(m._("Usage: /changepassword oldPassword newPassword"));
                return true;
            }

            String hashnew = pws.getHash(args[1]);

            if (pws.comparePasswordWithHash(args[0], PlayerCache.getInstance().getAuth(ip).getHash())) {
                PlayerAuth auth = new PlayerAuth(name, hashnew, ip);
                database.updatePassword(auth);
                PlayerCache.getInstance().updatePlayer(auth);
                player.sendMessage("Password changed");
            } else {
                player.sendMessage("Wrong old password");
            }
        }
        return false;
    }
}
