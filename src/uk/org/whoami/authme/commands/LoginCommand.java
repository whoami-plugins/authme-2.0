package uk.org.whoami.authme.commands;

import java.security.NoSuchAlgorithmException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import uk.org.whoami.authme.ConsoleLogger;
import uk.org.whoami.authme.cache.auth.PlayerAuth;
import uk.org.whoami.authme.cache.auth.PlayerCache;
import uk.org.whoami.authme.cache.inventory.LimboCache;
import uk.org.whoami.authme.cache.inventory.LimboPlayer;
import uk.org.whoami.authme.datasource.DataSource;
import uk.org.whoami.authme.security.PasswordSecurity;
import uk.org.whoami.authme.settings.Messages;
import uk.org.whoami.authme.settings.Settings;

public class LoginCommand implements CommandExecutor {

    private Messages m = Messages.getInstance();
    private Settings settings = Settings.getInstance();
    private DataSource database;

    public LoginCommand(DataSource database) {
        this.database = database;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmnd, String label, String[] args) {
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

        try {
            if (PasswordSecurity.comparePasswordWithHash(args[0], hash)) {
                PlayerAuth auth = new PlayerAuth(name, hash, ip);
                database.updateIP(auth);
                PlayerCache.getInstance().addPlayer(auth);
                LimboPlayer limbo = LimboCache.getInstance().getLimboPlayer(name);
                if (limbo != null) {
                    player.getInventory().setContents(limbo.getInventory());
                    player.getInventory().setArmorContents(limbo.getArmour());
                    if (settings.isTeleportToSpawnEnabled()) {
                        player.teleport(limbo.getLoc());
                    }
                    sender.getServer().getScheduler().cancelTask(limbo.getTimeoutTaskId());
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
        } catch (NoSuchAlgorithmException ex) {
            ConsoleLogger.showError(ex.getMessage());
            sender.sendMessage(m._("error"));
        }
        return true;
    }
}
