package ch.bzz.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Utility class for password hashing and salt generation
 * Provides secure password handling for user authentication
 */
public class PasswordHandler {

    /**
     * Generates a random salt for password hashing
     * @return byte array containing the generated salt
     */
    public static byte[] generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    /**
     * Hashes a password using SHA-256 with the provided salt
     * @param password the plain text password to hash
     * @param salt the salt to use for hashing
     * @return byte array containing the hashed password
     * @throws NoSuchAlgorithmException if SHA-256 algorithm is not available
     */
    public static byte[] hashPassword(String password, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(salt);
        return md.digest(password.getBytes());
    }
}