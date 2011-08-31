package uk.org.whoami.authme.settings;

import java.io.File;
import java.util.HashMap;
import org.bukkit.util.config.Configuration;

public class Messages extends Configuration {
    
    private static Messages singleton = null;
    private HashMap<String, String> map;

    private Messages() {
        super(new File(Settings.MESSAGE_FILE));
        map = new HashMap<String, String>();
        loadDefaults();
        loadFile();
    }
    
    private void loadDefaults() {
        map.put("Registration is disabled","Registration is disabled");
        map.put("Usage: /register password", "Usage: /register password");
    }

    private void loadFile() {
        this.load();
        for(String key : map.keySet()) {
            if(this.getString(key) == null) {
                this.setProperty(key, map.get(key));
            } else {
                map.put(key, this.getString(key));
            }
        }
        this.save();
    }
    
    public String _(String msg) {
        return map.get(msg);
    }
    
    public static Messages getInstance() {
        if(singleton == null) {
            singleton = new Messages();
        }
        return singleton;
    }
}
