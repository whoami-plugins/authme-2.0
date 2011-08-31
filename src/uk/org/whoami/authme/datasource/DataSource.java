package uk.org.whoami.authme.datasource;

import java.util.HashMap;
import java.util.List;

public interface DataSource {
    
    public boolean isAuthAvailable(String user);
    public boolean createAuth(String user, String password);
    public boolean removeAuth(String user);
    
    public HashMap<String,String> getAuth(String user);
    
    List<HashMap<String,String>> getAllRegisteredUsers();
}
