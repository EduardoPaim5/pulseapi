package com.fluentia.pulseapi.domain.entity;

import java.time.OffsetDateTime;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "check_runs")
public class CheckRun {
  @Id
  @Column(nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "monitor_id", nullable = false)
  private Monitor monitor;

  @Column(name = "started_at", nullable = false)
  private OffsetDateTime startedAt;

  @Column(name = "ended_at")
  private OffsetDateTime endedAt;

  @Column(nullable = false, length = 30)
  private String status;

  @Column(name = "http_status")
  private Integer httpStatus;

  @Column(nullable = false)
  private Boolean success;

  @Column(name = "status_code")
  private Integer statusCode;

  @Column(name = "latency_ms")
  private Integer latencyMs;

  @Column(name = "response_time_ms")
  private Integer responseTimeMs;

  @Column(name = "error_message", length = 2048)
  private String errorMessage;

  public CheckRun() {}

  public CheckRun(UUID id, Monitor monitor, OffsetDateTime startedAt, String status) {
    this.id = id;
    this.monitor = monitor;
    this.startedAt = startedAt;
    this.status = status;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public Monitor getMonitor() {
    return monitor;
  }

  public void setMonitor(Monitor monitor) {
    this.monitor = monitor;
  }

  public OffsetDateTime getStartedAt() {
    return startedAt;
  }

  public void setStartedAt(OffsetDateTime startedAt) {
    this.startedAt = startedAt;
  }

  public OffsetDateTime getEndedAt() {
    return endedAt;
  }

  public void setEndedAt(OffsetDateTime endedAt) {
    this.endedAt = endedAt;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Integer getHttpStatus() {
    return httpStatus;
  }

  public void setHttpStatus(Integer httpStatus) {
    this.httpStatus = httpStatus;
  }

  public Boolean getSuccess() {
    return success;
  }

  public void setSuccess(Boolean success) {
    this.success = success;
  }

  public Integer getStatusCode() {
    return statusCode;
  }

  public void setStatusCode(Integer statusCode) {
    this.statusCode = statusCode;
  }

  public Integer getLatencyMs() {
    return latencyMs;
  }

  public void setLatencyMs(Integer latencyMs) {
    this.latencyMs = latencyMs;
  }

  public Integer getResponseTimeMs() {
    return responseTimeMs;
  }

  public void setResponseTimeMs(Integer responseTimeMs) {
    this.responseTimeMs = responseTimeMs;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
}
