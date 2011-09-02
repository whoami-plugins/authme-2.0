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
    public synchronized boolean isAuthAvailable(String user) {
        return cache.containsKey(user);
    }

    @Override
    public synchronized PlayerAuth getAuth(String user) {
        return cache.get(user);
    }

    @Override
    public synchronized boolean saveAuth(PlayerAuth auth) {
        if(source.saveAuth(auth)) {
            cache.put(auth.getNickname(), auth);
        }
        return false;
    }

    @Override
    public synchronized boolean updateIP(PlayerAuth auth) {
        if(source.updateIP(auth)) {
            cache.get(auth.getNickname()).setIp(auth.getIp());
            return true;
        }
        return false;
    }

    @Override
    public synchronized boolean updatePassword(PlayerAuth auth) {
        if(source.updatePassword(auth)) {
            cache.get(auth.getNickname()).setHash(auth.getHash());
            return true;
        }
        return false;
    }

    @Override
    public synchronized boolean removeAuth(String user) {
        if(source.removeAuth(user)) {
            cache.remove(user);
            return true;
        }
        return false;
    }

    @Override
    public synchronized HashMap<String,PlayerAuth> getAllRegisteredUsers() {
        return cache;
    }

    @Override
    public synchronized void close() {
        source.close();
    }

    @Override
    public void reload() {
        cache = source.getAllRegisteredUsers();
    }
    
}
