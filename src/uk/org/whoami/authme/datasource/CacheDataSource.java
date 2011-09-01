package uk.org.whoami.authme.datasource;

import java.util.HashMap;
import uk.org.whoami.authme.cache.auth.PlayerAuth;

public class CacheDataSource implements DataSource {
    
    private DataSource source;
    private HashMap<String,PlayerAuth> cache;
    
    public CacheDataSource(DataSource source) {
        this.source = source;
        cache = source.getAllRegisteredUsers();
    }

    @Override
    public boolean isAuthAvailable(String user) {
        return cache.containsKey(user);
    }

    @Override
    public PlayerAuth getAuth(String user) {
        return cache.get(user);
    }

    @Override
    public boolean saveAuth(PlayerAuth auth) {
        if(source.saveAuth(auth)) {
            cache.put(auth.getNickname(), auth);
        }
        return false;
    }

    @Override
    public boolean updateIP(PlayerAuth auth) {
        if(source.updateIP(auth)) {
            cache.get(auth.getNickname()).setIp(auth.getIp());
            return true;
        }
        return false;
    }

    @Override
    public boolean updatePassword(PlayerAuth auth) {
        if(source.updateIP(auth)) {
            cache.get(auth.getNickname()).setHash(auth.getHash());
            return true;
        }
        return false;
    }

    @Override
    public boolean removeAuth(String user) {
        if(source.removeAuth(user)) {
            cache.remove(user);
            return true;
        }
        return false;
    }

    @Override
    public HashMap<String,PlayerAuth> getAllRegisteredUsers() {
        return cache;
    }
    
}
