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

package uk.org.whoami.authme.datasource;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import uk.org.whoami.authme.ConsoleLogger;
import uk.org.whoami.authme.cache.auth.PlayerAuth;
import uk.org.whoami.authme.settings.Settings;

public class FileDataSource implements DataSource {

    private File source;

    public FileDataSource() throws IOException {
        source = new File(Settings.AUTH_FILE);
        source.createNewFile();
    }

    @Override
    public synchronized boolean isAuthAvailable(String user) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(source));
            String line;
            while ((line = br.readLine()) != null) {
                String[] args = line.split(":");
                if (args.length > 1 && args[0].equals(user)) {
                    return true;
                }
            }
        } catch (FileNotFoundException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return false;
        } catch (IOException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return false;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                }
            }
        }
        return false;
    }

    @Override
    public synchronized boolean saveAuth(PlayerAuth auth) {
        if (isAuthAvailable(auth.getNickname())) {
            return false;
        }

        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(source, true));
            bw.write(auth.getNickname() + ":" + auth.getHash() + ":" + auth.getIp() + ":" + auth.getLastLogin().getTime() + "\n");
        } catch (IOException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return false;
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException ex) {
                }
            }
        }
        return true;
    }

    @Override
    public synchronized boolean updatePassword(PlayerAuth auth) {
        if (!isAuthAvailable(auth.getNickname())) {
            return false;
        }

        PlayerAuth newAuth = null;

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(source));
            String line = "";
            while ((line = br.readLine()) != null) {
                String[] args = line.split(":");
                if (args[0].equals(auth.getNickname())) {
                    newAuth = new PlayerAuth(args[0], auth.getHash(), args[2], new Date(Long.parseLong(args[4])));
                    break;
                }
            }
        } catch (FileNotFoundException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return false;
        } catch (IOException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return false;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                }
            }
        }
        removeAuth(auth.getNickname());
        saveAuth(newAuth);
        return true;
    }

    @Override
    public boolean updateLogin(PlayerAuth auth) {
        if (!isAuthAvailable(auth.getNickname())) {
            return false;
        }

        PlayerAuth newAuth = null;

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(source));
            String line = "";
            while ((line = br.readLine()) != null) {
                String[] args = line.split(":");
                if (args[0].equals(auth.getNickname())) {
                    newAuth = new PlayerAuth(args[0], args[1], auth.getIp(), auth.getLastLogin());
                    break;
                }
            }
        } catch (FileNotFoundException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return false;
        } catch (IOException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return false;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                }
            }
        }
        removeAuth(auth.getNickname());
        saveAuth(newAuth);
        return true;
    }

    @Override
    public synchronized boolean removeAuth(String user) {
        if (!isAuthAvailable(user)) {
            return false;
        }

        BufferedReader br = null;
        BufferedWriter bw = null;
        ArrayList<String> lines = new ArrayList<String>();
        try {
            br = new BufferedReader(new FileReader(source));
            String line;
            while ((line = br.readLine()) != null) {
                String[] args = line.split(":");
                if (args.length > 1 && !args[0].equals(user)) {
                    lines.add(line);
                }
            }
            bw = new BufferedWriter(new FileWriter(source));
            for (String l : lines) {
                bw.write(l + "\n");
            }
        } catch (FileNotFoundException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return false;
        } catch (IOException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return false;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                }
            }
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException ex) {
                }
            }
        }
        return true;
    }

    @Override
    public synchronized PlayerAuth getAuth(String user) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(source));
            String line;
            while ((line = br.readLine()) != null) {
                String[] args = line.split(":");
                switch (args.length) {
                    case 2:
                        return new PlayerAuth(args[0], args[1], "198.18.0.1", new Date(0));
                    case 3:
                        return new PlayerAuth(args[0], args[1], args[2], new Date(0));
                    case 4:
                        return new PlayerAuth(args[0], args[1], args[2], new Date(Long.parseLong(args[3])));
                }
            }
        } catch (FileNotFoundException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return null;
        } catch (IOException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return null;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                }
            }
        }
        return null;
    }

    @Override
    public synchronized HashMap<String, PlayerAuth> getAllRegisteredUsers() {
        HashMap<String, PlayerAuth> map = new HashMap<String, PlayerAuth>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(source));
            String line;
            while ((line = br.readLine()) != null) {
                String[] args = line.split(":");
                switch(args.length) {
                    case 2:
                        map.put(args[0], new PlayerAuth(args[0], args[1], "198.18.0.1", new Date(0)));
                        break;
                    case 3:
                        map.put(args[0], new PlayerAuth(args[0], args[1], args[2], new Date(0)));
                        break;
                    case 4:
                        map.put(args[0], new PlayerAuth(args[0], args[1], args[2], new Date(Long.parseLong(args[3]))));
                        break;
                }
            }
        } catch (FileNotFoundException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return map;
        } catch (IOException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return map;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                }
            }
        }
        return map;
    }

    @Override
    public synchronized void close() {
    }

    @Override
    public void reload() {
    }
}
