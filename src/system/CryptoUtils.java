package system;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public final class CryptoUtils {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int AES_KEY_BYTES = 16;
    private static final int GCM_TAG_BITS = 128;
    private static final int GCM_IV_BYTES = 12;
    private static final int PIN_ITERATIONS = 120_000;
    private static final int PIN_KEY_LEN_BITS = 256;
    private static final int PIN_SALT_BYTES = 16;

    private CryptoUtils() {
    }

    public static byte[] deriveAesKeyFromSecret(String secret) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] digest = sha256.digest(secret.getBytes(StandardCharsets.UTF_8));
            return Arrays.copyOf(digest, AES_KEY_BYTES);
        } catch (Exception ex) {
            throw new IllegalStateException("无法生成数据库密钥", ex);
        }
    }

    public static byte[] randomIv() {
        byte[] iv = new byte[GCM_IV_BYTES];
        RANDOM.nextBytes(iv);
        return iv;
    }

    public static byte[] encryptAesGcm(byte[] plainBytes, byte[] key, byte[] iv) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(GCM_TAG_BITS, iv));
            return cipher.doFinal(plainBytes);
        } catch (Exception ex) {
            throw new IllegalStateException("数据库加密失败", ex);
        }
    }

    public static byte[] decryptAesGcm(byte[] cipherBytes, byte[] key, byte[] iv) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(GCM_TAG_BITS, iv));
            return cipher.doFinal(cipherBytes);
        } catch (Exception ex) {
            throw new IllegalStateException("数据库解密失败，可能是密钥不正确或数据被篡改", ex);
        }
    }

    public static String newSaltBase64() {
        byte[] salt = new byte[PIN_SALT_BYTES];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hashPinBase64(String pin, String saltBase64) {
        try {
            byte[] salt = Base64.getDecoder().decode(saltBase64);
            KeySpec spec = new PBEKeySpec(pin.toCharArray(), salt, PIN_ITERATIONS, PIN_KEY_LEN_BITS);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception ex) {
            throw new IllegalStateException("密码哈希失败", ex);
        }
    }

    public static boolean verifyPin(String pin, String saltBase64, String expectedHashBase64) {
        String computed = hashPinBase64(pin, saltBase64);
        return MessageDigest.isEqual(computed.getBytes(StandardCharsets.UTF_8),
                expectedHashBase64.getBytes(StandardCharsets.UTF_8));
    }
}
