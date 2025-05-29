package com.universita.segreteria.security;

import com.universita.segreteria.model.Utente;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expirationMillis;

    private Key key;

    @PostConstruct
    public void init() {
        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException("jwt.secret must be at least 32 characters long");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }


    // Crea un nuovo token per un utente
    public String generateToken(Utente utente) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("ruolo", utente.getRuolo().name());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(utente.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(SignatureAlgorithm.HS256, key)
                .compact();
    }

    // Estrae la username/email dal token
    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    // Estrae il ruolo dal token
    public String extractRuolo(String token) {
        return (String) getClaims(token).get("ruolo");
    }

    // Verifica se il token è valido per l'utente
    public boolean isTokenValid(String token, Utente utente) {
        String email = extractUsername(token);
        return email.equals(utente.getEmail()) && !isTokenExpired(token);
    }

    // Crea un nuovo token mantenendo le stesse info (senza refresh token separato)
    public String refreshToken(String oldToken) {
        Claims claims = getClaims(oldToken);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(claims.getSubject())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith( SignatureAlgorithm.HS256, key)
                .compact();
    }

    // === Private helpers ===

    private Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean isTokenExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }
}
