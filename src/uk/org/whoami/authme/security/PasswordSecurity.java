package uk.org.whoami.authme.security;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import uk.org.whoami.authme.ConsoleLogger;

public class PasswordSecurity {

    private MessageDigest md5;
    private MessageDigest sha256;
    private MessageDigest sha1;

    public PasswordSecurity() {
        try {
            this.md5 = MessageDigest.getInstance("MD5");
            this.sha256 = MessageDigest.getInstance("SHA-256");
            this.sha1 = MessageDigest.getInstance("SHA1");
        } catch(NoSuchAlgorithmException ex) {
            ConsoleLogger.showError(ex.getMessage());
        }
    }

    public String getMD5(String message) {
        byte[] digest;
        md5.reset();
        md5.update(message.getBytes());
        digest = md5.digest();

        return String.format("%0" + (digest.length << 1) + "x", new BigInteger(1,
                digest));
    }

    public String getSHA1(String message) {
        byte[] digest;
        sha1.reset();
        sha1.update(message.getBytes());
        digest = sha1.digest();

        return String.format("%0" + (digest.length << 1) + "x", new BigInteger(1,
                digest));
    }

    public String getSHA256(String message) {
        byte[] digest;
        sha256.reset();
        sha256.update(message.getBytes());
        digest = sha256.digest();

        return String.format("%0" + (digest.length << 1) + "x", new BigInteger(1,
                digest));
    }

    public String getSaltedHash(String message, String salt) {
        return "$SHA$" + salt + "$" + getSHA256(getSHA256(message) + salt);
    }
}
