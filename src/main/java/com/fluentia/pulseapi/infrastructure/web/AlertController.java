package com.fluentia.pulseapi.infrastructure.web;

import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.fluentia.pulseapi.application.dto.AlertDtos.AlertResponse;
import com.fluentia.pulseapi.application.service.AlertService;
import com.fluentia.pulseapi.infrastructure.exception.ApiException;
import com.fluentia.pulseapi.infrastructure.security.UserPrincipal;

@RestController
@RequestMapping("/api/v1/alerts")
public class AlertController {
  private final AlertService alertService;

  public AlertController(AlertService alertService) {
    this.alertService = alertService;
  }

  @GetMapping
  public ResponseEntity<List<AlertResponse>> list(
      @AuthenticationPrincipal UserPrincipal principal,
      @RequestParam(value = "status", required = false) String status) {
    if (principal == null) {
      throw ApiException.unauthorized("Token inválido");
    }
    return ResponseEntity.ok(alertService.list(principal.getUser(), status));
  }

  @PostMapping("/{alertId}/ack")
  public ResponseEntity<AlertResponse> ack(
      @AuthenticationPrincipal UserPrincipal principal,
      @PathVariable UUID alertId) {
    if (principal == null) {
      throw ApiException.unauthorized("Token inválido");
    }
    return ResponseEntity.ok(alertService.ack(principal.getUser(), alertId));
  }
}
