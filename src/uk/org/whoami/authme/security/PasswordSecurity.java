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

public class PasswordSecurity {

    private static String getMD5(String message) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");

        md5.reset();
        md5.update(message.getBytes());
        byte[] digest = md5.digest();

        return String.format("%0" + (digest.length << 1) + "x", new BigInteger(1,
                digest));
    }

    private static String getSHA1(String message) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA1");
        sha1.reset();
        sha1.update(message.getBytes());
        byte[] digest = sha1.digest();

        return String.format("%0" + (digest.length << 1) + "x", new BigInteger(1,
                digest));
    }

    private static String getSHA256(String message) throws NoSuchAlgorithmException {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");

        sha256.reset();
        sha256.update(message.getBytes());
        byte[] digest = sha256.digest();

        return String.format("%0" + (digest.length << 1) + "x", new BigInteger(1,
                digest));
    }

    public static String getWhirlpool(String message) {
        Whirlpool w = new Whirlpool();
        byte[] digest = new byte[Whirlpool.DIGESTBYTES];
        w.NESSIEinit();
        w.NESSIEadd(message);
        w.NESSIEfinalize(digest);
        return Whirlpool.display(digest);
    }

    private static String getSaltedHash(String message, String salt) throws NoSuchAlgorithmException {
        return "$SHA$" + salt + "$" + getSHA256(getSHA256(message) + salt);
    }

    public static String getHash(HashAlgorithm alg, String password) throws NoSuchAlgorithmException {
        switch (alg) {
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

    public static boolean comparePasswordWithHash(String password, String hash) throws NoSuchAlgorithmException {
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

    public enum HashAlgorithm {

        MD5, SHA1, SHA256
    }
}
