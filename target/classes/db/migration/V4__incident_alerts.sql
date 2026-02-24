ALTER TABLE monitors
  ADD COLUMN consecutive_failures INTEGER NOT NULL DEFAULT 0,
  ADD COLUMN consecutive_successes INTEGER NOT NULL DEFAULT 0;

ALTER TABLE alerts
  ADD COLUMN event VARCHAR(10) NOT NULL DEFAULT 'DOWN',
  ADD COLUMN acked_at TIMESTAMPTZ;

CREATE INDEX idx_incidents_monitor_status ON incidents(monitor_id, status);
CREATE INDEX idx_alerts_status ON alerts(status);
CREATE INDEX idx_alerts_incident_event ON alerts(incident_id, event);
