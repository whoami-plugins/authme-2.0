package uk.org.whoami.authme.settings;

import java.io.File;
import org.bukkit.util.config.Configuration;
import uk.org.whoami.authme.security.PasswordSecurity;

public final class Settings extends Configuration{
    
    public static final String PLUGIN_FOLDER = "./plugins/AuthMe";
    public static final String CACHE_FOLDER = Settings.PLUGIN_FOLDER + "/cache";
    public static final String AUTH_FILE = Settings.PLUGIN_FOLDER + "/auths.db";
    public static final String MESSAGE_FILE = Settings.PLUGIN_FOLDER + "/messages.yml";
    public static final String SETTINGS_FILE = Settings.PLUGIN_FOLDER + "/config.yml";
    
    private static Settings singleton;
    
    private Settings() {
        super(new File(Settings.PLUGIN_FOLDER + "/config.yml"));
        reload();
    }
    
    public void reload() {
        load();
        write();
    }
    
    private void write() {
        isRegistrationEnabled();
        isForcedRegistrationEnabled();
        getWarnMessageInterval();
        isSessionsEnabled();
        getRegistrationTimeout();
        isChatAllowed();
        isMovementAllowed();
        getMovementRadius();
        isKickNonRegisteredEnabled();
        getPasswordHash();
        getDataSource();
        isCachingEnabled();
        getMySQLHost();
        getMySQLPort();
        getMySQLUsername();
        getMySQLPassword();
        getMySQLDatabase();
        getMySQLTablename();
        getMySQLColumnName();
        getMySQLColumnPassword();
        save();
    }
    
    public boolean isForcedRegistrationEnabled() {
        String key = "settings.registration.force";
        if(getString(key) == null) {
            setProperty(key, true);
        }
        return getBoolean(key, true);
    }
    
    public boolean isRegistrationEnabled() {
        String key = "settings.registration.enabled";
        if(getString(key) == null) {
            setProperty(key, true);
        }
        return getBoolean(key, true);
    }
    
    public int getWarnMessageInterval() {
        String key = "settings.registration.messageInterval";
        if(getString(key) == null) {
            setProperty(key, 5);
        }
        return getInt(key, 5);
    }
    
    public boolean isSessionsEnabled() {
        String key = "settings.sessions";
        if(getString(key) == null) {
            setProperty(key, false);
        }
        return getBoolean(key, false);
    }
    
    public boolean isKickOnWrongPasswordEnabled() {
        String key = "settings.restrictions.kickOnWrongPassword";
        if(getString(key) == null) {
            setProperty(key, false);
        }
        return getBoolean(key, false);
    }
    
    public int getRegistrationTimeout() {
        String key = "settings.restrictions.timeout";
        if(getString(key) == null) {
            setProperty(key, 30);
        }
        return getInt(key, 30);
    }
    
    public boolean isChatAllowed() {
        String key = "settings.restrictions.allowChat";
        if(getString(key) == null) {
            setProperty(key, false);
        }
        return getBoolean(key, false);
    }
    
    public boolean isMovementAllowed() {
        String key = "settings.restrictions.allowMovement";
        if(getString(key) == null) {
            setProperty(key, false);
        }
        return getBoolean(key, false);
    }
    
    public int getMovementRadius() {
        String key = "settings.restrictions.allowedMovementRadius";
        if(getString(key) == null) {
            setProperty(key, 100);
        }
        return getInt(key, 100);
    }
    
    public boolean isKickNonRegisteredEnabled() {
        String key = "settings.restrictions.kickNonRegistered";
        if(getString(key) == null) {
            setProperty(key, false);
        }
        return getBoolean(key, false);
    }
    
    public boolean isTeleportToSpawnEnabled() {
        String key = "settings.restrictions.teleportUnAuthedToSpawn";
        if(getString(key) == null) {
            setProperty(key, true);
        }
        return getBoolean(key, true);
    }
    
    public int getPasswordHash() {
        String key = "settings.security.passwordHash";
        if(getString(key) == null) {
            setProperty(key, "SHA256");
        }
        
        String entry = getString(key);
        if(entry.equals("MD5")) return PasswordSecurity.MD5;
        if(entry.equals("SHA1")) return PasswordSecurity.SHA1;
        if(entry.equals("SHA256")) return PasswordSecurity.SHA256;
        
        return 0;
    }
    
    public boolean isCachingEnabled() {
        String key ="DataSource.caching";
        if(getString(key) == null) {
            setProperty(key, true);
        }
        return getBoolean(key, true);
    }
    
    public String getDataSource() {
        String key = "DataSource.backend";
        if(getString(key) == null) {
            setProperty(key, "file");
        }
        return getString(key);
    }
    
    public String getMySQLHost() {
        String key = "DataSource.mySQLHost";
        if(getString(key) == null) {
            setProperty(key, "127.0.0.1");
        }
        return getString(key);
    }
    
    public String getMySQLPort() {
        String key = "DataSource.mySQLPort";
        if(getString(key) == null) {
            setProperty(key, "3306");
        }
        return getString(key);
    }
    
    public String getMySQLUsername() {
        String key = "DataSource.mySQLUsername";
        if(getString(key) == null) {
            setProperty(key, "authme");
        }
        return getString(key);
    }

    public String getMySQLPassword() {
        String key = "DataSource.mySQLPassword";
        if(getString(key) == null) {
            setProperty(key, "12345");
        }
        return getString(key);
    }
    
    public String getMySQLDatabase() {
        String key = "DataSource.mySQLDatabase";
        if(getString(key) == null) {
            setProperty(key, "authme");
        }
        return getString(key);
    }
        
    public String getMySQLTablename() {
        String key = "DataSource.mySQLTablename";
        if(getString(key) == null) {
            setProperty(key, "authme");
        }
        return getString(key);
    }

    public String getMySQLColumnName() {
        String key = "DataSource.mySQLColumnName";
        if(getString(key) == null) {
            setProperty(key, "username");
        }
        return getString(key);
    }

    public String getMySQLColumnPassword() {
        String key = "DataSource.mySQLColumnPassword";
        if(getString(key) == null) {
            setProperty(key, "password");
        }
        return getString(key);
    }
    
    public static Settings getInstance() {
        if(singleton == null) {
            singleton = new Settings();
        }
        return singleton;
    }
}
