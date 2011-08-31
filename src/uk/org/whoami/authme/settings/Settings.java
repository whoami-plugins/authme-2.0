package uk.org.whoami.authme.settings;

import org.bukkit.util.config.Configuration;
import uk.org.whoami.authme.security.PasswordSecurity;

public class Settings {
    
    public static final String PLUGIN_FOLDER = "./plugins/AuthMe";
    public static final String CACHE_FOLDER = Settings.PLUGIN_FOLDER + "/cache";
    public static final String AUTH_FILE = Settings.PLUGIN_FOLDER + "/auths.db";
    public static final String MESSAGE_FILE = Settings.PLUGIN_FOLDER + "/messages.yml";
    
    private Configuration conf;
    
    public Settings(Configuration conf) {
        this.conf = conf;
        conf.load();
        write();
    }
    
    private void write() {
        isRegistrationEnabled();
        isForcedRegistrationEnabled();
        getDataSource();
        isCachingEnabled();
        conf.save();
    }
    
    public boolean isForcedRegistrationEnabled() {
        String key = "Settings.forceRegistration";
        if(conf.getString(key) == null) {
            conf.setProperty(key, true);
        }
        return conf.getBoolean(key, true);
    }
    
    public boolean isRegistrationEnabled() {
        String key = "Settings.registrationEnabled";
        if(conf.getString(key) == null) {
            conf.setProperty(key, true);
        }
        return conf.getBoolean(key, true);
    }
    
    public boolean isChatAllowed() {
        String key = "Settings.enableChat";
        if(conf.getString(key) == null) {
            conf.setProperty(key, false);
        }
        return conf.getBoolean(key, false);
    }
    
    public boolean isMovementAllowed() {
        String key = "Settings.enableMove";
        if(conf.getString(key) == null) {
            conf.setProperty(key, false);
        }
        return conf.getBoolean(key, false);
    }
    
    public boolean isKickNonRegisteredEnabled() {
        String key = "Settings.kickNonRegistered";
        if(conf.getString(key) == null) {
            conf.setProperty(key, false);
        }
        return conf.getBoolean(key, false);
    }
    
    public int getPasswordHash() {
        String key = "Settings.passwordHash";
        if(conf.getString(key) == null) {
            conf.setProperty(key, "SHA256");
        }
        
        String entry = conf.getString(key);
        if(entry.equals("MD5")) return PasswordSecurity.MD5;
        if(entry.equals("SHA1")) return PasswordSecurity.SHA1;
        if(entry.equals("SHA256")) return PasswordSecurity.SHA256;
        
        return 0;
    }
    
    public boolean isCachingEnabled() {
        String key ="DataSource.caching";
        if(conf.getString(key) == null) {
            conf.setProperty(key, true);
        }
        return conf.getBoolean(key, true);
    }
    
    public String getDataSource() {
        String key = "DataSource.backend";
        if(conf.getString(key) == null) {
            conf.setProperty(key, "file");
        }
        return conf.getString(key);
    }
}
