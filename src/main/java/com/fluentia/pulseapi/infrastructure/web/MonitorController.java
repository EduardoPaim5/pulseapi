package com.fluentia.pulseapi.infrastructure.web;

import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fluentia.pulseapi.application.dto.MonitorDtos.CreateMonitorRequest;
import com.fluentia.pulseapi.application.dto.MonitorDtos.EnableMonitorRequest;
import com.fluentia.pulseapi.application.dto.MonitorDtos.MonitorResponse;
import com.fluentia.pulseapi.application.dto.MonitorDtos.RecheckResponse;
import com.fluentia.pulseapi.application.dto.MonitorDtos.UpdateMonitorRequest;
import com.fluentia.pulseapi.application.service.MonitorCheckService;
import com.fluentia.pulseapi.application.service.MonitorService;
import com.fluentia.pulseapi.infrastructure.security.UserPrincipal;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/monitors")
@Validated
public class MonitorController {
  private final MonitorService monitorService;
  private final MonitorCheckService monitorCheckService;

  public MonitorController(MonitorService monitorService, MonitorCheckService monitorCheckService) {
    this.monitorService = monitorService;
    this.monitorCheckService = monitorCheckService;
  }

  @GetMapping
  public ResponseEntity<List<MonitorResponse>> list(@AuthenticationPrincipal UserPrincipal principal) {
    return ResponseEntity.ok(monitorService.listByOwner(requireUser(principal)));
  }

  @PostMapping
  public ResponseEntity<MonitorResponse> create(
      @AuthenticationPrincipal UserPrincipal principal,
      @Valid @RequestBody CreateMonitorRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(monitorService.create(requireUser(principal), request));
  }

  @GetMapping("/{id}")
  public ResponseEntity<MonitorResponse> getById(
      @AuthenticationPrincipal UserPrincipal principal,
      @PathVariable UUID id) {
    return ResponseEntity.ok(monitorService.getById(requireUser(principal), id));
  }

  @PutMapping("/{id}")
  public ResponseEntity<MonitorResponse> update(
      @AuthenticationPrincipal UserPrincipal principal,
      @PathVariable UUID id,
      @Valid @RequestBody UpdateMonitorRequest request) {
    return ResponseEntity.ok(monitorService.update(requireUser(principal), id, request));
  }

  @PatchMapping("/{id}/enable")
  public ResponseEntity<MonitorResponse> enable(
      @AuthenticationPrincipal UserPrincipal principal,
      @PathVariable UUID id,
      @Valid @RequestBody EnableMonitorRequest request) {
    return ResponseEntity.ok(monitorService.updateEnabled(requireUser(principal), id, request));
  }

  @PostMapping("/{id}/recheck")
  public ResponseEntity<RecheckResponse> recheck(
      @AuthenticationPrincipal UserPrincipal principal,
      @PathVariable UUID id) {
    var user = requireUser(principal);
    var monitor = monitorService.getEntityById(user, id);
    return ResponseEntity.ok(monitorCheckService.runCheckNow(monitor));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(
      @AuthenticationPrincipal UserPrincipal principal,
      @PathVariable UUID id) {
    monitorService.delete(requireUser(principal), id);
    return ResponseEntity.noContent().build();
  }

  private com.fluentia.pulseapi.domain.entity.User requireUser(UserPrincipal principal) {
    if (principal == null) {
      throw com.fluentia.pulseapi.infrastructure.exception.ApiException.unauthorized("Token inválido");
    }
    return principal.getUser();
  }
}
