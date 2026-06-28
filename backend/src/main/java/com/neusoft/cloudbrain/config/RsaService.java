package com.neusoft.cloudbrain.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import javax.crypto.Cipher;

@Slf4j
@Service
public class RsaService {

    @Getter
    private String publicKey;

    private java.security.PrivateKey privateKey;

    @PostConstruct
    public void init() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair pair = generator.generateKeyPair();
            publicKey = Base64.getEncoder().encodeToString(pair.getPublic().getEncoded());
            privateKey = pair.getPrivate();
            log.info("RSA 密钥对已生成");
        } catch (Exception e) {
            log.error("RSA 密钥对生成失败", e);
            throw new RuntimeException("RSA 初始化失败", e);
        }
    }

    public String decrypt(String encryptedBase64) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] encrypted = Base64.getDecoder().decode(encryptedBase64);
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("RSA 解密失败", e);
            throw new RuntimeException("解密失败", e);
        }
    }
}
