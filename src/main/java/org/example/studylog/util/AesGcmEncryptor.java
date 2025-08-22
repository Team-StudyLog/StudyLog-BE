package org.example.studylog.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class AesGcmEncryptor {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LEN = 12;     // 96-bit nonce (권장)
    private static final int TAG_LEN = 128;   // bits
    private final SecretKeySpec key;

    public AesGcmEncryptor(@Value("${app.crypto.master-key-base64}") String base64Key) {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key); // 32 bytes(256-bit) 권장
        this.key = new SecretKeySpec(keyBytes, "AES");
    }

    public String encrypt(String plaintext) {
        if (plaintext == null) return null;
        try {
            byte[] iv = new byte[IV_LEN];
            SecureRandom.getInstanceStrong().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LEN, iv));
            byte[] ct = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            ByteBuffer bb = ByteBuffer.allocate(iv.length + ct.length);
            bb.put(iv).put(ct);
            // 버전접두어(키 로테이션 대비)
            return "v1:" + Base64.getUrlEncoder().withoutPadding().encodeToString(bb.array());
        } catch (Exception e) {
            throw new IllegalStateException("Encrypt failed", e);
        }
    }

    public String decrypt(String ciphertext) {
        if (ciphertext == null) return null;
        try {
            String payload = ciphertext.startsWith("v1:") ? ciphertext.substring(3) : ciphertext;
            byte[] enc = Base64.getUrlDecoder().decode(payload);

            ByteBuffer bb = ByteBuffer.wrap(enc);
            byte[] iv = new byte[IV_LEN];
            bb.get(iv);
            byte[] ct = new byte[bb.remaining()];
            bb.get(ct);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LEN, iv));
            byte[] pt = cipher.doFinal(ct);
            return new String(pt, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Decrypt failed", e);
        }
    }

}
