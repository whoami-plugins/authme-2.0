package uk.org.whoami.authme.cache.auth;

public class PlayerAuth {

    private String nickname;
    private String hash;
    private String ip;

    public PlayerAuth(String nickname, String hash, String ip) {
        this.nickname = nickname;
        this.hash = hash;
        this.ip = ip;
    }

    public String getIp() {
        return ip;
    }

    public String getNickname() {
        return nickname;
    }

    public String getHash() {
        return hash;
    }
    
    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof PlayerAuth)) return false;
        PlayerAuth other = (PlayerAuth) obj;
        if(other.getIp().equals(this.ip) && other.getNickname().equals(this.nickname)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + (this.nickname != null ? this.nickname.hashCode() : 0);
        hash = 71 * hash + (this.ip != null ? this.ip.hashCode() : 0);
        return hash;
    }
}
