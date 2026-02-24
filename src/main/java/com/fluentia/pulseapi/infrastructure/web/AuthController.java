package com.fluentia.pulseapi.infrastructure.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fluentia.pulseapi.application.dto.AuthDtos.AuthResponse;
import com.fluentia.pulseapi.application.dto.AuthDtos.LoginRequest;
import com.fluentia.pulseapi.application.dto.AuthDtos.MeResponse;
import com.fluentia.pulseapi.application.dto.AuthDtos.RegisterRequest;
import com.fluentia.pulseapi.application.service.AuthService;
import com.fluentia.pulseapi.infrastructure.security.UserPrincipal;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@Validated
public class AuthController {
  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    return ResponseEntity.ok(authService.login(request));
  }

  @GetMapping("/me")
  public ResponseEntity<MeResponse> me(@AuthenticationPrincipal UserPrincipal principal) {
    if (principal == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    return ResponseEntity.ok(new MeResponse(
        principal.getUser().getId().toString(),
        principal.getUser().getEmail(),
        principal.getUser().getRole().name()
    ));
  }
}
