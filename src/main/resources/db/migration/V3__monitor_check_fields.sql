ALTER TABLE monitors
  ADD COLUMN last_status VARCHAR(10),
  ADD COLUMN last_latency_ms INTEGER,
  ADD COLUMN last_checked_at TIMESTAMPTZ,
  ADD COLUMN next_check_at TIMESTAMPTZ;

ALTER TABLE check_runs
  ADD COLUMN success BOOLEAN NOT NULL DEFAULT FALSE,
  ADD COLUMN status_code INTEGER,
  ADD COLUMN latency_ms INTEGER;

CREATE INDEX idx_monitors_next_check ON monitors(next_check_at);
