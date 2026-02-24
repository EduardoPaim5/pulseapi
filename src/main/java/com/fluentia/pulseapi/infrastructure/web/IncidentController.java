package com.fluentia.pulseapi.infrastructure.web;

import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.fluentia.pulseapi.application.dto.IncidentDtos.IncidentResponse;
import com.fluentia.pulseapi.application.service.IncidentService;
import com.fluentia.pulseapi.infrastructure.exception.ApiException;
import com.fluentia.pulseapi.infrastructure.security.UserPrincipal;

@RestController
@RequestMapping("/api/v1/monitors/{id}/incidents")
public class IncidentController {
  private final IncidentService incidentService;

  public IncidentController(IncidentService incidentService) {
    this.incidentService = incidentService;
  }

  @GetMapping
  public ResponseEntity<List<IncidentResponse>> list(
      @AuthenticationPrincipal UserPrincipal principal,
      @PathVariable("id") UUID monitorId,
      @RequestParam(value = "status", required = false) String status) {
    if (principal == null) {
      throw ApiException.unauthorized("Token inválido");
    }
    return ResponseEntity.ok(incidentService.listByMonitor(principal.getUser(), monitorId, status));
  }
}
