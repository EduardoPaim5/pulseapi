package com.fluentia.pulseapi.infrastructure.web;

import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.fluentia.pulseapi.application.dto.MetricsDtos.DashboardOverviewResponse;
import com.fluentia.pulseapi.application.dto.MetricsDtos.MonitorSummaryResponse;
import com.fluentia.pulseapi.application.service.MetricsService;
import com.fluentia.pulseapi.infrastructure.exception.ApiException;
import com.fluentia.pulseapi.infrastructure.security.UserPrincipal;

@RestController
@RequestMapping("/api/v1")
public class MetricsController {
  private final MetricsService metricsService;

  public MetricsController(MetricsService metricsService) {
    this.metricsService = metricsService;
  }

  @GetMapping("/monitors/{id}/checks/summary")
  public ResponseEntity<MonitorSummaryResponse> monitorSummary(
      @AuthenticationPrincipal UserPrincipal principal,
      @PathVariable("id") UUID monitorId,
      @RequestParam("window") String window) {
    if (principal == null) {
      throw ApiException.unauthorized("Token inválido");
    }
    return ResponseEntity.ok(metricsService.getMonitorSummary(principal.getUser(), monitorId, window));
  }

  @GetMapping("/dashboard/overview")
  public ResponseEntity<DashboardOverviewResponse> overview(
      @AuthenticationPrincipal UserPrincipal principal,
      @RequestParam("window") String window) {
    if (principal == null) {
      throw ApiException.unauthorized("Token inválido");
    }
    return ResponseEntity.ok(metricsService.getOverview(principal.getUser(), window));
  }
}
