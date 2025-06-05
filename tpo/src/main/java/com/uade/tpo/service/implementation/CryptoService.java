package com.uade.tpo.service.implementation;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.AesBytesEncryptor;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.stereotype.Service;

@Service
public class CryptoService {

    private final BytesEncryptor encryptor;

    public CryptoService(
        @Value("${crypto.password}") String password,
        @Value("${crypto.salt}") String salt
    ) {
        this.encryptor = new AesBytesEncryptor(password, salt);
    }

    public String encrypt(String text) {
        byte[] cipher = encryptor.encrypt(text.getBytes(StandardCharsets.UTF_8));
        return new String(Base64.getEncoder().encode(cipher), StandardCharsets.UTF_8);
    }

    public String decrypt(String cipherText) {
        byte[] decoded = Base64.getDecoder().decode(cipherText.getBytes(StandardCharsets.UTF_8));
        byte[] plain  = encryptor.decrypt(decoded);
        return new String(plain, StandardCharsets.UTF_8);
    }
}

