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

import java.util.HashMap;

import uk.org.whoami.authme.cache.auth.PlayerAuth;

public class CacheDataSource implements DataSource {

    private DataSource source;
    private final HashMap<String, PlayerAuth> cache = new HashMap<String, PlayerAuth>();

    public CacheDataSource(DataSource source) {
        this.source = source;
    }

    @Override
    public synchronized boolean isAuthAvailable(String user) {
        return cache.containsKey(user) ? true : source.isAuthAvailable(user);
    }

    @Override
    public synchronized PlayerAuth getAuth(String user) {
        if(cache.containsKey(user)) {
            return cache.get(user);
        } else {
            PlayerAuth auth = source.getAuth(user);
            cache.put(user, auth);
            return auth;
        }
    }

    @Override
    public synchronized boolean saveAuth(PlayerAuth auth) {
        if (source.saveAuth(auth)) {
            cache.put(auth.getNickname(), auth);
            return true;
        }
        return false;
    }

    @Override
    public synchronized boolean updatePassword(PlayerAuth auth) {
        if (source.updatePassword(auth)) {
            cache.get(auth.getNickname()).setHash(auth.getHash());
            return true;
        }
        return false;
    }

    @Override
    public boolean updateSession(PlayerAuth auth) {
        if (source.updateSession(auth)) {
            cache.get(auth.getNickname()).setIp(auth.getIp());
            cache.get(auth.getNickname()).setLastLogin(auth.getLastLogin());
            return true;
        }
        return false;
    }

    @Override
    public int purgeDatabase(long until) {
        int cleared = source.purgeDatabase(until);

        if (cleared > 0) {
            for (PlayerAuth auth : cache.values()) {
                if(auth.getLastLogin() < until) {
                    cache.remove(auth.getNickname());
                }
            }
        }
        return cleared;
    }

    @Override
    public synchronized boolean removeAuth(String user) {
        if (source.removeAuth(user)) {
            cache.remove(user);
            return true;
        }
        return false;
    }

    @Override
    public synchronized void close() {
        source.close();
    }

    @Override
    public void reload() {
        cache.clear();
    }
}
