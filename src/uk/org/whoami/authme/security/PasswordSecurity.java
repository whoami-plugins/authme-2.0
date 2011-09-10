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
package uk.org.whoami.authme.security;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import uk.org.whoami.authme.ConsoleLogger;

public class PasswordSecurity {

    public enum Hash {

        MD5, SHA1, SHA256
    }
    private Hash hash;
    private MessageDigest md5;
    private MessageDigest sha256;
    private MessageDigest sha1;

    public PasswordSecurity(Hash hash) throws NullPointerException, NoSuchAlgorithmException {
        if (hash == null) {
            throw new NullPointerException("Hash can not be null");
        }

        this.hash = hash;
        this.md5 = MessageDigest.getInstance("MD5");
        this.sha256 = MessageDigest.getInstance("SHA-256");
        this.sha1 = MessageDigest.getInstance("SHA1");

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

    public String getHash(String password) throws NoSuchAlgorithmException {
        switch (hash) {
            case MD5:
                return getMD5(password);
            case SHA1:
                return getSHA1(password);
            case SHA256:
                String salt = Long.toHexString(Double.doubleToLongBits(Math.random()));
                return getSaltedHash(password, salt);
            default:
                throw new NoSuchAlgorithmException("Unknown hash algorithm");
        }
    }

    public boolean comparePasswordWithHash(String password, String hash) {
        if (hash.length() == 32) {
            return hash.equals(getMD5(password));
        }

        if (hash.length() == 40) {
            return hash.equals(getSHA1(password));
        }

        if (hash.contains("$")) {
            String[] line = hash.split("\\$");
            if (line.length > 3 && line[1].equals("SHA")) {
                return hash.equals(getSaltedHash(password, line[2]));
            } else {
                return false;
            }
        }
        return false;
    }
}
