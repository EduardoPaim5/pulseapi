package com.fluentia.pulseapi.application.service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import com.fluentia.pulseapi.application.dto.MonitorDtos.RecheckResponse;
import com.fluentia.pulseapi.domain.entity.Alert;
import com.fluentia.pulseapi.domain.entity.CheckRun;
import com.fluentia.pulseapi.domain.entity.Incident;
import com.fluentia.pulseapi.domain.entity.Monitor;
import com.fluentia.pulseapi.domain.repository.AlertRepository;
import com.fluentia.pulseapi.domain.repository.CheckRunRepository;
import com.fluentia.pulseapi.domain.repository.IncidentRepository;
import com.fluentia.pulseapi.domain.repository.MonitorRepository;

@Service
public class MonitorCheckService {
  private static final Logger logger = LoggerFactory.getLogger(MonitorCheckService.class);
  private static final String STATUS_UP = "UP";
  private static final String STATUS_DOWN = "DOWN";
  private static final String INCIDENT_OPEN = "OPEN";
  private static final String INCIDENT_CLOSED = "CLOSED";
  private static final String ALERT_UNACKED = "UNACKED";
  private static final String EVENT_DOWN = "DOWN";
  private static final String EVENT_UP = "UP";

  private final MonitorRepository monitorRepository;
  private final CheckRunRepository checkRunRepository;
  private final IncidentRepository incidentRepository;
  private final AlertRepository alertRepository;
  private final RestTemplate restTemplate;

  public MonitorCheckService(MonitorRepository monitorRepository,
      CheckRunRepository checkRunRepository,
      IncidentRepository incidentRepository,
      AlertRepository alertRepository,
      RestTemplate restTemplate) {
    this.monitorRepository = monitorRepository;
    this.checkRunRepository = checkRunRepository;
    this.incidentRepository = incidentRepository;
    this.alertRepository = alertRepository;
    this.restTemplate = restTemplate;
  }

  @Transactional
  public void runCheck(Monitor monitor) {
    performCheck(monitor);
  }

  @Transactional
  public RecheckResponse runCheckNow(Monitor monitor) {
    return performCheck(monitor);
  }

  private RecheckResponse performCheck(Monitor monitor) {
    OffsetDateTime startedAt = OffsetDateTime.now();
    long startNanos = System.nanoTime();

    Integer statusCode = null;
    Integer latencyMs = null;
    String errorMessage = null;
    boolean success = false;

    try {
      ResponseEntity<String> response = restTemplate.getForEntity(monitor.getUrl(), String.class);
      statusCode = response.getStatusCode().value();
      latencyMs = toMs(System.nanoTime() - startNanos);
      success = response.getStatusCode().is2xxSuccessful();
    } catch (ResourceAccessException ex) {
      latencyMs = toMs(System.nanoTime() - startNanos);
      errorMessage = "Timeout ou erro de conexão";
      logger.warn("Timeout/conexao no monitor {}", monitor.getId(), ex);
    } catch (RestClientException ex) {
      latencyMs = toMs(System.nanoTime() - startNanos);
      errorMessage = "Erro ao chamar endpoint";
      logger.warn("Erro HTTP no monitor {}", monitor.getId(), ex);
    }

    CheckRun checkRun = new CheckRun(UUID.randomUUID(), monitor, startedAt, success ? STATUS_UP : STATUS_DOWN);
    checkRun.setSuccess(success);
    checkRun.setStatusCode(statusCode);
    checkRun.setLatencyMs(latencyMs);
    checkRun.setErrorMessage(errorMessage);
    checkRunRepository.save(checkRun);

    monitor.setLastStatus(success ? STATUS_UP : STATUS_DOWN);
    monitor.setLastLatencyMs(latencyMs);
    monitor.setLastCheckedAt(startedAt);
    monitor.setNextCheckAt(startedAt.plusSeconds(monitor.getIntervalSec()));

    updateConsecutiveCounters(monitor, success);
    handleIncidentsAndAlerts(monitor, startedAt);
    monitorRepository.save(monitor);

    return new RecheckResponse(success, statusCode, latencyMs, errorMessage, startedAt);
  }

  private Integer toMs(long nanos) {
    return (int) Duration.ofNanos(nanos).toMillis();
  }

  private void updateConsecutiveCounters(Monitor monitor, boolean success) {
    if (success) {
      monitor.setConsecutiveSuccesses(monitor.getConsecutiveSuccesses() + 1);
      monitor.setConsecutiveFailures(0);
    } else {
      monitor.setConsecutiveFailures(monitor.getConsecutiveFailures() + 1);
      monitor.setConsecutiveSuccesses(0);
    }
  }

  private void handleIncidentsAndAlerts(Monitor monitor, OffsetDateTime now) {
    if (monitor.getConsecutiveFailures() >= 2) {
      Incident incident = incidentRepository.findFirstByMonitorIdAndStatus(monitor.getId(), INCIDENT_OPEN)
          .orElseGet(() -> openIncident(monitor, now));
      createAlertIfMissing(incident, EVENT_DOWN, now);
      return;
    }

    if (monitor.getConsecutiveSuccesses() >= 2) {
      incidentRepository.findFirstByMonitorIdAndStatus(monitor.getId(), INCIDENT_OPEN)
          .ifPresent(incident -> closeIncident(incident, now));
    }
  }

  private Incident openIncident(Monitor monitor, OffsetDateTime now) {
    Incident incident = new Incident(UUID.randomUUID(), monitor, now, INCIDENT_OPEN);
    return incidentRepository.save(incident);
  }

  private void closeIncident(Incident incident, OffsetDateTime now) {
    incident.setStatus(INCIDENT_CLOSED);
    incident.setResolvedAt(now);
    incidentRepository.save(incident);
    createAlertIfMissing(incident, EVENT_UP, now);
  }

  private void createAlertIfMissing(Incident incident, String event, OffsetDateTime now) {
    if (alertRepository.existsByIncidentIdAndEvent(incident.getId(), event)) {
      return;
    }
    Alert alert = new Alert(UUID.randomUUID(), incident, "SYSTEM", ALERT_UNACKED, event, now);
    alertRepository.save(alert);
  }
}
