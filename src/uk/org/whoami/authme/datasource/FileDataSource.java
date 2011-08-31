package uk.org.whoami.authme.datasource;

import java.util.HashMap;
import java.util.List;

public class FileDataSource implements DataSource {

    @Override
    public boolean isAuthAvailable(String user) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean createAuth(String user, String password) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean removeAuth(String user) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HashMap<String, String> getAuth(String user) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<HashMap<String, String>> getAllRegisteredUsers() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
