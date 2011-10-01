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
import java.util.Date;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import uk.org.whoami.authme.ConsoleLogger;
import uk.org.whoami.authme.cache.auth.PlayerAuth;
import uk.org.whoami.authme.cache.auth.PlayerCache;
import uk.org.whoami.authme.datasource.DataSource;
import uk.org.whoami.authme.security.PasswordSecurity;
import uk.org.whoami.authme.settings.Messages;
import uk.org.whoami.authme.settings.Settings;

public class AdminCommand implements CommandExecutor {

    private Messages m = Messages.getInstance();
    private Settings settings = Settings.getInstance();
    private DataSource database;

    public AdminCommand(DataSource database) {
        this.database = database;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmnd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("Usage: /authme reload|register playername password|changepassword playername password|unregister playername|purge");
            return true;
        }

        if (!sender.hasPermission("authme.admin." + args[0].toLowerCase())) {
            sender.sendMessage(m._("no_perm"));
            return true;
        }

        if (args[0].equalsIgnoreCase("purge")) {
            if (args.length != 2) {
                sender.sendMessage("Usage: /authme purge <DAYS>");
                return true;
            }

            try {
                long days = Long.parseLong(args[1]) * 86400000;
                long until = new Date().getTime() - days;

                sender.sendMessage("Deleted " + database.purgeDatabase(until) + " user accounts");

            } catch (NumberFormatException e) {
                sender.sendMessage("Usage: /authme purge <DAYS>");
                return true;
            }
        } else if (args[0].equalsIgnoreCase("reload")) {
            database.reload();
            settings.reload();
            m.reload();
            sender.sendMessage(m._("reload"));
        } else if (args[0].equalsIgnoreCase("register")) {
            if (args.length != 3) {
                sender.sendMessage("Usage: /authme register playername password");
                return true;
            }

            try {
                String name = args[1].toLowerCase();
                String hash = PasswordSecurity.getHash(settings.getPasswordHash(), args[2]);

                if (database.isAuthAvailable(name)) {
                    sender.sendMessage(m._("user_regged"));
                    return true;
                }

                PlayerAuth auth = new PlayerAuth(name, hash, "198.18.0.1", 0);
                if (!database.saveAuth(auth)) {
                    sender.sendMessage(m._("error"));
                    return true;
                }
                sender.sendMessage(m._("registered"));
                ConsoleLogger.info(args[1] + " registered");
            } catch (NoSuchAlgorithmException ex) {
                ConsoleLogger.showError(ex.getMessage());
                sender.sendMessage(m._("error"));
            }
        } else if (args[0].equalsIgnoreCase("changepassword")) {
            if (args.length != 3) {
                sender.sendMessage("Usage: /authme changepassword playername newpassword");
                return true;
            }

            try {
                String name = args[1].toLowerCase();
                String hash = PasswordSecurity.getHash(settings.getPasswordHash(), args[2]);

                PlayerAuth auth = null;
                if (PlayerCache.getInstance().isAuthenticated(name)) {
                    auth = PlayerCache.getInstance().getAuth(name);
                } else if (database.isAuthAvailable(name)) {
                    auth = database.getAuth(name);
                } else {
                    sender.sendMessage(m._("unknown_user"));
                    return true;
                }
                auth.setHash(hash);

                if (!database.updatePassword(auth)) {
                    sender.sendMessage(m._("error"));
                    return true;
                }

                sender.sendMessage("pwd_changed");
                ConsoleLogger.info(args[0] + "'s password changed");
            } catch (NoSuchAlgorithmException ex) {
                ConsoleLogger.showError(ex.getMessage());
                sender.sendMessage(m._("error"));
            }
        } else if (args[0].equalsIgnoreCase("unregister")) {
            if (args.length != 2) {
                sender.sendMessage("Usage: /authme unregister playername");
                return true;
            }

            String name = args[1].toLowerCase();

            if (!database.removeAuth(name)) {
                sender.sendMessage(m._("error"));
                return true;
            }

            PlayerCache.getInstance().removePlayer(name);
            sender.sendMessage("unregistered");

            ConsoleLogger.info(args[1] + " unregistered");
        } else {
            sender.sendMessage("Usage: /authme reload|register playername password|changepassword playername password|unregister playername");
        }
        return true;
    }
}
