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
  public AuthResponse register(RegisterRequest request) {
    if (userRepository.existsByEmail(request.email())) {
      throw ApiException.badRequest("E-mail já cadastrado");
    }

    User user = new User(UUID.randomUUID(), request.email().toLowerCase(),
        passwordEncoder.encode(request.password()), UserRole.USER);
    userRepository.save(user);

    String token = jwtService.generateToken(user);
    return new AuthResponse(token, "Bearer");
  }

  public AuthResponse login(LoginRequest request) {
    User user = userRepository.findByEmail(request.email().toLowerCase())
        .orElseThrow(() -> ApiException.unauthorized("Credenciais inválidas"));

    if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
      throw ApiException.unauthorized("Credenciais inválidas");
    }

    String token = jwtService.generateToken(user);
    return new AuthResponse(token, "Bearer");
  }
}
