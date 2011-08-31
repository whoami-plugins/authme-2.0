package uk.org.whoami.authme.datasource;

import java.util.List;
import uk.org.whoami.authme.cache.auth.PlayerAuth;

public class FileDataSource implements DataSource {

    @Override
    public boolean isAuthAvailable(String user) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean saveAuth(PlayerAuth auth) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean removeAuth(String user) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PlayerAuth getAuth(String user) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<PlayerAuth> getAllRegisteredUsers() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean updateIP(PlayerAuth auth) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean updatePassword(PlayerAuth auth) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
