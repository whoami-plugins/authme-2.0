package uk.org.whoami.authme.settings;

import org.bukkit.util.config.Configuration;

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
        String key = "Settings.registration";
        if(conf.getString(key) == null) {
            conf.setProperty(key, true);
        }
        return conf.getBoolean(key, true);
    }
    
    public String getPasswordHash() {
        String key = "Settings.passwordHash";
        if(conf.getString(key) == null) {
            conf.setProperty(key, "SHA256");
        }
        return conf.getString(key);
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
