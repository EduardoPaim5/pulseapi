package com.fluentia.pulseapi.application.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import com.fluentia.pulseapi.application.dto.IncidentDtos.IncidentResponse;
import com.fluentia.pulseapi.domain.entity.Incident;
import com.fluentia.pulseapi.domain.entity.Monitor;
import com.fluentia.pulseapi.domain.entity.User;
import com.fluentia.pulseapi.domain.repository.IncidentRepository;
import com.fluentia.pulseapi.domain.repository.MonitorRepository;
import com.fluentia.pulseapi.infrastructure.exception.ApiException;

@Service
public class IncidentService {
  private final IncidentRepository incidentRepository;
  private final MonitorRepository monitorRepository;

  public IncidentService(IncidentRepository incidentRepository, MonitorRepository monitorRepository) {
    this.incidentRepository = incidentRepository;
    this.monitorRepository = monitorRepository;
  }

  public List<IncidentResponse> listByMonitor(User owner, UUID monitorId, String status) {
    Monitor monitor = monitorRepository.findByIdAndOwnerId(monitorId, owner.getId())
        .orElseThrow(() -> ApiException.notFound("Monitor não encontrado"));

    List<Incident> incidents = status == null
        ? incidentRepository.findByMonitorId(monitor.getId())
        : incidentRepository.findByMonitorIdAndStatus(monitor.getId(), status.toUpperCase());

    return incidents.stream().map(this::toResponse).collect(Collectors.toList());
  }

  private IncidentResponse toResponse(Incident incident) {
    return new IncidentResponse(
        incident.getId(),
        incident.getMonitor().getId(),
        incident.getStatus(),
        incident.getOpenedAt(),
        incident.getResolvedAt()
    );
  }
}
