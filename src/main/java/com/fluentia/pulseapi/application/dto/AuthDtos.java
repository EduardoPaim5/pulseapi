package com.fluentia.pulseapi.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDtos {
  public record RegisterRequest(
      @Email @NotBlank String email,
      @NotBlank @Size(min = 6, max = 72) String password
  ) {}

  public record LoginRequest(
      @Email @NotBlank String email,
      @NotBlank String password
  ) {}

  public record AuthResponse(
      String accessToken,
      String tokenType
  ) {}

  public record MeResponse(
      String id,
      String email,
      String role
  ) {}
}
