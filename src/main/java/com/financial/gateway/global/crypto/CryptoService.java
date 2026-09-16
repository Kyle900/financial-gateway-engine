package com.financial.gateway.global.crypto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class CryptoService {

    @Value("${gateway.security.enc-key:defaultSecretKey123456789012345}")
    private String encKey;

    public String encrypt(String plainText) {
        if (plainText == null) return null;
        return Base64.getEncoder().encodeToString(plainText.getBytes(StandardCharsets.UTF_8));
    }

    public String decrypt(String cipherText) {
        if (cipherText == null) return null;
        return new String(Base64.getDecoder().decode(cipherText), StandardCharsets.UTF_8);
    }

    public boolean validateApiKey(String targetKey, String expectedKey) {
        return expectedKey != null && expectedKey.equals(targetKey);
    }
}
