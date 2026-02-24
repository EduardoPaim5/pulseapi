package com.fluentia.pulseapi.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;
import com.fluentia.pulseapi.domain.entity.User;
import com.fluentia.pulseapi.infrastructure.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
  private static final String CLAIM_TYPE = "type";
  private static final String TYPE_ACCESS = "access";
  private static final String TYPE_REFRESH = "refresh";

  private final JwtProperties properties;
  private final SecretKey secretKey;

  public JwtService(JwtProperties properties) {
    this.properties = properties;
    this.secretKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
  }

  public String generateAccessToken(User user) {
    Date now = new Date();
    Date expiration = new Date(now.getTime() + properties.expirationMinutes() * 60_000L);

    return Jwts.builder()
        .subject(user.getId().toString())
        .claim(CLAIM_TYPE, TYPE_ACCESS)
        .claim("email", user.getEmail())
        .claim("role", user.getRole().name())
        .issuedAt(now)
        .expiration(expiration)
        .signWith(secretKey, Jwts.SIG.HS256)
        .compact();
  }

  public String generateRefreshToken(User user) {
    Date now = new Date();
    Date expiration = new Date(now.getTime() + properties.refreshExpirationDays() * 24 * 60 * 60_000L);

    return Jwts.builder()
        .subject(user.getId().toString())
        .claim(CLAIM_TYPE, TYPE_REFRESH)
        .issuedAt(now)
        .expiration(expiration)
        .signWith(secretKey, Jwts.SIG.HS256)
        .compact();
  }

  public Claims parseToken(String token) {
    return Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  public UUID getUserId(Claims claims) {
    return UUID.fromString(claims.getSubject());
  }

  public boolean isAccessToken(Claims claims) {
    return TYPE_ACCESS.equals(claims.get(CLAIM_TYPE, String.class));
  }

  public boolean isRefreshToken(Claims claims) {
    return TYPE_REFRESH.equals(claims.get(CLAIM_TYPE, String.class));
  }
}
