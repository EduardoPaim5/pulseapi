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
@Table(name = "alerts")
public class Alert {
  @Id
  @Column(nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "incident_id", nullable = false)
  private Incident incident;

  @Column(nullable = false, length = 30)
  private String channel;

  @Column(nullable = false, length = 30)
  private String status;

  @Column(nullable = false, length = 10)
  private String event;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "acked_at")
  private OffsetDateTime ackedAt;

  public Alert() {}

  public Alert(UUID id, Incident incident, String channel, String status, String event, OffsetDateTime createdAt) {
    this.id = id;
    this.incident = incident;
    this.channel = channel;
    this.status = status;
    this.event = event;
    this.createdAt = createdAt;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public Incident getIncident() {
    return incident;
  }

  public void setIncident(Incident incident) {
    this.incident = incident;
  }

  public String getChannel() {
    return channel;
  }

  public void setChannel(String channel) {
    this.channel = channel;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getEvent() {
    return event;
  }

  public void setEvent(String event) {
    this.event = event;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public OffsetDateTime getAckedAt() {
    return ackedAt;
  }

  public void setAckedAt(OffsetDateTime ackedAt) {
    this.ackedAt = ackedAt;
  }
}
