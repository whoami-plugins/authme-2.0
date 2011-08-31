package uk.org.whoami.authme.cache.auth;

import java.util.ArrayList;

public class AuthenticatedPlayers {

    private ArrayList<PlayerAuth> cache;

    public AuthenticatedPlayers() {
        cache = new ArrayList<PlayerAuth>();
    }

    public void addPlayer(String user,String ip) {
        cache.add(new PlayerAuth(user,ip));
    }

    public void removePlayer(String user, String ip) {
        cache.remove(new PlayerAuth(user,ip));
    }

    public boolean isAuthenticated(String user, String ip) {
        return cache.contains(new PlayerAuth(user,ip));
    }
}
