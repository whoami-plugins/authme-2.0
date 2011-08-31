package uk.org.whoami.authme;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import uk.org.whoami.authme.cache.auth.AuthenticatedPlayers;
import uk.org.whoami.authme.datasource.DataSource;
import uk.org.whoami.authme.datasource.FileDataSource;
import uk.org.whoami.authme.security.PasswordSecurity;
import uk.org.whoami.authme.settings.Messages;
import uk.org.whoami.authme.settings.Settings;

public class AuthMe extends JavaPlugin {
    
    private AuthenticatedPlayers authcache;
    private DataSource database;
    private Settings settings;
    private Messages m;
    private PasswordSecurity hash;
    
    @Override
    public void onEnable() {
        this.settings = new Settings(this.getConfiguration());
        if(settings.getDataSource().equals("file")) {
            database = new FileDataSource();
        }
        authcache = new AuthenticatedPlayers();
        m = Messages.getInstance();
        hash = new PasswordSecurity();
    }
    
    @Override
    public void onDisable() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!(sender instanceof Player)) {
            return false;
        }
        Player player = (Player) sender;
         
        if(label.equalsIgnoreCase("register")) {
            if(settings.isRegistrationEnabled()) {
                if(args.length == 0) {
                    player.sendMessage(m._("Usage: /register password"));
                    return true;
                }
                                
                return true;
            } 
            player.sendMessage(m._("Registration is disabled"));
            
            return true;
        }       
        return false;
    }
}
