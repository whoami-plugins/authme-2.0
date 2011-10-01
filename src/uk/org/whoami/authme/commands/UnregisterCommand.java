/*
 * Copyright 2011 Sebastian Köhler <sebkoehler@whoami.org.uk>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.org.whoami.authme.commands;

import java.security.NoSuchAlgorithmException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import uk.org.whoami.authme.ConsoleLogger;
import uk.org.whoami.authme.cache.auth.PlayerCache;
import uk.org.whoami.authme.cache.limbo.LimboCache;
import uk.org.whoami.authme.datasource.DataSource;
import uk.org.whoami.authme.security.PasswordSecurity;
import uk.org.whoami.authme.settings.Messages;
import uk.org.whoami.authme.settings.Settings;
import uk.org.whoami.authme.task.MessageTask;
import uk.org.whoami.authme.task.TimeoutTask;

public class UnregisterCommand implements CommandExecutor {

    private Messages m = Messages.getInstance();
    private Settings settings = Settings.getInstance();
    private JavaPlugin plugin;
    private DataSource database;

    public UnregisterCommand(JavaPlugin plugin, DataSource database) {
        this.plugin = plugin;
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

        if (!PlayerCache.getInstance().isAuthenticated(name)) {
            player.sendMessage(m._("not_logged_in"));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(m._("usage_unreg"));
            return true;
        }
        try {
            if (PasswordSecurity.comparePasswordWithHash(args[0], PlayerCache.getInstance().getAuth(name).getHash())) {
                if (!database.removeAuth(name)) {
                    player.sendMessage("error");
                    return true;
                }
                PlayerCache.getInstance().removePlayer(player.getName().toLowerCase());
                LimboCache.getInstance().addLimboPlayer(player);
                player.getInventory().setArmorContents(new ItemStack[0]);
                player.getInventory().setContents(new ItemStack[36]);

                int delay = settings.getRegistrationTimeout() * 20;
                int interval = settings.getWarnMessageInterval();
                BukkitScheduler sched = sender.getServer().getScheduler();
                if (delay != 0) {
                    int id = sched.scheduleSyncDelayedTask(plugin, new TimeoutTask(plugin, name), delay);
                    LimboCache.getInstance().getLimboPlayer(name).setTimeoutTaskId(id);
                }
                sched.scheduleSyncDelayedTask(plugin, new MessageTask(plugin, name, m._("reg_msg"), interval));

                player.sendMessage("unregistered");
                ConsoleLogger.info(player.getDisplayName() + " unregistered himself");
            } else {
                player.sendMessage(m._("wrong_pwd"));
            }
        } catch (NoSuchAlgorithmException ex) {
            ConsoleLogger.showError(ex.getMessage());
            sender.sendMessage("Internal Error please read the server log");
        }
        return true;
    }
}
