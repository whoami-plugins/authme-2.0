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

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void setIp(String ip) {
        this.ip = ip;
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
        int hashCode = 7;
        hashCode = 71 * hashCode + (this.nickname != null ? this.nickname.hashCode() : 0);
        hashCode = 71 * hashCode + (this.ip != null ? this.ip.hashCode() : 0);
        return hashCode;
    }
}
