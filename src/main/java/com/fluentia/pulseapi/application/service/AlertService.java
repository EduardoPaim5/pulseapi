package com.fluentia.pulseapi.application.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fluentia.pulseapi.application.dto.AlertDtos.AlertResponse;
import com.fluentia.pulseapi.domain.entity.Alert;
import com.fluentia.pulseapi.domain.entity.User;
import com.fluentia.pulseapi.domain.repository.AlertRepository;
import com.fluentia.pulseapi.infrastructure.exception.ApiException;

@Service
public class AlertService {
  private final AlertRepository alertRepository;

  public AlertService(AlertRepository alertRepository) {
    this.alertRepository = alertRepository;
  }

  public List<AlertResponse> list(User owner, String status) {
    List<Alert> alerts = status == null || status.equalsIgnoreCase("all")
        ? alertRepository.findAllByOwner(owner.getId())
        : alertRepository.findAllByOwnerAndStatus(owner.getId(), status.toUpperCase());

    return alerts.stream().map(this::toResponse).collect(Collectors.toList());
  }

  @Transactional
  public AlertResponse ack(User owner, UUID alertId) {
    Alert alert = alertRepository.findByIdAndOwner(alertId, owner.getId())
        .orElseThrow(() -> ApiException.notFound("Alerta não encontrado"));

    if (!"ACKED".equalsIgnoreCase(alert.getStatus())) {
      alert.setStatus("ACKED");
      alert.setAckedAt(OffsetDateTime.now());
    }

    return toResponse(alert);
  }

  private AlertResponse toResponse(Alert alert) {
    return new AlertResponse(
        alert.getId(),
        alert.getIncident().getId(),
        alert.getIncident().getMonitor().getId(),
        alert.getEvent(),
        alert.getStatus(),
        alert.getChannel(),
        alert.getCreatedAt(),
        alert.getAckedAt()
    );
  }
}
