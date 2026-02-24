package com.fluentia.pulseapi.application.service;

import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fluentia.pulseapi.application.dto.AuthDtos.AuthResponse;
import com.fluentia.pulseapi.application.dto.AuthDtos.LoginRequest;
import com.fluentia.pulseapi.application.dto.AuthDtos.RegisterRequest;
import com.fluentia.pulseapi.domain.entity.User;
import com.fluentia.pulseapi.domain.entity.UserRole;
import com.fluentia.pulseapi.domain.repository.UserRepository;
import com.fluentia.pulseapi.infrastructure.exception.ApiException;
import com.fluentia.pulseapi.infrastructure.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

@Service
public class AuthService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
  }

  @Transactional
  public TokenPair register(RegisterRequest request) {
    if (userRepository.existsByEmail(request.email())) {
      throw ApiException.badRequest("E-mail já cadastrado");
    }

    User user = new User(UUID.randomUUID(), request.email().toLowerCase(),
        passwordEncoder.encode(request.password()), UserRole.USER);
    userRepository.save(user);

    return issueTokenPair(user);
  }

  public TokenPair login(LoginRequest request) {
    User user = userRepository.findByEmail(request.email().toLowerCase())
        .orElseThrow(() -> ApiException.unauthorized("Credenciais inválidas"));

    if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
      throw ApiException.unauthorized("Credenciais inválidas");
    }

    return issueTokenPair(user);
  }

  public AuthResponse refreshAccessToken(String refreshToken) {
    if (refreshToken == null || refreshToken.isBlank()) {
      throw ApiException.unauthorized("Refresh token ausente");
    }
    try {
      Claims claims = jwtService.parseToken(refreshToken);
      if (!jwtService.isRefreshToken(claims)) {
        throw ApiException.unauthorized("Refresh token inválido");
      }
      User user = userRepository.findById(jwtService.getUserId(claims))
          .orElseThrow(() -> ApiException.unauthorized("Usuário inválido"));
      return new AuthResponse(jwtService.generateAccessToken(user), "Bearer");
    } catch (JwtException ex) {
      throw ApiException.unauthorized("Refresh token inválido");
    }
  }

  private TokenPair issueTokenPair(User user) {
    String accessToken = jwtService.generateAccessToken(user);
    String refreshToken = jwtService.generateRefreshToken(user);
    return new TokenPair(new AuthResponse(accessToken, "Bearer"), refreshToken);
  }

  public record TokenPair(AuthResponse authResponse, String refreshToken) {}
}
