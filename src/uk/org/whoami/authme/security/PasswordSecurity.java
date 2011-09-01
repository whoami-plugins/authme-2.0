package uk.org.whoami.authme.security;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import uk.org.whoami.authme.ConsoleLogger;

public class PasswordSecurity {

    public static final int MD5 = 1;
    public static final int SHA1 = 2;
    public static final int SHA256 = 3;
    private MessageDigest md5;
    private MessageDigest sha256;
    private MessageDigest sha1;
    private int hash;

    public PasswordSecurity(int hash) throws NoSuchAlgorithmException {
        if (hash < MD5 || hash > SHA256) {
            throw new NoSuchAlgorithmException("Unknown hash");
        }

        this.hash = hash;

        try {
            this.md5 = MessageDigest.getInstance("MD5");
            this.sha256 = MessageDigest.getInstance("SHA-256");
            this.sha1 = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException ex) {
            ConsoleLogger.showError(ex.getMessage());
        }
    }

    private String getMD5(String message) {
        byte[] digest;
        md5.reset();
        md5.update(message.getBytes());
        digest = md5.digest();

        return String.format("%0" + (digest.length << 1) + "x", new BigInteger(1,
                digest));
    }

    private String getSHA1(String message) {
        byte[] digest;
        sha1.reset();
        sha1.update(message.getBytes());
        digest = sha1.digest();

        return String.format("%0" + (digest.length << 1) + "x", new BigInteger(1,
                digest));
    }

    private String getSHA256(String message) {
        byte[] digest;
        sha256.reset();
        sha256.update(message.getBytes());
        digest = sha256.digest();

        return String.format("%0" + (digest.length << 1) + "x", new BigInteger(1,
                digest));
    }

    private String getSaltedHash(String message, String salt) {
        return "$SHA$" + salt + "$" + getSHA256(getSHA256(message) + salt);
    }

    public String getHash(String password) {
        if (hash == MD5) {
            return getMD5(password);
        } else if (hash == SHA1) {
            return getSHA1(password);
        } else {
            String salt = Long.toHexString(Double.doubleToLongBits(Math.random()));
            return getSaltedHash(password, salt);
        }
    }
    
    public boolean comparePasswordWithHash(String password, String hash) {
        if(hash.length() == 32) {
            return hash.equals(getMD5(password));
        }
        
        if(hash.length() == 40) {
            return hash.equals(getSHA1(password));
        }
        
        if(hash.contains("$")) {
            String[] line = hash.split("\\$");
            if(line.length > 3 && line[1].equals("SHA")) {
                return hash.equals(getSaltedHash(password, line[2]));
            } else {
                return false;
            }
        }
        return false;
    }
}
