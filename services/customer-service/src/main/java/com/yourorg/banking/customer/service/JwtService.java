package com.yourorg.banking.customer.service;

import com.yourorg.banking.customer.model.Customer;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final int accessTokenExpirationMinutes;
    private final int refreshTokenExpirationDays;

    public JwtService(@Value("${app.jwt.secret:mySecretKey}") String secret,
                     @Value("${app.jwt.access-token-expiration-minutes:60}") int accessTokenExpirationMinutes,
                     @Value("${app.jwt.refresh-token-expiration-days:7}") int refreshTokenExpirationDays) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenExpirationMinutes = accessTokenExpirationMinutes;
        this.refreshTokenExpirationDays = refreshTokenExpirationDays;
    }

    public String generateAccessToken(Customer customer) {
        Instant now = Instant.now();
        Instant expiration = now.plus(accessTokenExpirationMinutes, ChronoUnit.MINUTES);

        return Jwts.builder()
                .setSubject(customer.id().toString())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiration))
                .claim("email", customer.email())
                .claim("fullName", customer.fullName())
                .claim("userNo", customer.userNo())
                .claim("roles", List.of("CUSTOMER")) // This would be fetched from RBAC service
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(Customer customer) {
        Instant now = Instant.now();
        Instant expiration = now.plus(refreshTokenExpirationDays, ChronoUnit.DAYS);

        return Jwts.builder()
                .setSubject(customer.id().toString())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiration))
                .claim("type", "refresh")
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = parseToken(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public String getSubject(String token) {
        return parseToken(token).getSubject();
    }
}

