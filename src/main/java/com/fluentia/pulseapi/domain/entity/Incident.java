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
@Table(name = "incidents")
public class Incident {
  @Id
  @Column(nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "monitor_id", nullable = false)
  private Monitor monitor;

  @Column(name = "opened_at", nullable = false)
  private OffsetDateTime openedAt;

  @Column(name = "resolved_at")
  private OffsetDateTime resolvedAt;

  @Column(nullable = false, length = 30)
  private String status;

  public Incident() {}

  public Incident(UUID id, Monitor monitor, OffsetDateTime openedAt, String status) {
    this.id = id;
    this.monitor = monitor;
    this.openedAt = openedAt;
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

  public OffsetDateTime getOpenedAt() {
    return openedAt;
  }

  public void setOpenedAt(OffsetDateTime openedAt) {
    this.openedAt = openedAt;
  }

  public OffsetDateTime getResolvedAt() {
    return resolvedAt;
  }

  public void setResolvedAt(OffsetDateTime resolvedAt) {
    this.resolvedAt = resolvedAt;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
