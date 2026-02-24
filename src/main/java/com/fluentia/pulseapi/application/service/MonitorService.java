package com.fluentia.pulseapi.application.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fluentia.pulseapi.application.dto.MonitorDtos.CreateMonitorRequest;
import com.fluentia.pulseapi.application.dto.MonitorDtos.EnableMonitorRequest;
import com.fluentia.pulseapi.application.dto.MonitorDtos.MonitorResponse;
import com.fluentia.pulseapi.application.dto.MonitorDtos.UpdateMonitorRequest;
import com.fluentia.pulseapi.domain.entity.Monitor;
import com.fluentia.pulseapi.domain.entity.User;
import com.fluentia.pulseapi.domain.repository.MonitorRepository;
import com.fluentia.pulseapi.infrastructure.exception.ApiException;

@Service
public class MonitorService {
  private final MonitorRepository monitorRepository;

  public MonitorService(MonitorRepository monitorRepository) {
    this.monitorRepository = monitorRepository;
  }

  public List<MonitorResponse> listByOwner(User owner) {
    return monitorRepository.findAllByOwnerId(owner.getId())
        .stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
  }

  public MonitorResponse getById(User owner, UUID id) {
    Monitor monitor = monitorRepository.findByIdAndOwnerId(id, owner.getId())
        .orElseThrow(() -> ApiException.notFound("Monitor não encontrado"));
    return toResponse(monitor);
  }

  public Monitor getEntityById(User owner, UUID id) {
    return monitorRepository.findByIdAndOwnerId(id, owner.getId())
        .orElseThrow(() -> ApiException.notFound("Monitor não encontrado"));
  }

  @Transactional
  public MonitorResponse create(User owner, CreateMonitorRequest request) {
    Monitor monitor = new Monitor(UUID.randomUUID(), owner, request.name(), request.url(),
        request.intervalSec(), request.timeoutMs(), request.enabled() != null ? request.enabled() : Boolean.TRUE);
    if (Boolean.TRUE.equals(monitor.getEnabled())) {
      monitor.setNextCheckAt(java.time.OffsetDateTime.now());
    }
    monitorRepository.save(monitor);
    return toResponse(monitor);
  }

  @Transactional
  public MonitorResponse update(User owner, UUID id, UpdateMonitorRequest request) {
    Monitor monitor = monitorRepository.findByIdAndOwnerId(id, owner.getId())
        .orElseThrow(() -> ApiException.notFound("Monitor não encontrado"));

    if (request.name() != null) {
      monitor.setName(request.name());
    }
    if (request.url() != null) {
      monitor.setUrl(request.url());
    }
    if (request.intervalSec() != null) {
      monitor.setIntervalSec(request.intervalSec());
    }
    if (request.timeoutMs() != null) {
      monitor.setTimeoutMs(request.timeoutMs());
    }
    if (request.enabled() != null) {
      monitor.setEnabled(request.enabled());
      if (request.enabled() && monitor.getNextCheckAt() == null) {
        monitor.setNextCheckAt(java.time.OffsetDateTime.now());
      }
    }

    return toResponse(monitor);
  }

  @Transactional
  public MonitorResponse updateEnabled(User owner, UUID id, EnableMonitorRequest request) {
    Monitor monitor = monitorRepository.findByIdAndOwnerId(id, owner.getId())
        .orElseThrow(() -> ApiException.notFound("Monitor não encontrado"));
    monitor.setEnabled(request.enabled());
    if (request.enabled() && monitor.getNextCheckAt() == null) {
      monitor.setNextCheckAt(java.time.OffsetDateTime.now());
    }
    return toResponse(monitor);
  }

  @Transactional
  public void delete(User owner, UUID id) {
    Monitor monitor = monitorRepository.findByIdAndOwnerId(id, owner.getId())
        .orElseThrow(() -> ApiException.notFound("Monitor não encontrado"));
    monitorRepository.delete(monitor);
  }

  private MonitorResponse toResponse(Monitor monitor) {
    return new MonitorResponse(
        monitor.getId(),
        monitor.getName(),
        monitor.getUrl(),
        monitor.getIntervalSec(),
        monitor.getTimeoutMs(),
        monitor.getEnabled(),
        monitor.getLastStatus(),
        monitor.getLastLatencyMs(),
        monitor.getLastCheckedAt(),
        monitor.getNextCheckAt(),
        monitor.getCreatedAt(),
        monitor.getUpdatedAt()
    );
  }
}
