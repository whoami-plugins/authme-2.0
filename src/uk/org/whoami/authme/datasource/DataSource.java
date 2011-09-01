package uk.org.whoami.authme.datasource;

import java.util.List;
import uk.org.whoami.authme.cache.auth.PlayerAuth;

public interface DataSource {
            
    public boolean isAuthAvailable(String user);
    public PlayerAuth getAuth(String user);
    
    public boolean saveAuth(PlayerAuth auth);
    public boolean updateIP(PlayerAuth auth);
    public boolean updatePassword(PlayerAuth auth);
    public boolean removeAuth(String user);
    
    List<PlayerAuth> getAllRegisteredUsers();
}
