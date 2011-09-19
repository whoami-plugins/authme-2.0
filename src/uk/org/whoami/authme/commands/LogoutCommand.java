package uk.org.whoami.authme.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import uk.org.whoami.authme.ConsoleLogger;
import uk.org.whoami.authme.cache.auth.PlayerCache;
import uk.org.whoami.authme.cache.inventory.LimboCache;
import uk.org.whoami.authme.settings.Messages;
import uk.org.whoami.authme.settings.Settings;
import uk.org.whoami.authme.task.MessageTask;
import uk.org.whoami.authme.task.TimeoutTask;

public class LogoutCommand implements CommandExecutor {

    private Messages m = Messages.getInstance();
    private Settings settings = Settings.getInstance();
    private JavaPlugin plugin;

    public LogoutCommand(JavaPlugin plugin) {
        this.plugin = plugin;
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

        if (!PlayerCache.getInstance().isAuthenticated(name)) {
            player.sendMessage(m._("not_logged_in"));
            return true;
        }

        PlayerCache.getInstance().removePlayer(player.getName().toLowerCase());

        LimboCache.getInstance().addLimboPlayer(player);
        player.getInventory().setArmorContents(new ItemStack[0]);
        player.getInventory().setContents(new ItemStack[36]);
        if(settings.isTeleportToSpawnEnabled()) {
            player.teleport(player.getWorld().getSpawnLocation());
        }

        int delay = settings.getRegistrationTimeout() * 20;
        int interval = settings.getWarnMessageInterval();
        BukkitScheduler sched = sender.getServer().getScheduler();
        if (delay != 0) {
            int id = sched.scheduleSyncDelayedTask(plugin, new TimeoutTask(plugin, name), delay);
            LimboCache.getInstance().getLimboPlayer(name).setTimeoutTaskId(id);
        }
        sched.scheduleSyncDelayedTask(plugin, new MessageTask(plugin, name, m._("login_msg"), interval));

        player.sendMessage(m._("logout"));
        ConsoleLogger.info(player.getDisplayName() + " logged out");
        return true;
    }
}
