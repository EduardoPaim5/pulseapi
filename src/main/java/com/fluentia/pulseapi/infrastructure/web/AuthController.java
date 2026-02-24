package com.fluentia.pulseapi.infrastructure.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.CookieValue;
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
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/auth")
@Validated
public class AuthController {
  private static final String REFRESH_COOKIE = "pulseapi_refresh";

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
    var tokenPair = authService.register(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .header("Set-Cookie", buildRefreshCookie(tokenPair.refreshToken(), httpRequest, false).toString())
        .body(tokenPair.authResponse());
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
    var tokenPair = authService.login(request);
    return ResponseEntity.ok()
        .header("Set-Cookie", buildRefreshCookie(tokenPair.refreshToken(), httpRequest, false).toString())
        .body(tokenPair.authResponse());
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthResponse> refresh(
      @CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken) {
    return ResponseEntity.ok(authService.refreshAccessToken(refreshToken));
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(HttpServletRequest httpRequest) {
    return ResponseEntity.noContent()
        .header("Set-Cookie", buildRefreshCookie("", httpRequest, true).toString())
        .build();
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

  private ResponseCookie buildRefreshCookie(String value, HttpServletRequest request, boolean clear) {
    boolean secure = request.isSecure() || "https".equalsIgnoreCase(request.getHeader("X-Forwarded-Proto"));
    String sameSite = secure ? "None" : "Lax";
    return ResponseCookie.from(REFRESH_COOKIE, value)
        .httpOnly(true)
        .secure(secure)
        .path("/api/v1/auth")
        .sameSite(sameSite)
        .maxAge(clear ? 0 : 7L * 24 * 60 * 60)
        .build();
  }
}
