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
        map.put("logged_in", "&cAlready logged in!");
        map.put("not_logged_in", "&cNot logged in!");
        map.put("reg_disabled", "&cRegistration is disabled");
        map.put("user_regged", "&cUsername already registered");
        map.put("usage_reg", "&cUsage: /register password");
        map.put("usage_log", "&cUsage: /login password");
        map.put("user_unknown", "&cUsername not registered");
        map.put("pwd_changed", "&cPassword changed!");
        map.put("reg_only", "Registered players only! Please visit http://example.com to register");
        map.put("valid_session", "&cSession login");
        map.put("login_msg", "&cPlease login with \"/login password\"");
        map.put("reg_msg", "&cPlease register with \"/register password\"");
        map.put("timeout", "Login Timeout");
        map.put("wrong_pwd", "&cWrong password");
        map.put("logout", "&cSuccessful logout");
        map.put("usage_unreg", "&cUsage: /unregister password");
        map.put("registered", "&cSuccessfully registered!");
        map.put("unregistered", "&cSuccessfully unregistered!");
        map.put("login", "&cSuccessful login!");
        map.put("no_perm", "&cNo Permission");
        map.put("same_nick", "Same nick is already playing");
        map.put("reg_voluntarily", "You can register your nickname with the server with the command \"/register password\"");
        map.put("reload", "Configuration and database has been reloaded");
        map.put("error", "An error ocurred; Please contact the admin");
        map.put("unknown_user", "User is not in database");
    }

    private void loadFile() {
        this.load();
        for (String key : map.keySet()) {
            if (this.getString(key) == null) {
                this.setProperty(key, map.get(key));
            } else {
                map.put(key, this.getString(key));
            }
        }
        this.save();
    }

    public String _(String msg) {
        String loc = map.get(msg);
        if (loc != null) {
            return loc.replace("&", "\u00a7");
        }
        return msg;
    }

    public void reload() {
        loadFile();
    }

    public static Messages getInstance() {
        if (singleton == null) {
            singleton = new Messages();
        }
        return singleton;
    }
}
