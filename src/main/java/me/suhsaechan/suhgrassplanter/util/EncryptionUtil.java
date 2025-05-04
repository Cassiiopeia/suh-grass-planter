package me.suhsaechan.suhgrassplanter.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionUtil {

  private static final String AES_KEY = "your-32-byte-secret-key-here1234"; // Replace with a secure key
  private static final String ALGORITHM = "AES";

  public static String encrypt(String data) throws Exception {
    SecretKeySpec key = new SecretKeySpec(AES_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
    Cipher cipher = Cipher.getInstance(ALGORITHM);
    cipher.init(Cipher.ENCRYPT_MODE, key);
    byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
    return Base64.getEncoder().encodeToString(encrypted);
  }

  public static String decrypt(String encryptedData) throws Exception {
    SecretKeySpec key = new SecretKeySpec(AES_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
    Cipher cipher = Cipher.getInstance(ALGORITHM);
    cipher.init(Cipher.DECRYPT_MODE, key);
    byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
    return new String(decrypted, StandardCharsets.UTF_8);
  }
}
