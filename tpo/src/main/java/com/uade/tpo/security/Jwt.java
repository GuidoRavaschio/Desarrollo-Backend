package com.uade.tpo.security;

import java.util.Base64;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
public class Jwt {

    @Value("${application.security.jwt.secretKey}")
    private String secretKeyEncoded;

    @Value("${application.security.jwt.expiration}")
    private long expirationMillis;

    private SecretKey key;

    @PostConstruct
    public void init() {
        byte[] decodedKey = Base64.getUrlDecoder().decode(secretKeyEncoded);
        this.key = Keys.hmacShaKeyFor(decodedKey);
    }

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        if (token == null || token.isBlank()) {
            throw new RuntimeException("El token está vacío o es nulo");
        }
        String tokenPurified = token.startsWith("Bearer ") ? token.substring(7) : token;
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(tokenPurified)
                    .getBody();
            return claims.getSubject();
        } catch (Exception e) {
            throw new RuntimeException("El token no es válido: " + e.getMessage(), e);
        }
    }
}
