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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;
import uk.org.whoami.authme.ConsoleLogger;
import uk.org.whoami.authme.cache.auth.PlayerAuth;
import uk.org.whoami.authme.settings.Settings;

public class MySQLDataSource implements DataSource {

    private String host;
    private String port;
    private String username;
    private String password;
    private String database;
    private String tableName;
    private String columnName;
    private String columnPassword;
    private String columnIp;
    private String columnLastLogin;
    private Connection con;

    public MySQLDataSource() throws ClassNotFoundException, SQLException {
        Settings s = Settings.getInstance();
        this.host = s.getMySQLHost();
        this.port = s.getMySQLPort();
        this.username = s.getMySQLUsername();
        this.password = s.getMySQLPassword();

        this.database = s.getMySQLDatabase();
        this.tableName = s.getMySQLTablename();
        this.columnName = s.getMySQLColumnName();
        this.columnPassword = s.getMySQLColumnPassword();
        this.columnIp = s.getMySQLColumnIp();
        this.columnLastLogin = s.getMySQLColumnLastLogin();

        connect();
        setup();
    }

    private synchronized void connect() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver");
        ConsoleLogger.info("MySQL driver loaded");
        con = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port
                + "/" + database, username, password);
        ConsoleLogger.info("Connected to MySQL");
    }

    private synchronized void setup() throws SQLException {

        Statement st = null;
        ResultSet rs = null;
        try {
            st = con.createStatement();
            st.executeUpdate("CREATE TABLE IF NOT EXISTS " + tableName + " ("
                    + "id INTEGER AUTO_INCREMENT,"
                    + columnName + " VARCHAR(255) NOT NULL,"
                    + columnPassword + " VARCHAR(255) NOT NULL,"
                    + columnIp + " VARCHAR(40) NOT NULL,"
                    + columnLastLogin + " TIMESTAMP,"
                    + "CONSTRAINT table_const_prim PRIMARY KEY (id));");

            rs = con.getMetaData().getColumns(null, null, tableName, columnIp);
            if (!rs.next()) {
                st.executeUpdate("ALTER TABLE " + tableName + " ADD COLUMN "
                        + columnIp + " VARCHAR(40) NOT NULL;");
            }
            rs.close();
            rs = con.getMetaData().getColumns(null, null, tableName, columnLastLogin);
            if (!rs.next()) {
                st.executeUpdate("ALTER TABLE " + tableName + " ADD COLUMN "
                        + columnLastLogin + " TIMESTAMP;");
            }

        } finally {
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException ex) {
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                }
            }
        }
        ConsoleLogger.info("MySQL Setup finished");
    }

    @Override
    public synchronized boolean isAuthAvailable(String user) {
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement("SELECT * FROM " + tableName + " WHERE "
                    + columnName + "=?;");
            pst.setString(1, user);
            ResultSet rs = pst.executeQuery();
            return rs.next();
        } catch (SQLException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return false;
        } finally {
            if (pst != null) {
                try {
                    pst.close();
                } catch (SQLException ex) {
                }
            }
        }
    }

    @Override
    public synchronized PlayerAuth getAuth(String user) {
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement("SELECT * FROM " + tableName + " WHERE "
                    + columnName + "=?;");
            pst.setString(1, user);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                if (rs.getString(columnIp).isEmpty()) {
                    return new PlayerAuth(rs.getString(columnName), rs.getString(columnPassword), "198.18.0.1", rs.getTimestamp(columnLastLogin));
                } else {
                    return new PlayerAuth(rs.getString(columnName), rs.getString(columnPassword), rs.getString(columnIp), rs.getTimestamp(columnLastLogin));
                }
            } else {
                return null;
            }
        } catch (SQLException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return null;
        } finally {
            if (pst != null) {
                try {
                    pst.close();
                } catch (SQLException ex) {
                }
            }
        }
    }

    @Override
    public synchronized boolean saveAuth(PlayerAuth auth) {
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement("INSERT INTO " + tableName + "(" + columnName + "," + columnPassword + "," + columnIp + "," + columnLastLogin + ") VALUES (?,?,?,?);");
            pst.setString(1, auth.getNickname());
            pst.setString(2, auth.getHash());
            pst.setString(3, auth.getIp());
            pst.setTimestamp(4, new Timestamp(auth.getLastLogin().getTime()));
            pst.executeUpdate();
        } catch (SQLException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return false;
        } finally {
            if (pst != null) {
                try {
                    pst.close();
                } catch (SQLException ex) {
                }
            }
        }
        return true;
    }

    @Override
    public synchronized boolean updatePassword(PlayerAuth auth) {
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement("UPDATE " + tableName + " SET " + columnPassword + "=? WHERE " + columnName + "=?;");
            pst.setString(1, auth.getHash());
            pst.setString(2, auth.getNickname());
            pst.executeUpdate();
        } catch (SQLException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return false;
        } finally {
            if (pst != null) {
                try {
                    pst.close();
                } catch (SQLException ex) {
                }
            }
        }
        return true;
    }

    @Override
    public boolean updateLogin(PlayerAuth auth) {
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement("UPDATE " + tableName + " SET " + columnIp + "=?, " + columnLastLogin + "=? WHERE " + columnName + "=?;");
            pst.setString(1, auth.getIp());
            pst.setTimestamp(2, new Timestamp(auth.getLastLogin().getTime()));
            pst.setString(3, auth.getNickname());
            pst.executeUpdate();
        } catch (SQLException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return false;
        } finally {
            if (pst != null) {
                try {
                    pst.close();
                } catch (SQLException ex) {
                }
            }
        }
        return true;
    }

    @Override
    public synchronized boolean removeAuth(String user) {
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement("DELETE FROM " + tableName + " WHERE " + columnName + "=?;");
            pst.setString(1, user);
            pst.executeUpdate();
        } catch (SQLException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return false;
        } finally {
            if (pst != null) {
                try {
                    pst.close();
                } catch (SQLException ex) {
                }
            }
        }
        return true;
    }

    @Override
    public synchronized HashMap<String, PlayerAuth> getAllRegisteredUsers() {
        HashMap<String, PlayerAuth> map = new HashMap<String, PlayerAuth>();
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement("SELECT * FROM " + tableName + ";");
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                if (rs.getString(columnIp).isEmpty()) {
                    map.put(rs.getString(columnName), new PlayerAuth(rs.getString(columnName), rs.getString(columnPassword), "198.18.0.1", rs.getTimestamp(columnLastLogin)));
                } else {
                    map.put(rs.getString(columnName), new PlayerAuth(rs.getString(columnName), rs.getString(columnPassword), rs.getString(columnIp), rs.getTimestamp(columnLastLogin)));
                }
            }
        } catch (SQLException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return map;
        } finally {
            if (pst != null) {
                try {
                    pst.close();
                } catch (SQLException ex) {
                }
            }
        }
        return map;
    }

    @Override
    public synchronized void close() {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException ex) {
                ConsoleLogger.showError("Couldn't close MySQL connection");
            }
        }
    }

    @Override
    public void reload() {
    }
}
