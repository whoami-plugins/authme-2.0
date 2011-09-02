package uk.org.whoami.authme.datasource;

import java.util.HashMap;
import uk.org.whoami.authme.cache.auth.PlayerAuth;

public interface DataSource {
            
    boolean isAuthAvailable(String user);
    PlayerAuth getAuth(String user);
    
    boolean saveAuth(PlayerAuth auth);
    boolean updateIP(PlayerAuth auth);
    boolean updatePassword(PlayerAuth auth);
    boolean removeAuth(String user);
    
    void close();
    void reload();
    
    HashMap<String,PlayerAuth> getAllRegisteredUsers();
}
