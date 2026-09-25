package com.studentresults.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public final class PasswordUtil {
    private static final int ITERATIONS = 65_536;
    private static final int KEY_LENGTH = 256;

    private PasswordUtil() { }

    public static String newSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hash(String password, String encodedSalt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(
                    password.toCharArray(),
                    Base64.getDecoder().decode(encodedSalt),
                    ITERATIONS,
                    KEY_LENGTH
            );
            byte[] hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec).getEncoded();
            spec.clearPassword();
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not hash password", exception);
        }
    }

    public static boolean matches(String password, String salt, String expectedHash) {
        return MessageDigest.isEqual(
                hash(password, salt).getBytes(),
                expectedHash.getBytes()
        );
    }
}
